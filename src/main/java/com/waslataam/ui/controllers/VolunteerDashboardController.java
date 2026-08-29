package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.command.MarkPickedUpCommand;
import com.waslataam.command.MarkDeliveredCommand;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.Volunteer;
import com.waslataam.viewmodel.DonationListViewModel;
import com.waslataam.viewmodel.SessionContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.List;
import java.util.UUID;

public class VolunteerDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private ToggleButton availabilityToggle;
    @FXML private TableView<DonationListViewModel.DonationRow> pickupsTable;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> typeColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, Number> quantityColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> statusColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> locationColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> recipientColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, Void> actionColumn;

    private final DonationListViewModel viewModel = new DonationListViewModel();
    private final SessionContext sessionContext = SessionContext.getInstance();
    private DonationCoordinator coordinator;

    public void setCoordinator(DonationCoordinator coordinator) {
        this.coordinator = coordinator;
        loadPickups();
    }

    @FXML
    public void initialize() {
        setupTable();
        pickupsTable.setItems(viewModel.getDonations());
        
        // Set initial availability
        var user = sessionContext.getCurrentUser();
        if (user instanceof Volunteer volunteer) {
            availabilityToggle.setSelected(volunteer.isAvailable());
            availabilityToggle.setText(volunteer.isAvailable() ? "Available" : "Unavailable");
        }
    }

    private void setupTable() {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        recipientColumn.setCellValueFactory(new PropertyValueFactory<>("donorName"));

        actionColumn.setCellFactory(createActionButtonFactory());
    }

    private void loadPickups() {
        if (coordinator == null) return;

        var user = sessionContext.getCurrentUser();
        if (user == null) return;

        List<Donation> donations = coordinator.getDonationRepository().findByVolunteerId(user.getId());
        viewModel.setDonations(donations);
    }

    @FXML
    private void handleAvailabilityToggle() {
        var user = sessionContext.getCurrentUser();
        if (user instanceof Volunteer volunteer) {
            volunteer.setAvailable(availabilityToggle.isSelected());
            availabilityToggle.setText(availabilityToggle.isSelected() ? "Available" : "Unavailable");
            
            // In a real app, update in repository
            coordinator.getUserRepository().update(volunteer);
        }
    }

    private Callback<TableColumn<DonationListViewModel.DonationRow, Void>, TableCell<DonationListViewModel.DonationRow, Void>> createActionButtonFactory() {
        return col -> new TableCell<>() {
            private final HBox buttonBox = new HBox(8);
            private final Button pickupBtn = new Button("Confirm Pickup");
            private final Button deliverBtn = new Button("Confirm Delivery");

            {
                pickupBtn.getStyleClass().add("button");
                deliverBtn.getStyleClass().add("button-secondary");
                
                pickupBtn.setOnAction(e -> {
                    DonationListViewModel.DonationRow row = getTableView().getItems().get(getIndex());
                    handlePickup(row);
                });
                
                deliverBtn.setOnAction(e -> {
                    DonationListViewModel.DonationRow row = getTableView().getItems().get(getIndex());
                    handleDeliver(row);
                });
                
                buttonBox.getChildren().addAll(pickupBtn, deliverBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DonationListViewModel.DonationRow row = getTableView().getItems().get(getIndex());
                    DonationStatus status = DonationStatus.valueOf(row.getStatus().replace(" ", "_").toUpperCase());
                    
                    buttonBox.getChildren().clear();
                    if (status == DonationStatus.CLAIMED) {
                        buttonBox.getChildren().add(pickupBtn);
                    } else if (status == DonationStatus.IN_TRANSIT) {
                        buttonBox.getChildren().add(deliverBtn);
                    }
                    setGraphic(buttonBox);
                }
            }
        };
    }

    private void handlePickup(DonationListViewModel.DonationRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Pickup");
        confirm.setHeaderText("Confirm you have picked up this donation?");
        confirm.setContentText("This will update the status to 'In Transit'.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    MarkPickedUpCommand cmd = new MarkPickedUpCommand(
                            coordinator.getDonationRepository(),
                            row.getId(),
                            sessionContext.getCurrentUser().getId()
                    );
                    coordinator.getCommandInvoker().execute(cmd);
                    
                    showSuccess("Pickup confirmed! Donation is now in transit.");
                    loadPickups();
                } catch (Exception e) {
                    showError("Failed to confirm pickup: " + e.getMessage());
                }
            }
        });
    }

    private void handleDeliver(DonationListViewModel.DonationRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delivery");
        confirm.setHeaderText("Confirm you have delivered this donation?");
        confirm.setContentText("This will mark the donation as delivered and update impact metrics.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    MarkDeliveredCommand cmd = new MarkDeliveredCommand(
                            coordinator.getDonationRepository(),
                            row.getId(),
                            sessionContext.getCurrentUser().getId()
                    );
                    coordinator.getCommandInvoker().execute(cmd);
                    
                    showSuccess("Delivery confirmed! Thank you for your service.");
                    loadPickups();
                } catch (Exception e) {
                    showError("Failed to confirm delivery: " + e.getMessage());
                }
            }
        });
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