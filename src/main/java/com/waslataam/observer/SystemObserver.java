package com.waslataam.observer;

public interface SystemObserver {
    void onDonationPosted(SystemEvent event);

    void onDonationClaimed(SystemEvent event);

    void onDonationCancelled(SystemEvent event);

    void onDonationPickedUp(SystemEvent event);

    void onDonationDelivered(SystemEvent event);

    void onDonationExpired(SystemEvent event);

    void onDonationRejected(SystemEvent event);
}