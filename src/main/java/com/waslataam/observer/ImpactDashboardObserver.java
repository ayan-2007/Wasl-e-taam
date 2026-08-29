package com.waslataam.observer;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.viewmodel.DashboardViewModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpactDashboardObserver implements SystemObserver {
    private static final Logger log = LoggerFactory.getLogger(ImpactDashboardObserver.class);
    private final DashboardViewModel dashboardViewModel;

    public ImpactDashboardObserver(DashboardViewModel dashboardViewModel) {
        this.dashboardViewModel = dashboardViewModel;
    }

    @Override
    public void onDonationPosted(SystemEvent event) {
        Platform.runLater(() -> {
            dashboardViewModel.setActiveDonations(dashboardViewModel.getActiveDonations() + 1);
        });
    }

    @Override
    public void onDonationClaimed(SystemEvent event) {
        // Active count stays same, just status changes
    }

    @Override
    public void onDonationCancelled(SystemEvent event) {
        Platform.runLater(() -> {
            dashboardViewModel.setActiveDonations(dashboardViewModel.getActiveDonations() - 1);
        });
    }

    @Override
    public void onDonationPickedUp(SystemEvent event) {
        // Active count stays same
    }

    @Override
    public void onDonationDelivered(SystemEvent event) {
        Platform.runLater(() -> {
            Donation donation = event.getDonation();
            int servings = 0;
            if (donation instanceof com.waslataam.domain.PreparedMealDonation pmd) {
                servings = pmd.getServings();
            }
            dashboardViewModel.updateFromDelivery(donation.getQuantity(), servings);
            dashboardViewModel.setActiveDonations(dashboardViewModel.getActiveDonations() - 1);
        });
    }

    @Override
    public void onDonationExpired(SystemEvent event) {
        Platform.runLater(() -> {
            dashboardViewModel.setActiveDonations(dashboardViewModel.getActiveDonations() - 1);
        });
    }

    @Override
    public void onDonationRejected(SystemEvent event) {
        Platform.runLater(() -> {
            dashboardViewModel.setActiveDonations(dashboardViewModel.getActiveDonations() - 1);
        });
    }
}