package com.waslataam.auth;

import com.waslataam.domain.Admin;
import com.waslataam.domain.AdminPermission;
import com.waslataam.domain.Location;
import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;
import com.waslataam.factory.UserFactory;
import com.waslataam.factory.UserParams;
import com.waslataam.repository.AccountRecord;
import com.waslataam.repository.Database;
import com.waslataam.repository.SqliteAccountRepository;
import com.waslataam.repository.SqliteUserRepository;
import com.waslataam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private static final Location LOCATION =
            new Location(24.8607, 67.0011, "Karachi, Pakistan", "Karachi");

    @TempDir
    Path tempDir;

    private AuthService authService;
    private SqliteAccountRepository accounts;
    private UserRepository users;
    private UserFactory userFactory;

    @BeforeEach
    void setUp() {
        Database database = new Database(tempDir.resolve("auth-test.db"));
        database.initialize();
        accounts = new SqliteAccountRepository(database);
        users = new SqliteUserRepository(database);
        userFactory = new UserFactory();
        AdminPolicy adminPolicy = new AdminPolicy(accounts);
        authService = new AuthService(accounts, users, userFactory, adminPolicy);
    }

    @Test
    void registersThenSignsIn() throws Exception {
        User registered = authService.register(
                "Test Donor", "Donor@Example.com", "demo1234", "demo1234",
                UserRole.DONOR, "Test Kitchen");

        assertEquals(UserRole.DONOR, registered.getRole());
        assertTrue(accounts.findByEmail("donor@example.com").isPresent());

        User signedIn = authService.login("donor@example.com", "demo1234");
        assertEquals(registered.getId(), signedIn.getId());
    }

    @Test
    void rejectsDuplicateEmailRegardlessOfCase() throws Exception {
        authService.register("First", "duplicate@example.com", "demo1234", "demo1234",
                UserRole.RECIPIENT, null);

        AuthException error = assertThrows(AuthException.class, () ->
                authService.register("Second", "DUPLICATE@example.com", "demo1234", "demo1234",
                        UserRole.VOLUNTEER, null));
        assertEquals(AuthException.Reason.EMAIL_TAKEN, error.getReason());
    }

    @Test
    void publicSignupCannotCreateAnAdministrator() {
        AuthException error = assertThrows(AuthException.class, () ->
                authService.register("Sneaky Admin", "sneaky@example.com", "demo1234", "demo1234",
                        UserRole.ADMIN, null));
        assertEquals(AuthException.Reason.ADMIN_RESTRICTED, error.getReason());
        assertTrue(accounts.findByEmail("sneaky@example.com").isEmpty());
    }

    @Test
    void locksAccountAfterFiveFailedAttempts() throws Exception {
        authService.register("Locked User", "locked@example.com", "demo1234", "demo1234",
                UserRole.DONOR, null);

        for (int attempt = 1; attempt < 5; attempt++) {
            AuthException wrong = assertThrows(AuthException.class, () ->
                    authService.login("locked@example.com", "wrong-pass1"));
            assertEquals(AuthException.Reason.INVALID_CREDENTIALS, wrong.getReason());
        }

        AuthException fifth = assertThrows(AuthException.class, () ->
                authService.login("locked@example.com", "wrong-pass1"));
        assertEquals(AuthException.Reason.LOCKED, fifth.getReason());

        // Even the correct password is refused while the lock is active.
        AuthException locked = assertThrows(AuthException.class, () ->
                authService.login("locked@example.com", "demo1234"));
        assertEquals(AuthException.Reason.LOCKED, locked.getReason());
    }

    @Test
    void administratorSignInRequiresTheAllowlist() {
        Admin candidate = createAdmin("allowed-admin@example.com", false);
        assertTrue(accounts.findByEmail(candidate.getEmail()).isPresent());

        AuthException blocked = assertThrows(AuthException.class, () ->
                authService.login(candidate.getEmail(), "demo1234"));
        assertEquals(AuthException.Reason.ADMIN_RESTRICTED, blocked.getReason());

        accounts.allowlistAdmin(candidate.getEmail(), "test");
        assertDoesNotThrowLogin(candidate.getEmail());

        createAdmin("unlisted-admin@example.com", false);
        AuthException unlisted = assertThrows(AuthException.class, () ->
                authService.login("unlisted-admin@example.com", "demo1234"));
        assertEquals(AuthException.Reason.ADMIN_RESTRICTED, unlisted.getReason());
    }

    private Admin createAdmin(String email, boolean allowlist) {
        User user = userFactory.create(UserParams.builder()
                .role(UserRole.ADMIN)
                .name("Test Admin")
                .email(email)
                .location(LOCATION)
                .permissions(EnumSet.allOf(AdminPermission.class))
                .build());
        users.save(user);

        PasswordHasher.HashedPassword hashed = PasswordHasher.hash("demo1234");
        accounts.save(new AccountRecord(
                user.getId(), email, hashed.hash(), hashed.salt(), true,
                0, null, null, LocalDateTime.now()));
        if (allowlist) {
            accounts.allowlistAdmin(email, "test");
        }
        return (Admin) user;
    }

    private void assertDoesNotThrowLogin(String email) {
        try {
            User user = authService.login(email, "demo1234");
            assertTrue(user instanceof Admin);
        } catch (AuthException e) {
            throw new AssertionError("Expected administrator sign-in to succeed", e);
        }
    }
}
