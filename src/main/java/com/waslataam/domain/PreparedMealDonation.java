package com.waslataam.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class PreparedMealDonation extends Donation {
    private final int servings;
    private final LocalDateTime cookedAt;
    private final LocalDateTime expiryTime;

    public PreparedMealDonation(UUID id, double quantity, String unit, UUID donorId,
                                int servings, LocalDateTime cookedAt) {
        super(id, DonationType.PREPARED_MEAL, quantity, unit, donorId);
        if (servings <= 0) {
            throw new IllegalArgumentException("Servings must be positive");
        }
        if (cookedAt == null || cookedAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cooked time must be in the past or now");
        }
        this.servings = servings;
        this.cookedAt = cookedAt;
        // Prepared meals expire in 4 hours from cooking
        this.expiryTime = cookedAt.plusHours(4);
    }

    @Override
    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public int getServings() {
        return servings;
    }

    public LocalDateTime getCookedAt() {
        return cookedAt;
    }

    @Override
    public String toString() {
        return "PreparedMealDonation{" +
                "servings=" + servings +
                ", cookedAt=" + cookedAt +
                ", expiryTime=" + expiryTime +
                "} " + super.toString();
    }
}