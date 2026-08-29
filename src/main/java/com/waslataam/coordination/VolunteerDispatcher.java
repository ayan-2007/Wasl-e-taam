package com.waslataam.coordination;

import com.waslataam.domain.Donation;
import com.waslataam.domain.User;
import com.waslataam.domain.Volunteer;
import com.waslataam.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VolunteerDispatcher {
    private static final Logger log = LoggerFactory.getLogger(VolunteerDispatcher.class);
    private final UserRepository userRepository;

    public VolunteerDispatcher(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<Volunteer> findBestVolunteer(Donation donation) {
        List<Volunteer> availableVolunteers = userRepository.findByRole(com.waslataam.domain.UserRole.VOLUNTEER)
                .stream()
                .map(v -> (Volunteer) v)
                .filter(Volunteer::isAvailable)
                .filter(v -> v.getVehicleCapacityKg() >= donation.getQuantity())
                .toList();

        if (availableVolunteers.isEmpty()) {
            return Optional.empty();
        }

        // Sort by distance to donation pickup location
        return availableVolunteers.stream()
                .min(Comparator.comparingDouble(v ->
                        v.getLocation().distanceTo(donation.getLocation())));
    }

    public void assignVolunteer(UUID donationId, UUID volunteerId) {
        userRepository.findById(volunteerId).ifPresent(user -> {
            if (user instanceof Volunteer volunteer) {
                volunteer.assignPickup(donationId);
                userRepository.update(volunteer);
                log.info("Assigned volunteer {} to donation {}", volunteerId, donationId);
            }
        });
    }
}