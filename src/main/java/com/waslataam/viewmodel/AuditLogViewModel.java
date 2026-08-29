package com.waslataam.viewmodel;

import com.waslataam.command.AuditTrail;
import com.waslataam.domain.DonationStatus;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AuditLogViewModel {
    private final ObservableList<AuditEntryRow> entries = FXCollections.observableArrayList();
    private final ObjectProperty<LocalDateTime> filterStartDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> filterEndDate = new SimpleObjectProperty<>();
    private final StringProperty filterActor = new SimpleStringProperty("");
    private final StringProperty filterAction = new SimpleStringProperty("");

    public ObservableList<AuditEntryRow> getEntries() {
        return entries;
    }

    public void setEntries(List<AuditTrail.AuditEntry> auditEntries) {
        entries.clear();
        for (AuditTrail.AuditEntry e : auditEntries) {
            entries.add(new AuditEntryRow(e));
        }
    }

    public void addEntry(AuditTrail.AuditEntry entry) {
        entries.add(0, new AuditEntryRow(entry)); // Add to top
    }

    public ObjectProperty<LocalDateTime> filterStartDateProperty() {
        return filterStartDate;
    }

    public LocalDateTime getFilterStartDate() {
        return filterStartDate.get();
    }

    public void setFilterStartDate(LocalDateTime value) {
        filterStartDate.set(value);
    }

    public ObjectProperty<LocalDateTime> filterEndDateProperty() {
        return filterEndDate;
    }

    public LocalDateTime getFilterEndDate() {
        return filterEndDate.get();
    }

    public void setFilterEndDate(LocalDateTime value) {
        filterEndDate.set(value);
    }

    public StringProperty filterActorProperty() {
        return filterActor;
    }

    public String getFilterActor() {
        return filterActor.get();
    }

    public void setFilterActor(String value) {
        filterActor.set(value);
    }

    public StringProperty filterActionProperty() {
        return filterAction;
    }

    public String getFilterAction() {
        return filterAction.get();
    }

    public void setFilterAction(String value) {
        filterAction.set(value);
    }

    public static class AuditEntryRow {
        private final LocalDateTime timestamp;
        private final String actorId;
        private final String actorRole;
        private final String action;
        private final UUID donationId;
        private final String previousStatus;
        private final String newStatus;

        public AuditEntryRow(AuditTrail.AuditEntry entry) {
            this.timestamp = entry.timestamp();
            this.actorId = entry.actorId().toString().substring(0, 8);
            this.actorRole = entry.actorRole();
            this.action = entry.action();
            this.donationId = entry.donationId();
            this.previousStatus = entry.previousStatus() != null ? entry.previousStatus().getDisplayName() : "N/A";
            this.newStatus = entry.newStatus() != null ? entry.newStatus().getDisplayName() : "N/A";
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public String getActorId() { return actorId; }
        public String getActorRole() { return actorRole; }
        public String getAction() { return action; }
        public UUID getDonationId() { return donationId; }
        public String getPreviousStatus() { return previousStatus; }
        public String getNewStatus() { return newStatus; }
    }
}