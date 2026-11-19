package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.Volunteer;
import com.waslataam.viewmodel.DonationListViewModel;
import com.waslataam.viewmodel.SessionContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.List;
import java.util.UUID;

public class VolunteerDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label openPickupsValue;
    @FXML private Label inTransitValue;
    @FXML private Label completedValue;
    @FXML private Label tableCountLabel;
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
        if (coordinator == null) {
            coordinator = com.waslataam.Main.getCoordinator();
        }
        setupTable();
        pickupsTable.setItems(viewModel.getDonations());
        loadPickups();
        refreshAvailability();
    }

    private void refreshAvailability() {
        var user = sessionContext.getCurrentUser();
        if (user instanceof Volunteer volunteer && availabilityToggle != null) {
            availabilityToggle.setSelected(volunteer.isAvailable());
            availabilityToggle.setText(volunteer.isAvailable() ? "Available" : "Unavailable");
        }
    }

    private void setupTable() {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        recipientColumn.setCellValueFactory(new PropertyValueFactory<>("recipientName"));

        actionColumn.setCellFactory(createActionButtonFactory());
    }

    private void loadPickups() {
        if (coordinator == null) return;

        var user = sessionContext.getCurrentUser();
        if (user == null) return;

        UUID volunteerId = user.getId();

        // The queue contains every claimed/in-transit donation that has not
        // been assigned to another volunteer, plus this volunteer's completed
        // deliveries for context. Unassigned claims are how a new volunteer
        // can pick up their first job.
        List<Donation> donations = coordinator.getDonationRepository().findAll().stream()
                .filter(d -> {
                    DonationStatus status = d.getStatus();
                    boolean unassignedOrMine =
                            d.getVolunteerId() == null || volunteerId.equals(d.getVolunteerId());
                    boolean activePickup = (status == DonationStatus.CLAIMED
                            || status == DonationStatus.IN_TRANSIT) && unassignedOrMine;
                    boolean myHistory = status == DonationStatus.DELIVERED
                            && volunteerId.equals(d.getVolunteerId());
                    return activePickup || myHistory;
                })
                .sorted(java.util.Comparator
                        .comparingInt((Donation d) -> statusRank(d.getStatus()))
                        .thenComparing(Donation::getExpiryTime))
                .toList();
        viewModel.setDonations(donations);

        // Resolve recipient names by the donation's recipient id (same order as the rows).
        var rows = viewModel.getDonations();
        for (int i = 0; i < donations.size(); i++) {
            UUID recipientId = donations.get(i).getRecipientId();
            if (recipientId != null) {
                var row = rows.get(i);
                coordinator.getUserRepository().findById(recipientId)
                        .ifPresent(u -> row.setRecipientName(u.getName()));
            }
        }

        long open = donations.stream().filter(d -> d.getStatus() == DonationStatus.CLAIMED).count();
        long inTransit = donations.stream().filter(d -> d.getStatus() == DonationStatus.IN_TRANSIT).count();
        long completed = donations.stream().filter(d -> d.getStatus() == DonationStatus.DELIVERED).count();

        if (openPickupsValue != null) {
            openPickupsValue.setText(String.valueOf(open));
        }
        if (inTransitValue != null) {
            inTransitValue.setText(String.valueOf(inTransit));
        }
        if (completedValue != null) {
            completedValue.setText(String.valueOf(completed));
        }
        if (tableCountLabel != null) {
            long active = open + inTransit;
            tableCountLabel.setText(active + " active");
        }
    }

    private static int statusRank(DonationStatus status) {
        return switch (status) {
            case CLAIMED -> 0;
            case IN_TRANSIT -> 1;
            case DELIVERED -> 2;
            default -> 3;
        };
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
                    coordinator.pickupDonation(row.getId(), sessionContext.getCurrentUser().getId());

                    showSuccess("Pickup confirmed! Donation is now in transit.");
                    loadPickups();
                    refreshAvailability();
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
                    coordinator.deliverDonation(row.getId(), sessionContext.getCurrentUser().getId());

                    showSuccess("Delivery confirmed! Thank you for your service.");
                    loadPickups();
                    refreshAvailability();
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