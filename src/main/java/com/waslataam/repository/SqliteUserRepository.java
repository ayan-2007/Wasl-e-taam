package com.waslataam.repository;

import com.waslataam.domain.Admin;
import com.waslataam.domain.AdminPermission;
import com.waslataam.domain.Donor;
import com.waslataam.domain.Location;
import com.waslataam.domain.Recipient;
import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;
import com.waslataam.domain.VehicleType;
import com.waslataam.domain.Volunteer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SQLite-backed {@link UserRepository}. Users survive application restarts,
 * unlike the in-memory implementation used by the unit tests.
 */
public class SqliteUserRepository implements UserRepository {

    private static final String COLUMNS = """
            id, email, name, role, location_lat, location_lng, location_address,
            location_zone, organization_name, capacity_kg, current_load_kg,
            reliability_score, vehicle_type, admin_permissions, active, registered_at
            """;

    private final Database database;

    public SqliteUserRepository(Database database) {
        this.database = database;
    }

    @Override
    public void save(User user) {
        String sql = """
                INSERT INTO users (%s)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    email = excluded.email,
                    name = excluded.name,
                    role = excluded.role,
                    location_lat = excluded.location_lat,
                    location_lng = excluded.location_lng,
                    location_address = excluded.location_address,
                    location_zone = excluded.location_zone,
                    organization_name = excluded.organization_name,
                    capacity_kg = excluded.capacity_kg,
                    current_load_kg = excluded.current_load_kg,
                    reliability_score = excluded.reliability_score,
                    vehicle_type = excluded.vehicle_type,
                    admin_permissions = excluded.admin_permissions,
                    active = excluded.active
                """.formatted(COLUMNS);
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, user);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save user " + user.getEmail(), e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        return findOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", id.toString());
    }

    @Override
    public List<User> findByRole(UserRole role) {
        List<User> users = new ArrayList<>();
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + COLUMNS + " FROM users WHERE role = ? ORDER BY name")) {
            statement.setString(1, role.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not list users with role " + role, e);
        }
        return users;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + COLUMNS + " FROM users ORDER BY name");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(map(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not list users", e);
        }
        return users;
    }

    @Override
    public void delete(UUID id) {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not delete user " + id, e);
        }
    }

    @Override
    public void update(User user) {
        save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findOne("SELECT " + COLUMNS + " FROM users WHERE email = ? COLLATE NOCASE", email);
    }

    private Optional<User> findOne(String sql, String parameter) {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load user for " + parameter, e);
        }
    }

    private static void bind(PreparedStatement statement, User user) throws SQLException {
        Location location = user.getLocation();
        statement.setString(1, user.getId().toString());
        statement.setString(2, user.getEmail());
        statement.setString(3, user.getName());
        statement.setString(4, user.getRole().name());
        statement.setDouble(5, location != null ? location.getLatitude() : 0);
        statement.setDouble(6, location != null ? location.getLongitude() : 0);
        statement.setString(7, location != null ? location.getAddress() : "");
        statement.setString(8, location != null ? location.getProximityZone() : "");
        statement.setString(9, user instanceof Donor donor ? donor.getOrganizationName() : null);
        statement.setInt(10, user instanceof Recipient recipient ? recipient.getCapacityKg() : 0);
        statement.setInt(11, user instanceof Recipient recipient ? recipient.getCurrentLoadKg() : 0);
        statement.setDouble(12, user instanceof Recipient recipient ? recipient.getReliabilityScore() : 0);
        statement.setString(13, user instanceof Volunteer volunteer ? volunteer.getVehicleType().name() : null);
        statement.setString(14, user instanceof Admin admin ? serializePermissions(admin.getPermissions()) : null);
        statement.setInt(15, user.isActive() ? 1 : 0);
        statement.setString(16, user.getRegisteredAt().toString());
    }

    private static User map(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        String name = resultSet.getString("name");
        String email = resultSet.getString("email");
        UserRole role = UserRole.valueOf(resultSet.getString("role"));
        Location location = new Location(
                resultSet.getDouble("location_lat"),
                resultSet.getDouble("location_lng"),
                resultSet.getString("location_address"),
                resultSet.getString("location_zone")
        );

        User user = switch (role) {
            case DONOR -> new Donor(id, name, email, location,
                    resultSet.getString("organization_name"));
            case RECIPIENT -> {
                Recipient recipient = new Recipient(
                        id,
                        name,
                        email,
                        location,
                        Math.max(1, resultSet.getInt("capacity_kg")),
                        clampReliability(resultSet.getDouble("reliability_score"))
                );
                recipient.increaseLoad(resultSet.getInt("current_load_kg"));
                yield recipient;
            }
            case VOLUNTEER -> new Volunteer(id, name, email, location,
                    VehicleType.valueOf(resultSet.getString("vehicle_type")));
            case ADMIN -> new Admin(id, name, email, location,
                    parsePermissions(resultSet.getString("admin_permissions")));
        };

        user.setActive(resultSet.getInt("active") == 1);
        return user;
    }

    private static double clampReliability(double value) {
        if (value < 0.0 || value > 1.0) {
            return 0.5;
        }
        return value;
    }

    private static String serializePermissions(Set<AdminPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "";
        }
        return permissions.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(","));
    }

    private static Set<AdminPermission> parsePermissions(String csv) {
        EnumSet<AdminPermission> permissions = EnumSet.noneOf(AdminPermission.class);
        if (csv == null || csv.isBlank()) {
            return permissions;
        }
        for (String token : csv.split(",")) {
            String name = token.trim();
            if (name.isEmpty()) {
                continue;
            }
            try {
                permissions.add(AdminPermission.valueOf(name));
            } catch (IllegalArgumentException ignored) {
                // Unknown permission names are skipped instead of blocking login.
            }
        }
        return permissions;
    }
}
