package com.waslataam.factory;

import com.waslataam.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UserFactory {
    private static final Logger log = LoggerFactory.getLogger(UserFactory.class);

    public User create(UserParams params) {
        log.debug("Creating user of role: {}", params.getRole());
        return switch (params.getRole()) {
            case DONOR -> createDonor(params);
            case RECIPIENT -> createRecipient(params);
            case VOLUNTEER -> createVolunteer(params);
            case ADMIN -> createAdmin(params);
        };
    }

    private Donor createDonor(UserParams params) {
        if (params.getOrganizationName() == null || params.getOrganizationName().isBlank()) {
            throw new IllegalArgumentException("Donor requires an organization name");
        }
        return new Donor(
                UUID.randomUUID(),
                params.getName(),
                params.getEmail(),
                params.getLocation(),
                params.getOrganizationName()
        );
    }

    private Recipient createRecipient(UserParams params) {
        if (params.getCapacityKg() <= 0) {
            throw new IllegalArgumentException("Recipient requires a positive capacity");
        }
        if (params.getReliabilityScore() < 0.0 || params.getReliabilityScore() > 1.0) {
            throw new IllegalArgumentException("Reliability score must be between 0.0 and 1.0");
        }
        return new Recipient(
                UUID.randomUUID(),
                params.getName(),
                params.getEmail(),
                params.getLocation(),
                params.getCapacityKg(),
                params.getReliabilityScore()
        );
    }

    private Volunteer createVolunteer(UserParams params) {
        if (params.getVehicleType() == null) {
            throw new IllegalArgumentException("Volunteer requires a vehicle type");
        }
        return new Volunteer(
                UUID.randomUUID(),
                params.getName(),
                params.getEmail(),
                params.getLocation(),
                params.getVehicleType()
        );
    }

    private Admin createAdmin(UserParams params) {
        return new Admin(
                UUID.randomUUID(),
                params.getName(),
                params.getEmail(),
                params.getLocation(),
                params.getPermissions()
        );
    }
}