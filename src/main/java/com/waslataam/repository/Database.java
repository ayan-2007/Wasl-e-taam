package com.waslataam.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the embedded SQLite database used for registered users, credentials and
 * the administrator allowlist.
 *
 * <p>The database file defaults to {@code data/wasl-e-taam.db} in the working
 * directory. Override it with {@code -Dwasl.db.path=/some/path.db} (the test
 * suite points it at {@code target/test-data}).</p>
 *
 * <p>A connection is opened per operation and closed by the caller. SQLite is
 * fast enough for a desktop app of this size, and it avoids sharing a mutable
 * JDBC handle between the JavaFX thread and the expiry watcher.</p>
 */
public final class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);

    public static final String DB_PATH_PROPERTY = "wasl.db.path";

    private final Path file;
    private final String url;

    public Database(Path file) {
        this.file = file.toAbsolutePath().normalize();
        this.url = "jdbc:sqlite:" + this.file;
        try {
            // Explicitly loading the driver also gives a clear error when the
            // sqlite-jdbc dependency is missing from a shaded build.
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("SQLite JDBC driver is not on the classpath", e);
        }
    }

    public static Database defaultDatabase() {
        String override = System.getProperty(DB_PATH_PROPERTY);
        Path path = (override != null && !override.isBlank())
                ? Path.of(override)
                : Path.of("data", "wasl-e-taam.db");
        return new Database(path);
    }

    public Path getFile() {
        return file;
    }

    public void initialize() {
        Path parent = file.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create database directory: " + parent, e);
            }
        }

        try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id                  TEXT PRIMARY KEY,
                        email               TEXT NOT NULL UNIQUE COLLATE NOCASE,
                        name                TEXT NOT NULL,
                        role                TEXT NOT NULL,
                        location_lat        REAL NOT NULL,
                        location_lng        REAL NOT NULL,
                        location_address    TEXT NOT NULL,
                        location_zone       TEXT NOT NULL,
                        organization_name   TEXT,
                        capacity_kg         INTEGER,
                        current_load_kg     INTEGER,
                        reliability_score   REAL,
                        vehicle_type        TEXT,
                        admin_permissions   TEXT,
                        active              INTEGER NOT NULL DEFAULT 1,
                        registered_at       TEXT NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        email           TEXT PRIMARY KEY COLLATE NOCASE,
                        user_id         TEXT NOT NULL UNIQUE,
                        password_hash   TEXT NOT NULL,
                        password_salt   TEXT NOT NULL,
                        is_admin        INTEGER NOT NULL DEFAULT 0,
                        failed_attempts INTEGER NOT NULL DEFAULT 0,
                        locked_until    TEXT,
                        last_login_at   TEXT,
                        created_at      TEXT NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS admin_allowlist (
                        email TEXT PRIMARY KEY COLLATE NOCASE,
                        note  TEXT
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS app_metadata (
                        key   TEXT PRIMARY KEY,
                        value TEXT
                    )
                    """);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialise the database at " + file, e);
        }

        log.info("SQLite database ready at {}", file);
    }

    public Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }
}
