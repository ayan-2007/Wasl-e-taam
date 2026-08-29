package com.waslataam.observer;

import com.waslataam.command.AuditTrail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogger implements SystemObserver {
    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);
    private final AuditTrail auditTrail;

    public AuditLogger(AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    @Override
    public void onDonationPosted(SystemEvent event) {
        auditTrail.log(event.getActorId(), event.getActorRole(), "POST_DONATION",
                event.getDonation().getId(), null, event.getCurrentStatus());
        log.debug("AUDIT: Donation posted - {}", event.getDonation().getId());
    }

    @Override
    public void onDonationClaimed(SystemEvent event) {
        auditTrail.log(event.getActorId(), event.getActorRole(), "CLAIM_DONATION",
                event.getDonation().getId(), event.getPreviousStatus(), event.getCurrentStatus());
        log.debug("AUDIT: Donation claimed - {}", event.getDonation().getId());
    }

    @Override
    public void onDonationCancelled(SystemEvent event) {
        auditTrail.log(event.getActorId(), event.getActorRole(), "CANCEL_CLAIM",
                event.getDonation().getId(), event.getPreviousStatus(), event.getCurrentStatus());
        log.debug("AUDIT: Donation claim cancelled - {}", event.getDonation().getId());
    }

    @Override
    public void onDonationPickedUp(SystemEvent event) {
        auditTrail.log(event.getActorId(), event.getActorRole(), "PICKUP_DONATION",
                event.getDonation().getId(), event.getPreviousStatus(), event.getCurrentStatus());
        log.debug("AUDIT: Donation picked up - {}", event.getDonation().getId());
    }

    @Override
    public void onDonationDelivered(SystemEvent event) {
        auditTrail.log(event.getActorId(), event.getActorRole(), "DELIVER_DONATION",
                event.getDonation().getId(), event.getPreviousStatus(), event.getCurrentStatus());
        log.debug("AUDIT: Donation delivered - {}", event.getDonation().getId());
    }

    @Override
    public void onDonationExpired(SystemEvent event) {
        auditTrail.log(event.getActorId(), "SYSTEM", "EXPIRE_DONATION",
                event.getDonation().getId(), event.getPreviousStatus(), event.getCurrentStatus());
        log.debug("AUDIT: Donation expired - {}", event.getDonation().getId());
    }

    @Override
    public void onDonationRejected(SystemEvent event) {
        auditTrail.log(event.getActorId(), event.getActorRole(), "REJECT_DONATION",
                event.getDonation().getId(), event.getPreviousStatus(), event.getCurrentStatus());
        log.debug("AUDIT: Donation rejected - {}", event.getDonation().getId());
    }
}