package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.domain.AdminPermission;
import com.waslataam.security.AdminGuardrails;
import com.waslataam.strategy.MatchingEngine;
import com.waslataam.strategy.MatchingStrategy;
import com.waslataam.strategy.NearestFirstStrategy;
import com.waslataam.strategy.ExpiryFirstStrategy;
import com.waslataam.strategy.CapacityBasedStrategy;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;

public class StrategySwitcherController {
    @FXML private ComboBox<String> strategyComboBox;
    @FXML private Label currentStrategyLabel;

    private DonationCoordinator coordinator;

    public void setCoordinator(DonationCoordinator coordinator) {
        this.coordinator = coordinator;
        initializeStrategies();
        applyGuardrails();
    }

    @FXML
    public void initialize() {
        if (coordinator == null) {
            coordinator = com.waslataam.Main.getCoordinator();
        }
        initializeStrategies();
        applyGuardrails();
    }

    private void initializeStrategies() {
        if (coordinator == null) return;
        
        MatchingEngine engine = coordinator.getMatchingEngine();
        MatchingStrategy current = engine.getCurrentStrategy();
        
        strategyComboBox.getItems().clear();
        strategyComboBox.getItems().addAll("Nearest First", "Expiry First", "Capacity Based");
        strategyComboBox.setValue(current.getName());
        currentStrategyLabel.setText("Current: " + current.getName());
    }

    @FXML
    private void handleStrategyChange() {
        if (coordinator == null) return;

        if (!AdminGuardrails.hasPermission(SessionContext.getInstance().getCurrentUser(),
                AdminPermission.MANAGE_STRATEGIES)) {
            currentStrategyLabel.setText("Read-only — your administrator account cannot change matching strategies.");
            return;
        }

        String selected = strategyComboBox.getValue();
        if (selected == null) return;

        MatchingEngine engine = coordinator.getMatchingEngine();
        MatchingStrategy newStrategy = switch (selected) {
            case "Nearest First" -> new NearestFirstStrategy();
            case "Expiry First" -> new ExpiryFirstStrategy();
            case "Capacity Based" -> new CapacityBasedStrategy();
            default -> engine.getCurrentStrategy();
        };

        engine.setStrategy(newStrategy);
        currentStrategyLabel.setText("Current: " + newStrategy.getName());
    }

    private void applyGuardrails() {
        if (coordinator == null) {
            return;
        }
        boolean allowed = AdminGuardrails.hasPermission(
                SessionContext.getInstance().getCurrentUser(), AdminPermission.MANAGE_STRATEGIES);
        strategyComboBox.setDisable(!allowed);
        if (!allowed) {
            currentStrategyLabel.setText("Read-only · current: "
                    + coordinator.getMatchingEngine().getCurrentStrategy().getName()
                    + " — changing it requires the Manage Matching Strategies permission.");
        }
    }

    @FXML
    private void handleBack() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navigateTo("/com/waslataam/ui/resources/fxml/AdminDashboard.fxml", "adminDashboard");
        }
    }
}