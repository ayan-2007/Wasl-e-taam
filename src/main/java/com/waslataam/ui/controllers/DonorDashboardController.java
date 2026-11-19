package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.viewmodel.DonationListViewModel;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DonorDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label totalDonationsValue;
    @FXML private Label activeDonationsValue;
    @FXML private Label deliveredValue;
    @FXML private Label tableCountLabel;
    @FXML private Button postDonationButton;
    @FXML private TableView<DonationListViewModel.DonationRow> donationsTable;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> typeColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, Number> quantityColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> statusColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> expiryColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> locationColumn;

    private final DonationListViewModel viewModel = new DonationListViewModel();
    private final SessionContext sessionContext = SessionContext.getInstance();
    private DonationCoordinator coordinator;

    public void setCoordinator(DonationCoordinator coordinator) {
        this.coordinator = coordinator;
        loadDonations();
    }

    @FXML
    public void initialize() {
        setupTable();
        donationsTable.setItems(viewModel.getDonations());
        if (coordinator == null) {
            setCoordinator(com.waslataam.Main.getCoordinator());
        }
    }

    private void setupTable() {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        expiryColumn.setCellValueFactory(cellData -> cellData.getValue().expiryTimeProperty().asString());
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        // Format dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        expiryColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    try {
                        setText(java.time.LocalDateTime.parse(item).format(formatter));
                    } catch (Exception e) {
                        setText(item);
                    }
                }
            }
        });
    }

    private void loadDonations() {
        if (coordinator == null) return;
        
        var user = sessionContext.getCurrentUser();
        if (user == null) return;

        List<Donation> donations = coordinator.getDonationRepository().findByDonorId(user.getId())
                .stream()
                .sorted(java.util.Comparator.comparing(Donation::getPostedAt).reversed())
                .toList();
        viewModel.setDonations(donations);
        
        // Update donor names
        for (var row : viewModel.getDonations()) {
            row.setDonorName(user.getName());
        }

        long active = donations.stream().filter(d -> d.getStatus().isActive()).count();
        long delivered = donations.stream().filter(d -> d.getStatus() == DonationStatus.DELIVERED).count();

        if (welcomeLabel != null) {
            welcomeLabel.setText("Your donations, " + user.getName());
        }
        if (totalDonationsValue != null) {
            totalDonationsValue.setText(String.valueOf(donations.size()));
        }
        if (activeDonationsValue != null) {
            activeDonationsValue.setText(String.valueOf(active));
        }
        if (deliveredValue != null) {
            deliveredValue.setText(String.valueOf(delivered));
        }
        if (tableCountLabel != null) {
            tableCountLabel.setText(donations.size() + (donations.size() == 1 ? " listing" : " listings"));
        }
    }

    @FXML
    private void handlePostDonation() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navigateTo("/com/waslataam/ui/resources/fxml/PostDonationForm.fxml", "postDonation");
        }
    }

    public void refresh() {
        loadDonations();
    }
}