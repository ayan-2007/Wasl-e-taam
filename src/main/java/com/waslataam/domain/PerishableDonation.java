package com.waslataam.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class PerishableDonation extends Donation {
    private final LocalDateTime expiryTime;
    private final String temperatureRequirement;

    public PerishableDonation(UUID id, double quantity, String unit, UUID donorId,
                              LocalDateTime expiryTime, String temperatureRequirement, Location location) {
        super(id, DonationType.PERISHABLE, quantity, unit, donorId, location);
        if (expiryTime == null) {
            throw new IllegalArgumentException("Expiry time cannot be null");
        }
        this.expiryTime = expiryTime;
        this.temperatureRequirement = temperatureRequirement;
    }

    public PerishableDonation(UUID id, double quantity, String unit, UUID donorId,
                              LocalDateTime expiryTime, String temperatureRequirement) {
        this(id, quantity, unit, donorId, expiryTime, temperatureRequirement, null);
    }

    @Override
    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public String getTemperatureRequirement() {
        return temperatureRequirement;
    }

    @Override
    public String toString() {
        return "PerishableDonation{" +
                "expiryTime=" + expiryTime +
                ", temperatureRequirement='" + temperatureRequirement + '\'' +
                "} " + super.toString();
    }
}