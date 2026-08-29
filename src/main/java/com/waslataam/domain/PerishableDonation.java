package com.waslataam.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class PerishableDonation extends Donation {
    private final LocalDateTime expiryTime;
    private final String temperatureRequirement;

    public PerishableDonation(UUID id, double quantity, String unit, UUID donorId,
                              LocalDateTime expiryTime, String temperatureRequirement) {
        super(id, DonationType.PERISHABLE, quantity, unit, donorId);
        if (expiryTime == null || expiryTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiry time must be in the future");
        }
        this.expiryTime = expiryTime;
        this.temperatureRequirement = temperatureRequirement;
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