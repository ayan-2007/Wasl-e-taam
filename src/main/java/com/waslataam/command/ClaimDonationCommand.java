package com.waslataam.command;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;
import com.waslataam.repository.DonationRepository;

import java.util.UUID;

public class ClaimDonationCommand implements DonationCommand {
    private final DonationRepository donationRepository;
    private final UUID donationId;
    private final UUID recipientId;
    private DonationStatus previousStatus;

    public ClaimDonationCommand(DonationRepository donationRepository, UUID donationId, UUID recipientId) {
        this.donationRepository = donationRepository;
        this.donationId = donationId;
        this.recipientId = recipientId;
    }

    @Override
    public void execute() throws IllegalStateTransitionException {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateTransitionException("Donation not found: " + donationId));
        previousStatus = donation.getStatus();
        donation.claim(recipientId);
        donationRepository.update(donation);
    }

    @Override
    public void undo() throws IllegalStateTransitionException {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateTransitionException("Donation not found: " + donationId));
        if (previousStatus == DonationStatus.AVAILABLE) {
            donation.cancelClaim();
            donationRepository.update(donation);
        }
    }

    @Override
    public String describe() {
        return "Claimed donation " + donationId + " by recipient " + recipientId;
    }

    @Override
    public UUID getDonationId() {
        return donationId;
    }

    @Override
    public UUID getActorId() {
        return recipientId;
    }
}