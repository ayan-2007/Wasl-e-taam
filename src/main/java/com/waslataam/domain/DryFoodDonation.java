package com.waslataam.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class DryFoodDonation extends Donation {
    private final int shelfLifeDays;
    private final LocalDateTime expiryTime;

    public DryFoodDonation(UUID id, double quantity, String unit, UUID donorId, int shelfLifeDays, Location location) {
        super(id, DonationType.DRY_FOOD, quantity, unit, donorId, location);
        if (shelfLifeDays <= 0) {
            throw new IllegalArgumentException("Shelf life must be positive");
        }
        this.shelfLifeDays = shelfLifeDays;
        this.expiryTime = LocalDateTime.now().plusDays(shelfLifeDays);
    }

    public DryFoodDonation(UUID id, double quantity, String unit, UUID donorId, int shelfLifeDays) {
        this(id, quantity, unit, donorId, shelfLifeDays, null);
    }

    @Override
    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public int getShelfLifeDays() {
        return shelfLifeDays;
    }

    @Override
    public String toString() {
        return "DryFoodDonation{" +
                "shelfLifeDays=" + shelfLifeDays +
                ", expiryTime=" + expiryTime +
                "} " + super.toString();
    }
}