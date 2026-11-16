package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.viewmodel.SessionContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label lastUpdatedLabel;
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
        if (coordinator == null) {
            coordinator = com.waslataam.Main.getCoordinator();
        }
        setupActivityTable();
        refresh();
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

        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Updated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void updateActivityTable() {
        var auditEntries = coordinator.getAuditTrail().getEntries();
        var items = FXCollections.<ActivityRow>observableArrayList();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        
        for (int i = Math.max(0, auditEntries.size() - 20); i < auditEntries.size(); i++) {
            var entry = auditEntries.get(i);
            String actor = entry.actorId() != null
                    ? entry.actorRole() + " (" + entry.actorId().toString().substring(0, 8) + ")"
                    : entry.actorRole();
            items.add(new ActivityRow(
                    entry.timestamp().format(formatter),
                    entry.action(),
                    actor
            ));
        }
        
        recentActivityTable.setItems(items);
    }

    @FXML
    private void handleStrategySwitcher() {
        navigateTo("/com/waslataam/ui/resources/fxml/StrategySwitcherView.fxml", "strategySwitcher");
    }

    @FXML
    private void handleAuditReport() {
        navigateTo("/com/waslataam/ui/resources/fxml/AuditReportView.fxml", "auditReport");
    }

    @FXML
    private void handleImpactDashboard() {
        navigateTo("/com/waslataam/ui/resources/fxml/ImpactDashboardView.fxml", "impactDashboard");
    }

    private void navigateTo(String fxmlPath, String cacheKey) {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navigateTo(fxmlPath, cacheKey);
        }
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