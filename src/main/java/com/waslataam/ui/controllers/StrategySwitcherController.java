package com.waslataam.ui.controllers;

import com.waslataam.coordination.DonationCoordinator;
import com.waslataam.strategy.MatchingEngine;
import com.waslataam.strategy.MatchingStrategy;
import com.waslataam.strategy.NearestFirstStrategy;
import com.waslataam.strategy.ExpiryFirstStrategy;
import com.waslataam.strategy.CapacityBasedStrategy;
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
    }

    @FXML
    public void initialize() {
        // Will be populated when coordinator is set
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

    @FXML
    private void handleBack() {
        // Navigation handled by MainShellController
    }
}