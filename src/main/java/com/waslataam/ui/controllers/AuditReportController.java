package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.command.AuditTrail;
import com.waslataam.command.DonationCommand;
import com.waslataam.domain.DonationStatus;
import com.waslataam.viewmodel.AuditLogViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private DonationCoordinator coordinator;

    public void setCoordinator(DonationCoordinator coordinator) {
        this.coordinator = coordinator;
        initialize();
        loadAuditLog();
    }

    @FXML
    public void initialize() {
        if (coordinator == null) {
            coordinator = com.waslataam.Main.getCoordinator();
        }
        auditTable.setItems(viewModel.getEntries());
        loadAuditLog();
        
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        actorColumn.setCellValueFactory(new PropertyValueFactory<>("actorId"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("actorRole"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        donationColumn.setCellValueFactory(new PropertyValueFactory<>("donationId"));
        prevStatusColumn.setCellValueFactory(new PropertyValueFactory<>("previousStatus"));
        newStatusColumn.setCellValueFactory(new PropertyValueFactory<>("newStatus"));

        actionFilterComboBox.getItems().addAll(
                "All Actions", "POST_DONATION", "CLAIM_DONATION", "CANCEL_CLAIM",
                "PICKUP_DONATION", "DELIVER_DONATION", "EXPIRE_DONATION", "REJECT_DONATION"
        );
        actionFilterComboBox.setValue("All Actions");

        // Enable undo button if there are commands to undo
        updateUndoButton();
    }

    private void loadAuditLog() {
        if (coordinator == null) return;

        List<AuditTrail.AuditEntry> entries = coordinator.getAuditTrail().getEntries();
        viewModel.setEntries(entries);
        updateUndoButton();
    }

    @FXML
    private void handleFilterChange() {
        // For now, reload all - in a full implementation, filter the viewModel
        loadAuditLog();
    }

    @FXML
    private void handleUndo() {
        if (coordinator == null) return;

        try {
            coordinator.undoLastAction();
            showSuccess("Last action undone successfully!");
            loadAuditLog();
        } catch (Exception e) {
            showError("Failed to undo: " + e.getMessage());
        }
    }

    private void updateUndoButton() {
        if (coordinator != null) {
            undoButton.setDisable(!coordinator.getCommandInvoker().canUndo());
        } else {
            undoButton.setDisable(true);
        }
    }

    @FXML
    private void handleBack() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navigateTo("/com/waslataam/ui/resources/fxml/AdminDashboard.fxml", "adminDashboard");
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