package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.domain.Donation;
import com.waslataam.domain.DonationStatus;
import com.waslataam.domain.Donor;
import com.waslataam.domain.Recipient;
import com.waslataam.viewmodel.DashboardViewModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ImpactDashboardController {
    @FXML private Label kgSavedValue;
    @FXML private Label mealsServedValue;
    @FXML private Label co2AvoidedValue;
    @FXML private Label activeDonationsValue;
    @FXML private TableView<TopEntityRow> topDonorsTable;
    @FXML private TableView<TopEntityRow> topRecipientsTable;

    private final DashboardViewModel dashboardViewModel = DashboardViewModel.getInstance();
    private DonationCoordinator coordinator;

    public void setCoordinator(DonationCoordinator coordinator) {
        this.coordinator = coordinator;
        bindDashboard();
        loadTopEntities();
    }

    private void bindDashboard() {
        kgSavedValue.textProperty().bind(dashboardViewModel.kgSavedProperty().asString("%.1f kg"));
        mealsServedValue.textProperty().bind(dashboardViewModel.mealsServedProperty().asString("%d meals"));
        co2AvoidedValue.textProperty().bind(dashboardViewModel.co2AvoidedProperty().asString("%.1f kg CO₂"));
        activeDonationsValue.textProperty().bind(dashboardViewModel.activeDonationsProperty().asString("%d active"));
    }

    @FXML
    public void initialize() {
        setupTopDonorsTable();
        setupTopRecipientsTable();
    }

    private void setupTopDonorsTable() {
        topDonorsTable.getColumns().clear();
        
        TableColumn<TopEntityRow, Integer> rankCol = new TableColumn<>("Rank");
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        rankCol.setPrefWidth(60);
        
        TableColumn<TopEntityRow, String> nameCol = new TableColumn<>("Donor");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<TopEntityRow, Integer> countCol = new TableColumn<>("Donations");
        countCol.setCellValueFactory(new PropertyValueFactory<>("count"));
        countCol.setPrefWidth(100);
        
        TableColumn<TopEntityRow, Double> kgCol = new TableColumn<>("Total kg");
        kgCol.setCellValueFactory(new PropertyValueFactory<>("totalKg"));
        kgCol.setPrefWidth(100);
        
        topDonorsTable.getColumns().addAll(rankCol, nameCol, countCol, kgCol);
    }

    private void setupTopRecipientsTable() {
        topRecipientsTable.getColumns().clear();
        
        TableColumn<TopEntityRow, Integer> rankCol = new TableColumn<>("Rank");
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        rankCol.setPrefWidth(60);
        
        TableColumn<TopEntityRow, String> nameCol = new TableColumn<>("Recipient");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<TopEntityRow, Integer> countCol = new TableColumn<>("Claims");
        countCol.setCellValueFactory(new PropertyValueFactory<>("count"));
        countCol.setPrefWidth(100);
        
        TableColumn<TopEntityRow, Double> kgCol = new TableColumn<>("Total kg");
        kgCol.setCellValueFactory(new PropertyValueFactory<>("totalKg"));
        kgCol.setPrefWidth(100);
        
        topRecipientsTable.getColumns().addAll(rankCol, nameCol, countCol, kgCol);
    }

    private void loadTopEntities() {
        if (coordinator == null) return;

        List<Donation> deliveredDonations = coordinator.getDonationRepository().findByStatus(DonationStatus.DELIVERED);

        // Top Donors
        Map<UUID, DonorStats> donorStats = deliveredDonations.stream()
                .collect(Collectors.groupingBy(
                        Donation::getDonorId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    double totalKg = list.stream().mapToDouble(Donation::getQuantity).sum();
                                    return new DonorStats(list.size(), totalKg);
                                }
                        )
                ));

        var donorItems = FXCollections.<TopEntityRow>observableArrayList();
        AtomicInteger donorRank = new AtomicInteger(1);
        for (var entry : donorStats.entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, DonorStats>comparingByValue(Comparator.comparingDouble(ds -> -ds.totalKg)))
                .limit(10)
                .toList()) {
            
            coordinator.getUserRepository().findById(entry.getKey()).ifPresent(user -> {
                if (user instanceof Donor donor) {
                    donorItems.add(new TopEntityRow(
                            donorRank.getAndIncrement(), donor.getName(), entry.getValue().count, entry.getValue().totalKg
                    ));
                }
            });
        }
        topDonorsTable.setItems(donorItems);

        // Top Recipients
        Map<UUID, RecipientStats> recipientStats = deliveredDonations.stream()
                .filter(d -> d.getRecipientId() != null)
                .collect(Collectors.groupingBy(
                        Donation::getRecipientId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    double totalKg = list.stream().mapToDouble(Donation::getQuantity).sum();
                                    return new RecipientStats(list.size(), totalKg);
                                }
                        )
                ));

        var recipientItems = FXCollections.<TopEntityRow>observableArrayList();
        AtomicInteger recipientRank = new AtomicInteger(1);
        for (var entry : recipientStats.entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, RecipientStats>comparingByValue(Comparator.comparingDouble(rs -> -rs.totalKg)))
                .limit(10)
                .toList()) {
            
            coordinator.getUserRepository().findById(entry.getKey()).ifPresent(user -> {
                if (user instanceof Recipient recipient) {
                    recipientItems.add(new TopEntityRow(
                            recipientRank.getAndIncrement(), recipient.getName(), entry.getValue().count, entry.getValue().totalKg
                    ));
                }
            });
        }
        topRecipientsTable.setItems(recipientItems);
    }

    @FXML
    private void handleBack() {
        // Navigation handled by MainShellController
    }

    private record DonorStats(int count, double totalKg) {}
    private record RecipientStats(int count, double totalKg) {}

    public static class TopEntityRow {
        private final int rank;
        private final String name;
        private final int count;
        private final double totalKg;

        public TopEntityRow(int rank, String name, int count, double totalKg) {
            this.rank = rank;
            this.name = name;
            this.count = count;
            this.totalKg = totalKg;
        }

        public int getRank() { return rank; }
        public String getName() { return name; }
        public int getCount() { return count; }
        public double getTotalKg() { return totalKg; }
    }
}