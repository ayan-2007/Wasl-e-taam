package com.waslataam.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * SQLite-backed {@link AccountRepository}.
 */
public class SqliteAccountRepository implements AccountRepository {

    private final Database database;

    public SqliteAccountRepository(Database database) {
        this.database = database;
    }

    @Override
    public void save(AccountRecord account) {
        String sql = """
                INSERT INTO accounts
                    (email, user_id, password_hash, password_salt, is_admin,
                     failed_attempts, locked_until, last_login_at, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(email) DO UPDATE SET
                    user_id = excluded.user_id,
                    password_hash = excluded.password_hash,
                    password_salt = excluded.password_salt,
                    is_admin = excluded.is_admin,
                    failed_attempts = excluded.failed_attempts,
                    locked_until = excluded.locked_until,
                    last_login_at = excluded.last_login_at
                """;
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalize(account.email()));
            statement.setString(2, account.userId().toString());
            statement.setString(3, account.passwordHash());
            statement.setString(4, account.passwordSalt());
            statement.setInt(5, account.admin() ? 1 : 0);
            statement.setInt(6, account.failedAttempts());
            statement.setString(7, text(account.lockedUntil()));
            statement.setString(8, text(account.lastLoginAt()));
            statement.setString(9, text(account.createdAt()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save the account for " + account.email(), e);
        }
    }

    @Override
    public Optional<AccountRecord> findByEmail(String email) {
        String sql = "SELECT * FROM accounts WHERE email = ? COLLATE NOCASE";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalize(email));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new AccountRecord(
                        UUID.fromString(resultSet.getString("user_id")),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("password_salt"),
                        resultSet.getInt("is_admin") == 1,
                        resultSet.getInt("failed_attempts"),
                        time(resultSet.getString("locked_until")),
                        time(resultSet.getString("last_login_at")),
                        time(resultSet.getString("created_at"))
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load the account for " + email, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM accounts WHERE email = ? COLLATE NOCASE LIMIT 1";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalize(email));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not check the account for " + email, e);
        }
    }

    @Override
    public void allowlistAdmin(String email, String note) {
        String sql = "INSERT INTO admin_allowlist (email, note) VALUES (?, ?) "
                + "ON CONFLICT(email) DO UPDATE SET note = excluded.note";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalize(email));
            statement.setString(2, note);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update the administrator allowlist", e);
        }
    }

    @Override
    public boolean isAdminAllowlisted(String email) {
        String sql = "SELECT 1 FROM admin_allowlist WHERE email = ? COLLATE NOCASE LIMIT 1";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalize(email));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read the administrator allowlist", e);
        }
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String text(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private static LocalDateTime time(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
