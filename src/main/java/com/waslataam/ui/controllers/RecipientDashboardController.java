package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.Recipient;
import com.waslataam.viewmodel.DonationListViewModel;
import com.waslataam.viewmodel.SessionContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.List;
import java.util.UUID;

public class RecipientDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label availableCountLabel;
    @FXML private Label claimedCountLabel;
    @FXML private ComboBox<String> typeFilterComboBox;
    @FXML private CheckBox urgentCheckBox;
    @FXML private TableView<DonationListViewModel.DonationRow> donationsTable;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> typeColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, Number> quantityColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> expiryColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> locationColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> donorColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, Void> actionColumn;

    @FXML private TableView<DonationListViewModel.DonationRow> claimedTable;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> cTypeColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, Number> cQuantityColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> cStatusColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> cExpiryColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, Void> cActionColumn;

    private final DonationListViewModel availableViewModel = new DonationListViewModel();
    private final DonationListViewModel claimedViewModel = new DonationListViewModel();
    private final SessionContext sessionContext = SessionContext.getInstance();
    private DonationCoordinator coordinator;

    public void setCoordinator(DonationCoordinator coordinator) {
        this.coordinator = coordinator;
        loadAvailableDonations();
        loadClaimedDonations();
    }

    @FXML
    public void initialize() {
        if (coordinator == null) {
            coordinator = com.waslataam.Main.getCoordinator();
        }
        setupAvailableTable();
        if (claimedTable != null) {
            setupClaimedTable();
        }
        setupFilters();
        loadAvailableDonations();
        loadClaimedDonations();
    }

    private void setupAvailableTable() {
        donationsTable.setItems(availableViewModel.getDonations());
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        expiryColumn.setCellValueFactory(cellData -> cellData.getValue().expiryTimeProperty().asString());
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        donorColumn.setCellValueFactory(new PropertyValueFactory<>("donorName"));

        actionColumn.setCellFactory(createActionButtonFactory("Claim", this::handleClaim));
    }

    private void setupClaimedTable() {
        claimedTable.setItems(claimedViewModel.getDonations());
        cTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        cQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        cExpiryColumn.setCellValueFactory(cellData -> cellData.getValue().expiryTimeProperty().asString());

        cActionColumn.setCellFactory(createActionButtonFactory("Cancel", this::handleCancelClaim));
    }

    private void setupFilters() {
        typeFilterComboBox.getItems().addAll("All Types", "Perishable Food", "Dry Food", "Prepared Meal");
        typeFilterComboBox.setValue("All Types");
    }

    private void loadAvailableDonations() {
        if (coordinator == null) return;

        String typeFilter = typeFilterComboBox.getValue();
        boolean urgentOnly = urgentCheckBox.isSelected();

        List<Donation> donations = coordinator.getDonationRepository().findByStatus(DonationStatus.AVAILABLE)
                .stream()
                .filter(d -> typeFilter == null || "All Types".equals(typeFilter)
                        || d.getType().getDisplayName().equals(typeFilter))
                .filter(d -> !urgentOnly || d.isUrgent())
                .sorted(java.util.Comparator.comparing(Donation::isUrgent).reversed()
                        .thenComparing(Donation::getExpiryTime))
                .toList();
        availableViewModel.setDonations(donations);

        // Resolve donor names by the donation's donor id (same order as the rows).
        var rows = availableViewModel.getDonations();
        for (int i = 0; i < donations.size(); i++) {
            Donation donation = donations.get(i);
            var row = rows.get(i);
            coordinator.getUserRepository().findById(donation.getDonorId())
                    .ifPresent(u -> row.setDonorName(u.getName()));
        }

        if (availableCountLabel != null) {
            availableCountLabel.setText(donations.size() + (donations.size() == 1 ? " listing" : " listings"));
        }
    }

    private void loadClaimedDonations() {
        if (coordinator == null || claimedTable == null) return;

        var user = sessionContext.getCurrentUser();
        if (user == null) return;

        List<Donation> donations = coordinator.getDonationRepository().findByRecipientId(user.getId());
        claimedViewModel.setDonations(donations);
        if (claimedCountLabel != null) {
            claimedCountLabel.setText(donations.size() + (donations.size() == 1 ? " claim" : " claims"));
        }
    }

    @FXML
    private void handleFilterChange() {
        loadAvailableDonations();
    }

    @FXML
    private void handleClearFilters() {
        typeFilterComboBox.setValue("All Types");
        urgentCheckBox.setSelected(false);
        loadAvailableDonations();
    }

    private void handleClaim(DonationListViewModel.DonationRow row) {
        try {
            // Go through the coordinator so the audit trail, observers and
            // recipient histories stay in step with the state machine.
            coordinator.claimDonation(row.getId(), sessionContext.getCurrentUser().getId());

            showSuccess("Donation claimed successfully!");
            loadAvailableDonations();
            loadClaimedDonations();
        } catch (Exception e) {
            showError("Failed to claim donation: " + e.getMessage());
        }
    }

    private void handleCancelClaim(DonationListViewModel.DonationRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Claim");
        confirm.setHeaderText("Cancel your claim on this donation?");
        confirm.setContentText("This will make the donation available to other recipients.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    coordinator.cancelClaim(row.getId(), sessionContext.getCurrentUser().getId());

                    showSuccess("Claim cancelled.");
                    loadAvailableDonations();
                    loadClaimedDonations();
                } catch (Exception e) {
                    showError("Failed to cancel claim: " + e.getMessage());
                }
            }
        });
    }

    private Callback<TableColumn<DonationListViewModel.DonationRow, Void>, TableCell<DonationListViewModel.DonationRow, Void>> createActionButtonFactory(String buttonText, java.util.function.Consumer<DonationListViewModel.DonationRow> action) {
        return col -> new TableCell<>() {
            private final Button btn = new Button(buttonText);
            {
                btn.getStyleClass().add("button");
                btn.setOnAction(e -> {
                    DonationListViewModel.DonationRow row = getTableView().getItems().get(getIndex());
                    action.accept(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };
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