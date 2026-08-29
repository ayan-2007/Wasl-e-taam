package com.waslataam.factory;

import com.waslataam.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

public class DonationFactory {
    private static final Logger log = LoggerFactory.getLogger(DonationFactory.class);

    public Donation create(DonationParams params) {
        log.debug("Creating donation of type: {}", params.getType());
        return switch (params.getType()) {
            case PERISHABLE -> createPerishable(params);
            case DRY_FOOD -> createDryFood(params);
            case PREPARED_MEAL -> createPreparedMeal(params);
        };
    }

    private PerishableDonation createPerishable(DonationParams params) {
        if (params.getExpiryTime() == null) {
            throw new IllegalArgumentException("Perishable donations require an expiry time");
        }
        if (params.getTemperatureRequirement() == null || params.getTemperatureRequirement().isBlank()) {
            throw new IllegalArgumentException("Perishable donations require a temperature requirement");
        }
        return new PerishableDonation(
                UUID.randomUUID(),
                params.getQuantity(),
                params.getUnit(),
                params.getDonorId(),
                params.getExpiryTime(),
                params.getTemperatureRequirement()
        );
    }

    private DryFoodDonation createDryFood(DonationParams params) {
        if (params.getShelfLifeDays() <= 0) {
            throw new IllegalArgumentException("Dry food donations require a positive shelf life in days");
        }
        return new DryFoodDonation(
                UUID.randomUUID(),
                params.getQuantity(),
                params.getUnit(),
                params.getDonorId(),
                params.getShelfLifeDays()
        );
    }

    private PreparedMealDonation createPreparedMeal(DonationParams params) {
        if (params.getServings() <= 0) {
            throw new IllegalArgumentException("Prepared meal donations require positive servings");
        }
        if (params.getCookedAt() == null) {
            throw new IllegalArgumentException("Prepared meal donations require a cooked-at time");
        }
        return new PreparedMealDonation(
                UUID.randomUUID(),
                params.getQuantity(),
                params.getUnit(),
                params.getDonorId(),
                params.getServings(),
                params.getCookedAt()
        );
    }
}