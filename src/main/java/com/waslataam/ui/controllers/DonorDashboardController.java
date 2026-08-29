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
    @FXML private Button postDonationButton;
    @FXML private TableView<DonationListViewModel.DonationRow> donationsTable;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> typeColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, Number> quantityColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> statusColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> expiryColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> locationColumn;
    @FXML private TableColumn<DonationListViewModel.DonationRow, String> postedColumn;

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
    }

    private void setupTable() {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        expiryColumn.setCellValueFactory(cellData -> cellData.getValue().expiryTimeProperty().asString());
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        postedColumn.setCellValueFactory(cellData -> cellData.getValue().expiryTimeProperty().asString()); // Will fix

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

        postedColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : "N/A"); // Placeholder
            }
        });
    }

    private void loadDonations() {
        if (coordinator == null) return;
        
        var user = sessionContext.getCurrentUser();
        if (user == null) return;

        List<Donation> donations = coordinator.getDonationRepository().findByDonorId(user.getId());
        viewModel.setDonations(donations);
        
        // Update donor names
        for (var row : viewModel.getDonations()) {
            row.setDonorName(user.getName());
        }
    }

    @FXML
    private void handlePostDonation() {
        // Navigation handled by MainShellController
    }

    public void refresh() {
        loadDonations();
    }
}