package com.waslataam.command;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;
import com.waslataam.repository.DonationRepository;

import java.util.UUID;

public class MarkDeliveredCommand implements DonationCommand {
    private final DonationRepository donationRepository;
    private final UUID donationId;
    private final UUID volunteerId;
    private DonationStatus previousStatus;

    public MarkDeliveredCommand(DonationRepository donationRepository, UUID donationId, UUID volunteerId) {
        this.donationRepository = donationRepository;
        this.donationId = donationId;
        this.volunteerId = volunteerId;
    }

    @Override
    public void execute() throws IllegalStateTransitionException {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateTransitionException("Donation not found: " + donationId));
        previousStatus = donation.getStatus();
        donation.deliver();
        donationRepository.update(donation);
    }

    @Override
    public void undo() throws IllegalStateTransitionException {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateTransitionException("Donation not found: " + donationId));
        if (previousStatus == DonationStatus.IN_TRANSIT) {
            // Revert to in transit state
            donation.setState(new com.waslataam.state.InTransitState());
            donationRepository.update(donation);
        }
    }

    @Override
    public String describe() {
        return "Marked donation " + donationId + " as delivered by volunteer " + volunteerId;
    }

    @Override
    public UUID getDonationId() {
        return donationId;
    }

    @Override
    public UUID getActorId() {
        return volunteerId;
    }
}