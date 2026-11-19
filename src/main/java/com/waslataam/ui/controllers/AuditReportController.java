package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.command.AuditTrail;
import com.waslataam.domain.AdminPermission;
import com.waslataam.security.AdminGuardrails;
import com.waslataam.viewmodel.AuditLogViewModel;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class AuditReportController {
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TextField actorFilterField;
    @FXML private ComboBox<String> actionFilterComboBox;
    @FXML private Button undoButton;
    @FXML private TableView<AuditLogViewModel.AuditEntryRow> auditTable;
    @FXML private TableColumn<AuditLogViewModel.AuditEntryRow, String> timestampColumn;
    @FXML private TableColumn<AuditLogViewModel.AuditEntryRow, String> actorColumn;
    @FXML private TableColumn<AuditLogViewModel.AuditEntryRow, String> roleColumn;
    @FXML private TableColumn<AuditLogViewModel.AuditEntryRow, String> actionColumn;
    @FXML private TableColumn<AuditLogViewModel.AuditEntryRow, String> donationColumn;
    @FXML private TableColumn<AuditLogViewModel.AuditEntryRow, String> prevStatusColumn;
    @FXML private TableColumn<AuditLogViewModel.AuditEntryRow, String> newStatusColumn;

    private final AuditLogViewModel viewModel = new AuditLogViewModel();
    private final SessionContext sessionContext = SessionContext.getInstance();
    private DonationCoordinator coordinator;

    public void setCoordinator(DonationCoordinator coordinator) {
        this.coordinator = coordinator;
        loadAuditLog();
    }

    @FXML
    public void initialize() {
        if (coordinator == null) {
            coordinator = com.waslataam.Main.getCoordinator();
        }

        auditTable.setItems(viewModel.getEntries());

        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        actorColumn.setCellValueFactory(new PropertyValueFactory<>("actorId"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("actorRole"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        donationColumn.setCellValueFactory(new PropertyValueFactory<>("donationId"));
        prevStatusColumn.setCellValueFactory(new PropertyValueFactory<>("previousStatus"));
        newStatusColumn.setCellValueFactory(new PropertyValueFactory<>("newStatus"));

        actionFilterComboBox.getItems().setAll(
                "All Actions", "POST_DONATION", "CLAIM_DONATION", "CANCEL_CLAIM",
                "PICKUP_DONATION", "DELIVER_DONATION", "EXPIRE_DONATION", "REJECT_DONATION"
        );
        actionFilterComboBox.setValue("All Actions");

        loadAuditLog();
    }

    @FXML
    private void handleFilterChange() {
        loadAuditLog();
    }

    private void loadAuditLog() {
        if (coordinator == null) {
            return;
        }

        LocalDate from = fromDatePicker != null ? fromDatePicker.getValue() : null;
        LocalDate to = toDatePicker != null ? toDatePicker.getValue() : null;
        String actor = actorFilterField != null && actorFilterField.getText() != null
                ? actorFilterField.getText().trim().toLowerCase(Locale.ROOT)
                : "";
        String action = actionFilterComboBox != null ? actionFilterComboBox.getValue() : null;

        List<AuditTrail.AuditEntry> entries = coordinator.getAuditTrail().getEntries().stream()
                .filter(entry -> from == null || !entry.timestamp().toLocalDate().isBefore(from))
                .filter(entry -> to == null || !entry.timestamp().toLocalDate().isAfter(to))
                .filter(entry -> actor.isEmpty() || matchesActor(entry, actor))
                .filter(entry -> action == null || action.isBlank() || "All Actions".equals(action)
                        || action.equals(entry.action()))
                .toList();

        viewModel.setEntries(entries);
        updateUndoButton();
    }

    private static boolean matchesActor(AuditTrail.AuditEntry entry, String actor) {
        String id = entry.actorId() == null ? "" : entry.actorId().toString().toLowerCase(Locale.ROOT);
        String role = entry.actorRole() == null ? "" : entry.actorRole().toLowerCase(Locale.ROOT);
        return id.startsWith(actor) || id.contains(actor) || role.contains(actor);
    }

    @FXML
    private void handleUndo() {
        if (coordinator == null) {
            return;
        }
        if (!AdminGuardrails.hasPermission(sessionContext.getCurrentUser(), AdminPermission.UNDO_ACTIONS)) {
            showError("Access denied. Your administrator account does not have the Undo Actions permission.");
            return;
        }

        try {
            coordinator.undoLastAction();
            showSuccess("Last action undone successfully!");
            loadAuditLog();
        } catch (Exception e) {
            showError("Failed to undo: " + e.getMessage());
        }
    }

    private void updateUndoButton() {
        boolean permitted = AdminGuardrails.hasPermission(
                sessionContext.getCurrentUser(), AdminPermission.UNDO_ACTIONS);
        boolean canUndo = coordinator != null && coordinator.getCommandInvoker().canUndo();

        if (undoButton != null) {
            undoButton.setDisable(!permitted || !canUndo);
            undoButton.setTooltip(new Tooltip(!permitted
                    ? "Requires the Undo Actions permission"
                    : canUndo ? "Undo the most recent donation action" : "No recorded action to undo"));
        }
    }

    @FXML
    private void handleBack() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navigateTo(
                    "/com/waslataam/ui/resources/fxml/AdminDashboard.fxml", "adminDashboard");
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
