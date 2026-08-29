package com.waslataam.factory;

import com.waslataam.domain.Location;
import com.waslataam.domain.UserRole;
import com.waslataam.domain.VehicleType;
import com.waslataam.domain.AdminPermission;
import java.util.Set;
import java.util.UUID;

public class UserParams {
    private final UserRole role;
    private final String name;
    private final String email;
    private final Location location;
    private final String organizationName;
    private final int capacityKg;
    private final double reliabilityScore;
    private final VehicleType vehicleType;
    private final Set<AdminPermission> permissions;

    private UserParams(Builder builder) {
        this.role = builder.role;
        this.name = builder.name;
        this.email = builder.email;
        this.location = builder.location;
        this.organizationName = builder.organizationName;
        this.capacityKg = builder.capacityKg;
        this.reliabilityScore = builder.reliabilityScore;
        this.vehicleType = builder.vehicleType;
        this.permissions = builder.permissions;
    }

    public UserRole getRole() { return role; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Location getLocation() { return location; }
    public String getOrganizationName() { return organizationName; }
    public int getCapacityKg() { return capacityKg; }
    public double getReliabilityScore() { return reliabilityScore; }
    public VehicleType getVehicleType() { return vehicleType; }
    public Set<AdminPermission> getPermissions() { return permissions; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserRole role;
        private String name;
        private String email;
        private Location location;
        private String organizationName;
        private int capacityKg;
        private double reliabilityScore;
        private VehicleType vehicleType;
        private Set<AdminPermission> permissions;

        public Builder role(UserRole role) { this.role = role; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder location(Location location) { this.location = location; return this; }
        public Builder organizationName(String organizationName) { this.organizationName = organizationName; return this; }
        public Builder capacityKg(int capacityKg) { this.capacityKg = capacityKg; return this; }
        public Builder reliabilityScore(double reliabilityScore) { this.reliabilityScore = reliabilityScore; return this; }
        public Builder vehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; return this; }
        public Builder permissions(Set<AdminPermission> permissions) { this.permissions = permissions; return this; }

        public UserParams build() {
            if (role == null) throw new IllegalStateException("User role is required");
            if (name == null || name.isBlank()) throw new IllegalStateException("Name is required");
            if (email == null || email.isBlank()) throw new IllegalStateException("Email is required");
            if (location == null) throw new IllegalStateException("Location is required");
            return new UserParams(this);
        }
    }
}