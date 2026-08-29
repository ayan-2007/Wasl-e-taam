package com.waslataam.observer;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.repository.DonationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiryWatcher implements SystemObserver {
    private static final Logger log = LoggerFactory.getLogger(ExpiryWatcher.class);
    private final DonationRepository donationRepository;
    private final ScheduledExecutorService scheduler;
    private final long checkIntervalSeconds;
    private volatile boolean running = false;

    public ExpiryWatcher(DonationRepository donationRepository, long checkIntervalSeconds) {
        this.donationRepository = donationRepository;
        this.checkIntervalSeconds = checkIntervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ExpiryWatcher");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (running) return;
        running = true;
        scheduler.scheduleAtFixedRate(this::checkExpiries, checkIntervalSeconds, checkIntervalSeconds, TimeUnit.SECONDS);
        log.info("ExpiryWatcher started with interval {} seconds", checkIntervalSeconds);
    }

    public void stop() {
        running = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("ExpiryWatcher stopped");
    }

    private void checkExpiries() {
        try {
            List<Donation> activeDonations = donationRepository.findByStatus(DonationStatus.AVAILABLE);
            activeDonations.addAll(donationRepository.findByStatus(DonationStatus.CLAIMED));
            activeDonations.addAll(donationRepository.findByStatus(DonationStatus.IN_TRANSIT));

            LocalDateTime now = LocalDateTime.now();
            for (Donation donation : activeDonations) {
                if (donation.isExpired()) {
                    log.info("ExpiryWatcher: Donation {} has expired, transitioning to EXPIRED", donation.getId());
                    donation.expire();
                    donationRepository.update(donation);
                    // Notify observers through coordinator - this would be called from coordinator
                } else if (donation.isUrgent()) {
                    log.debug("ExpiryWatcher: Donation {} is now URGENT (expires within 2 hours)", donation.getId());
                    // Could escalate priority here
                }
            }
        } catch (Exception e) {
            log.error("Error in ExpiryWatcher check", e);
        }
    }

    // SystemObserver methods - no-op for ExpiryWatcher as it's a background watcher
    @Override
    public void onDonationPosted(SystemEvent event) {}

    @Override
    public void onDonationClaimed(SystemEvent event) {}

    @Override
    public void onDonationCancelled(SystemEvent event) {}

    @Override
    public void onDonationPickedUp(SystemEvent event) {}

    @Override
    public void onDonationDelivered(SystemEvent event) {}

    @Override
    public void onDonationExpired(SystemEvent event) {}

    @Override
    public void onDonationRejected(SystemEvent event) {}
}