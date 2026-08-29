package com.waslataam.factory;

import com.waslataam.domain.DonationType;
import com.waslataam.domain.Location;
import java.time.LocalDateTime;
import java.util.UUID;

public class DonationParams {
    private final DonationType type;
    private final double quantity;
    private final String unit;
    private final UUID donorId;
    private final Location location;
    private final LocalDateTime expiryTime;
    private final String temperatureRequirement;
    private final int shelfLifeDays;
    private final int servings;
    private final LocalDateTime cookedAt;

    private DonationParams(Builder builder) {
        this.type = builder.type;
        this.quantity = builder.quantity;
        this.unit = builder.unit;
        this.donorId = builder.donorId;
        this.location = builder.location;
        this.expiryTime = builder.expiryTime;
        this.temperatureRequirement = builder.temperatureRequirement;
        this.shelfLifeDays = builder.shelfLifeDays;
        this.servings = builder.servings;
        this.cookedAt = builder.cookedAt;
    }

    public DonationType getType() { return type; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public UUID getDonorId() { return donorId; }
    public Location getLocation() { return location; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public String getTemperatureRequirement() { return temperatureRequirement; }
    public int getShelfLifeDays() { return shelfLifeDays; }
    public int getServings() { return servings; }
    public LocalDateTime getCookedAt() { return cookedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DonationType type;
        private double quantity;
        private String unit;
        private UUID donorId;
        private Location location;
        private LocalDateTime expiryTime;
        private String temperatureRequirement;
        private int shelfLifeDays;
        private int servings;
        private LocalDateTime cookedAt;

        public Builder type(DonationType type) { this.type = type; return this; }
        public Builder quantity(double quantity) { this.quantity = quantity; return this; }
        public Builder unit(String unit) { this.unit = unit; return this; }
        public Builder donorId(UUID donorId) { this.donorId = donorId; return this; }
        public Builder location(Location location) { this.location = location; return this; }
        public Builder expiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; return this; }
        public Builder temperatureRequirement(String temperatureRequirement) { this.temperatureRequirement = temperatureRequirement; return this; }
        public Builder shelfLifeDays(int shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; return this; }
        public Builder servings(int servings) { this.servings = servings; return this; }
        public Builder cookedAt(LocalDateTime cookedAt) { this.cookedAt = cookedAt; return this; }

        public DonationParams build() {
            if (type == null) throw new IllegalStateException("Donation type is required");
            if (quantity <= 0) throw new IllegalStateException("Quantity must be positive");
            if (unit == null || unit.isBlank()) throw new IllegalStateException("Unit is required");
            if (donorId == null) throw new IllegalStateException("Donor ID is required");
            if (location == null) throw new IllegalStateException("Location is required");
            return new DonationParams(this);
        }
    }
}