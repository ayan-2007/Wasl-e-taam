package com.waslataam.repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Login credentials plus lockout bookkeeping for one user.
 *
 * <p>Credentials live in their own table so that {@link com.waslataam.domain.User}
 * stays a pure domain object with no password knowledge.</p>
 */
public record AccountRecord(
        UUID userId,
        String email,
        String passwordHash,
        String passwordSalt,
        boolean admin,
        int failedAttempts,
        LocalDateTime lockedUntil,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    public AccountRecord withFailedAttempt(int attempts, LocalDateTime lockUntil) {
        return new AccountRecord(userId, email, passwordHash, passwordSalt, admin,
                attempts, lockUntil, lastLoginAt, createdAt);
    }

    public AccountRecord withLoginSuccess(LocalDateTime at) {
        return new AccountRecord(userId, email, passwordHash, passwordSalt, admin,
                0, null, at, createdAt);
    }
}
