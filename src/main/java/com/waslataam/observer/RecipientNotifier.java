package com.waslataam.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipientNotifier implements SystemObserver {
    private static final Logger log = LoggerFactory.getLogger(RecipientNotifier.class);

    @Override
    public void onDonationPosted(SystemEvent event) {
        log.info("NOTIFICATION: New donation posted - {} (ID: {})", event.getDonation().getType(), event.getDonation().getId());
        // In a real system, this would send push notifications, emails, etc.
    }

    @Override
    public void onDonationClaimed(SystemEvent event) {
        log.info("NOTIFICATION: Donation claimed - {} by recipient {}", event.getDonation().getId(), event.getActorId());
    }

    @Override
    public void onDonationCancelled(SystemEvent event) {
        log.info("NOTIFICATION: Donation claim cancelled - {}", event.getDonation().getId());
    }

    @Override
    public void onDonationPickedUp(SystemEvent event) {
        log.info("NOTIFICATION: Donation picked up - {} by volunteer {}", event.getDonation().getId(), event.getActorId());
    }

    @Override
    public void onDonationDelivered(SystemEvent event) {
        log.info("NOTIFICATION: Donation delivered - {} to recipient {}", event.getDonation().getId(), event.getDonation().getRecipientId());
    }

    @Override
    public void onDonationExpired(SystemEvent event) {
        log.info("NOTIFICATION: Donation expired - {}", event.getDonation().getId());
    }

    @Override
    public void onDonationRejected(SystemEvent event) {
        log.info("NOTIFICATION: Donation rejected - {}", event.getDonation().getId());
    }
}