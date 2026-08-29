package com.waslataam.coordination;

import com.waslataam.chain.VerificationHandler;
import com.waslataam.chain.VerificationResult;
import com.waslataam.command.AuditTrail;
import com.waslataam.command.CommandInvoker;
import com.waslataam.domain.*;
import com.waslataam.domain.exceptions.IllegalStateTransitionException;
import com.waslataam.factory.DonationFactory;
import com.waslataam.factory.DonationParams;
import com.waslataam.observer.*;
import com.waslataam.repository.DonationRepository;
import com.waslataam.repository.UserRepository;
import com.waslataam.strategy.MatchingEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DonationCoordinator {
    private static final Logger log = LoggerFactory.getLogger(DonationCoordinator.class);

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final DonationFactory donationFactory;
    private final VerificationHandler verificationChain;
    private final CommandInvoker commandInvoker;
    private final AuditTrail auditTrail;
    private final MatchingEngine matchingEngine;
    private final List<SystemObserver> observers = new ArrayList<>();

    public DonationCoordinator(DonationRepository donationRepository,
                               UserRepository userRepository,
                               DonationFactory donationFactory,
                               VerificationHandler verificationChain,
                               CommandInvoker commandInvoker,
                               AuditTrail auditTrail,
                               MatchingEngine matchingEngine) {
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
        this.donationFactory = donationFactory;
        this.verificationChain = verificationChain;
        this.commandInvoker = commandInvoker;
        this.auditTrail = auditTrail;
        this.matchingEngine = matchingEngine;
    }

    public void addObserver(SystemObserver observer) {
        observers.add(observer);
    }

    public Donation postDonation(DonationParams params) {
        log.info("Posting new donation for donor: {}", params.getDonorId());

        // Create donation via factory
        Donation donation = donationFactory.create(params);
        donation.setLocation(params.getLocation());

        // Run through verification chain
        VerificationResult result = verificationChain.handle(donation);
        if (!result.isPassed()) {
            log.warn("Donation verification failed: {}", result.getReason());
            donation.setState(new com.waslataam.state.RejectedState());
            donationRepository.save(donation);
            notifyObservers(new SystemEvent(SystemEvent.Type.DONATION_REJECTED, donation,
                    params.getDonorId(), "DONOR", DonationStatus.PENDING_REVIEW));
            throw new IllegalStateTransitionException("Donation rejected: " + result.getReason());
        }

        // Passed verification - set to AVAILABLE
        donation.setState(new com.waslataam.state.AvailableState());
        donationRepository.save(donation);

        // Add to donor's history
        userRepository.findById(params.getDonorId()).ifPresent(user -> {
            if (user instanceof Donor donor) {
                donor.addDonation(donation.getId());
                userRepository.update(donor);
            }
        });

        // Notify observers
        notifyObservers(new SystemEvent(SystemEvent.Type.DONATION_POSTED, donation,
                params.getDonorId(), "DONOR", DonationStatus.PENDING_REVIEW));

        // Auto-match to recipient
        matchingEngine.findBestMatch(donation).ifPresent(recipient -> {
            log.info("Auto-matched donation {} to recipient {}", donation.getId(), recipient.getId());
            try {
                ClaimDonationCommand claimCmd = new ClaimDonationCommand(donationRepository, donation.getId(), recipient.getId());
                commandInvoker.execute(claimCmd);
                notifyObservers(new SystemEvent(SystemEvent.Type.DONATION_CLAIMED, donation,
                        recipient.getId(), "RECIPIENT", DonationStatus.AVAILABLE));
            } catch (IllegalStateTransitionException e) {
                log.error("Auto-match claim failed", e);
            }
        });

        return donation;
    }

    public void claimDonation(UUID donationId, UUID recipientId) throws IllegalStateTransitionException {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateTransitionException("Donation not found"));

        DonationStatus previousStatus = donation.getStatus();
        ClaimDonationCommand cmd = new ClaimDonationCommand(donationRepository, donationId, recipientId);
        commandInvoker.execute(cmd);

        notifyObservers(new SystemEvent(SystemEvent.Type.DONATION_CLAIMED, donation,
                recipientId, "RECIPIENT", previousStatus));
    }

    public void cancelClaim(UUID donationId, UUID recipientId) throws IllegalStateTransitionException {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateTransitionException("Donation not found"));

        DonationStatus previousStatus = donation.getStatus();
        CancelClaimCommand cmd = new CancelClaimCommand(donationRepository, donationId, recipientId);
        commandInvoker.execute(cmd);

        notifyObservers(new SystemEvent(SystemEvent.Type.DONATION_CANCELLED, donation,
                recipientId, "RECIPIENT", previousStatus));
    }

    public void pickupDonation(UUID donationId, UUID volunteerId) throws IllegalStateTransitionException {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateTransitionException("Donation not found"));

        DonationStatus previousStatus = donation.getStatus();
        MarkPickedUpCommand cmd = new MarkPickedUpCommand(donationRepository, donationId, volunteerId);
        commandInvoker.execute(cmd);

        // Update volunteer assignment
        userRepository.findById(volunteerId).ifPresent(user -> {
            if (user instanceof Volunteer volunteer) {
                volunteer.assignPickup(donationId);
                userRepository.update(volunteer);
            }
        });

        notifyObservers(new SystemEvent(SystemEvent.Type.DONATION_PICKED_UP, donation,
                volunteerId, "VOLUNTEER", previousStatus));
    }

    public void deliverDonation(UUID donationId, UUID volunteerId) throws IllegalStateTransitionException {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateTransitionException("Donation not found"));

        DonationStatus previousStatus = donation.getStatus();
        MarkDeliveredCommand cmd = new MarkDeliveredCommand(donationRepository, donationId, volunteerId);
        commandInvoker.execute(cmd);

        // Update volunteer and recipient
        userRepository.findById(volunteerId).ifPresent(user -> {
            if (user instanceof Volunteer volunteer) {
                volunteer.completePickup(donationId);
                userRepository.update(volunteer);
            }
        });

        if (donation.getRecipientId() != null) {
            userRepository.findById(donation.getRecipientId()).ifPresent(user -> {
                if (user instanceof Recipient recipient) {
                    recipient.completeDonation(donationId, (int) donation.getQuantity());
                    userRepository.update(recipient);
                }
            });
        }

        notifyObservers(new SystemEvent(SystemEvent.Type.DONATION_DELIVERED, donation,
                volunteerId, "VOLUNTEER", previousStatus));
    }

    public void undoLastAction() throws IllegalStateTransitionException {
        if (commandInvoker.canUndo()) {
            DonationCommand lastCommand = commandInvoker.peekLast();
            commandInvoker.undo();
            log.info("Undid last action: {}", lastCommand.describe());

            // Note: In a full implementation, we'd need to notify observers of the undo
            // For now, the state change is handled by the command's undo()
        }
    }

    private void notifyObservers(SystemEvent event) {
        for (SystemObserver observer : observers) {
            try {
                switch (event.getType()) {
                    case DONATION_POSTED -> observer.onDonationPosted(event);
                    case DONATION_CLAIMED -> observer.onDonationClaimed(event);
                    case DONATION_CANCELLED -> observer.onDonationCancelled(event);
                    case DONATION_PICKED_UP -> observer.onDonationPickedUp(event);
                    case DONATION_DELIVERED -> observer.onDonationDelivered(event);
                    case DONATION_EXPIRED -> observer.onDonationExpired(event);
                    case DONATION_REJECTED -> observer.onDonationRejected(event);
                }
            } catch (Exception e) {
                log.error("Error notifying observer: {}", observer.getClass().getSimpleName(), e);
            }
        }
    }

    public DonationRepository getDonationRepository() {
        return donationRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public MatchingEngine getMatchingEngine() {
        return matchingEngine;
    }

    public CommandInvoker getCommandInvoker() {
        return commandInvoker;
    }

    public AuditTrail getAuditTrail() {
        return auditTrail;
    }
}