package com.waslataam.auth;

import com.waslataam.domain.Location;
import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;
import com.waslataam.domain.VehicleType;
import com.waslataam.factory.UserFactory;
import com.waslataam.factory.UserParams;
import com.waslataam.repository.AccountRecord;
import com.waslataam.repository.AccountRepository;
import com.waslataam.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Registration and sign-in rules for the application.
 *
 * <p>Public sign-up supports donors, recipients and volunteers. Administrator
 * accounts are provisioned from demo seed data (or by a future management tool)
 * and are additionally checked against the administrator allowlist, so the
 * sign-up form can never create an admin.</p>
 */
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(5);
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Location DEFAULT_LOCATION =
            new Location(24.8607, 67.0011, "Karachi, Pakistan", "Karachi");

    private final AccountRepository accounts;
    private final UserRepository users;
    private final UserFactory userFactory;
    private final AdminPolicy adminPolicy;

    public AuthService(AccountRepository accounts,
                       UserRepository users,
                       UserFactory userFactory,
                       AdminPolicy adminPolicy) {
        this.accounts = accounts;
        this.users = users;
        this.userFactory = userFactory;
        this.adminPolicy = adminPolicy;
    }

    /**
     * Creates a donor, recipient or volunteer account and returns the saved user.
     * Administrator registration is rejected by design.
     */
    public User register(String name,
                         String email,
                         String password,
                         String confirmPassword,
                         UserRole role,
                         String organizationName) throws AuthException {
        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = normalize(email);

        if (role == null) {
            throw new AuthException(AuthException.Reason.VALIDATION, "Choose the role you are signing up as.");
        }
        if (role == UserRole.ADMIN) {
            throw new AuthException(AuthException.Reason.ADMIN_RESTRICTED,
                    "Administrator accounts are provisioned internally and cannot be created from this form.");
        }
        if (cleanName.length() < 2) {
            throw new AuthException(AuthException.Reason.VALIDATION, "Enter your full name.");
        }
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new AuthException(AuthException.Reason.VALIDATION, "Enter a valid email address.");
        }
        validatePassword(password, confirmPassword);

        if (accounts.existsByEmail(cleanEmail) || users.findByEmail(cleanEmail).isPresent()) {
            throw new AuthException(AuthException.Reason.EMAIL_TAKEN,
                    "An account with this email already exists. Sign in instead.");
        }

        UserParams.Builder params = UserParams.builder()
                .role(role)
                .name(cleanName)
                .email(cleanEmail)
                .location(DEFAULT_LOCATION);

        switch (role) {
            case DONOR -> params.organizationName(
                    organizationName == null || organizationName.isBlank()
                            ? cleanName + " Foundation"
                            : organizationName.trim());
            case RECIPIENT -> params.capacityKg(500).reliabilityScore(0.85);
            case VOLUNTEER -> params.vehicleType(VehicleType.CAR);
            case ADMIN -> throw new AuthException(AuthException.Reason.ADMIN_RESTRICTED,
                    "Administrator accounts are provisioned internally.");
        }

        User user = userFactory.create(params.build());
        users.save(user);

        PasswordHasher.HashedPassword hashed = PasswordHasher.hash(password);
        accounts.save(new AccountRecord(
                user.getId(),
                cleanEmail,
                hashed.hash(),
                hashed.salt(),
                false,
                0,
                null,
                null,
                LocalDateTime.now()
        ));

        log.info("Registered new {} account for {}", role, cleanEmail);
        return user;
    }

    /**
     * Verifies credentials and returns the persisted domain user. The role is
     * taken from the stored account, not from a UI selector, so a user cannot
     * "sign in as admin" by choosing the admin option.
     */
    public User login(String email, String password) throws AuthException {
        String cleanEmail = normalize(email);
        if (cleanEmail.isEmpty() || password == null || password.isEmpty()) {
            throw new AuthException(AuthException.Reason.VALIDATION, "Enter your email and password.");
        }

        AccountRecord account = accounts.findByEmail(cleanEmail)
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_CREDENTIALS,
                        "Incorrect email or password."));

        LocalDateTime now = LocalDateTime.now();
        if (account.lockedUntil() != null && account.lockedUntil().isAfter(now)) {
            throw new AuthException(AuthException.Reason.LOCKED, lockMessage(account.lockedUntil(), now));
        }
        // A lock that has elapsed restarts the attempt counter.
        if (account.lockedUntil() != null) {
            account = account.withFailedAttempt(0, null);
        }

        if (!PasswordHasher.verify(password, account.passwordHash(), account.passwordSalt())) {
            int attempts = account.failedAttempts() + 1;
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                LocalDateTime lockedUntil = now.plus(LOCKOUT_DURATION);
                accounts.save(account.withFailedAttempt(attempts, lockedUntil));
                log.warn("Account {} locked after {} failed sign-in attempts", cleanEmail, attempts);
                throw new AuthException(AuthException.Reason.LOCKED, lockMessage(lockedUntil, now));
            }
            accounts.save(account.withFailedAttempt(attempts, null));
            int remaining = MAX_FAILED_ATTEMPTS - attempts;
            throw new AuthException(AuthException.Reason.INVALID_CREDENTIALS,
                    "Incorrect email or password. " + remaining + " attempt"
                            + (remaining == 1 ? "" : "s") + " left before a temporary lock.");
        }

        User user = users.findByEmail(cleanEmail)
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_CREDENTIALS,
                        "This account is incomplete. Please contact the administrator."));

        if (!user.isActive()) {
            throw new AuthException(AuthException.Reason.ACCOUNT_DISABLED,
                    "This account has been disabled. Contact the administrator.");
        }

        if (user.getRole() == UserRole.ADMIN && !adminPolicy.isProvisionedAdmin(account, user)) {
            log.warn("Blocked administrator sign-in for a non-allowlisted account: {}", cleanEmail);
            throw new AuthException(AuthException.Reason.ADMIN_RESTRICTED,
                    "This account is not authorised for administrator access.");
        }

        accounts.save(account.withLoginSuccess(now));
        log.info("Signed in {} as {}", cleanEmail, user.getRole());
        return user;
    }

    private static void validatePassword(String password, String confirmPassword) throws AuthException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new AuthException(AuthException.Reason.VALIDATION,
                    "Use a password with at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (!password.chars().anyMatch(Character::isLetter)
                || !password.chars().anyMatch(Character::isDigit)) {
            throw new AuthException(AuthException.Reason.VALIDATION,
                    "Use at least one letter and one number in your password.");
        }
        if (!password.equals(confirmPassword)) {
            throw new AuthException(AuthException.Reason.VALIDATION, "The two passwords do not match.");
        }
    }

    private static String lockMessage(LocalDateTime lockedUntil, LocalDateTime now) {
        long minutes = Math.max(1, Duration.between(now, lockedUntil).toMinutes() + 1);
        return "Too many failed attempts. Try again in about " + minutes + " minute"
                + (minutes == 1 ? "" : "s") + ".";
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
