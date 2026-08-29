package com.waslataam.viewmodel;

import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.UUID;

public class DonationListViewModel {
    private final ObservableList<DonationRow> donations = FXCollections.observableArrayList();
    private final StringProperty filterType = new SimpleStringProperty("");
    private final StringProperty filterStatus = new SimpleStringProperty("");
    private final BooleanProperty showUrgentOnly = new SimpleBooleanProperty(false);

    public ObservableList<DonationRow> getDonations() {
        return donations;
    }

    public void setDonations(java.util.List<Donation> donationList) {
        donations.clear();
        for (Donation d : donationList) {
            donations.add(new DonationRow(d));
        }
    }

    public void addDonation(Donation donation) {
        donations.add(new DonationRow(donation));
    }

    public void removeDonation(UUID donationId) {
        donations.removeIf(row -> row.getId().equals(donationId));
    }

    public void updateDonation(Donation donation) {
        for (int i = 0; i < donations.size(); i++) {
            if (donations.get(i).getId().equals(donation.getId())) {
                donations.set(i, new DonationRow(donation));
                return;
            }
        }
        // If not found, add it
        donations.add(new DonationRow(donation));
    }

    public StringProperty filterTypeProperty() {
        return filterType;
    }

    public String getFilterType() {
        return filterType.get();
    }

    public void setFilterType(String value) {
        filterType.set(value);
    }

    public StringProperty filterStatusProperty() {
        return filterStatus;
    }

    public String getFilterStatus() {
        return filterStatus.get();
    }

    public void setFilterStatus(String value) {
        filterStatus.set(value);
    }

    public BooleanProperty showUrgentOnlyProperty() {
        return showUrgentOnly;
    }

    public boolean isShowUrgentOnly() {
        return showUrgentOnly.get();
    }

    public void setShowUrgentOnly(boolean value) {
        showUrgentOnly.set(value);
    }

    public static class DonationRow {
        private final UUID id;
        private final String type;
        private final DoubleProperty quantity = new SimpleDoubleProperty();
        private final String unit;
        private final StringProperty status = new SimpleStringProperty();
        private final StringProperty donorName = new SimpleStringProperty();
        private final ObjectProperty<LocalDateTime> expiryTime = new SimpleObjectProperty<>();
        private final BooleanProperty urgent = new SimpleBooleanProperty();
        private final StringProperty location = new SimpleStringProperty();

        public DonationRow(Donation donation) {
            this.id = donation.getId();
            this.type = donation.getType().getDisplayName();
            this.quantity.set(donation.getQuantity());
            this.unit = donation.getUnit();
            this.status.set(donation.getStatus().getDisplayName());
            this.expiryTime.set(donation.getExpiryTime());
            this.urgent.set(donation.isUrgent());
            this.location.set(donation.getLocation() != null ? donation.getLocation().getAddress() : "Unknown");
        }

        public UUID getId() { return id; }
        public String getType() { return type; }
        public DoubleProperty quantityProperty() { return quantity; }
        public double getQuantity() { return quantity.get(); }
        public String getUnit() { return unit; }
        public StringProperty statusProperty() { return status; }
        public String getStatus() { return status.get(); }
        public StringProperty donorNameProperty() { return donorName; }
        public String getDonorName() { return donorName.get(); }
        public void setDonorName(String name) { donorName.set(name); }
        public ObjectProperty<LocalDateTime> expiryTimeProperty() { return expiryTime; }
        public LocalDateTime getExpiryTime() { return expiryTime.get(); }
        public BooleanProperty urgentProperty() { return urgent; }
        public boolean isUrgent() { return urgent.get(); }
        public StringProperty locationProperty() { return location; }
        public String getLocation() { return location.get(); }
    }
}