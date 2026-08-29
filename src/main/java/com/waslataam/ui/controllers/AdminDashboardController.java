package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label totalDonationsValue;
    @FXML private Label activeDonationsValue;
    @FXML private Label deliveredValue;
    @FXML private Label expiredValue;
    @FXML private TableView<ActivityRow> recentActivityTable;

    private final SessionContext sessionContext = SessionContext.getInstance();
    private DonationCoordinator coordinator;

    public void setCoordinator(DonationCoordinator coordinator) {
        this.coordinator = coordinator;
        refresh();
    }

    @FXML
    public void initialize() {
        setupActivityTable();
    }

    private void setupActivityTable() {
        recentActivityTable.getColumns().clear();
        
        TableColumn<ActivityRow, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timeCol.setPrefWidth(120);
        
        TableColumn<ActivityRow, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionCol.setPrefWidth(200);
        
        TableColumn<ActivityRow, String> actorCol = new TableColumn<>("Actor");
        actorCol.setCellValueFactory(new PropertyValueFactory<>("actor"));
        actorCol.setPrefWidth(150);
        
        recentActivityTable.getColumns().addAll(timeCol, actionCol, actorCol);
    }

    @FXML
    private void handleRefresh() {
        refresh();
    }

    private void refresh() {
        if (coordinator == null) return;

        List<Donation> allDonations = coordinator.getDonationRepository().findAll();
        
        totalDonationsValue.setText(String.valueOf(allDonations.size()));
        
        long active = allDonations.stream()
                .filter(d -> d.getStatus().isActive())
                .count();
        activeDonationsValue.setText(String.valueOf(active));
        
        long delivered = allDonations.stream()
                .filter(d -> d.getStatus() == DonationStatus.DELIVERED)
                .count();
        deliveredValue.setText(String.valueOf(delivered));
        
        long expired = allDonations.stream()
                .filter(d -> d.getStatus() == DonationStatus.EXPIRED)
                .count();
        expiredValue.setText(String.valueOf(expired));

        // Update recent activity from audit trail
        updateActivityTable();
    }

    private void updateActivityTable() {
        var auditEntries = coordinator.getAuditTrail().getEntries();
        var items = FXCollections.observableArrayList();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        
        for (int i = Math.max(0, auditEntries.size() - 20); i < auditEntries.size(); i++) {
            var entry = auditEntries.get(i);
            items.add(new ActivityRow(
                    entry.timestamp().format(formatter),
                    entry.action(),
                    entry.actorRole() + " (" + entry.actorId().toString().substring(0, 8) + ")"
            ));
        }
        
        recentActivityTable.setItems(items);
    }

    @FXML
    private void handleStrategySwitcher() {
        // Navigation handled by MainShellController
    }

    @FXML
    private void handleAuditReport() {
        // Navigation handled by MainShellController
    }

    @FXML
    private void handleImpactDashboard() {
        // Navigation handled by MainShellController
    }

    public static class ActivityRow {
        private final String timestamp;
        private final String action;
        private final String actor;

        public ActivityRow(String timestamp, String action, String actor) {
            this.timestamp = timestamp;
            this.action = action;
            this.actor = actor;
        }

        public String getTimestamp() { return timestamp; }
        public String getAction() { return action; }
        public String getActor() { return actor; }
    }
}