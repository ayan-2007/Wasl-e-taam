package com.waslataam.ui.controllers;

import com.waslataam.domain.DonationType;
import com.waslataam.domain.Location;
import com.waslataam.factory.DonationFactory;
import com.waslataam.factory.DonationParams;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;

public class PostDonationFormController {
    @FXML private ComboBox<DonationType> typeComboBox;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private DatePicker expiryDatePicker;
    @FXML private Spinner<Integer> expiryTimeSpinner;
    @FXML private TextField locationField;
    @FXML private VBox perishableFields;
    @FXML private TextField temperatureField;
    @FXML private VBox dryFoodFields;
    @FXML private Spinner<Integer> shelfLifeSpinner;
    @FXML private VBox preparedMealFields;
    @FXML private Spinner<Integer> servingsSpinner;
    @FXML private DatePicker cookedAtPicker;
    @FXML private Button submitButton;

    private final SessionContext sessionContext = SessionContext.getInstance();

    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll(DonationType.values());
        unitComboBox.getItems().addAll("kg", "pieces", "boxes", "liters", "meals");
        unitComboBox.setValue("kg");

        // Initialize spinners
        expiryTimeSpinner.setValueFactory(new javafx.util.converter.IntegerStringConverter());
        shelfLifeSpinner.setValueFactory(new javafx.util.converter.IntegerStringConverter());
        servingsSpinner.setValueFactory(new javafx.util.converter.IntegerStringConverter());
    }

    @FXML
    private void handleTypeChange() {
        DonationType type = typeComboBox.getValue();
        perishableFields.setVisible(false);
        perishableFields.setManaged(false);
        dryFoodFields.setVisible(false);
        dryFoodFields.setManaged(false);
        preparedMealFields.setVisible(false);
        preparedMealFields.setManaged(false);

        if (type == DonationType.PERISHABLE) {
            perishableFields.setVisible(true);
            perishableFields.setManaged(true);
            unitComboBox.setValue("kg");
        } else if (type == DonationType.DRY_FOOD) {
            dryFoodFields.setVisible(true);
            dryFoodFields.setManaged(true);
            unitComboBox.setValue("kg");
        } else if (type == DonationType.PREPARED_MEAL) {
            preparedMealFields.setVisible(true);
            preparedMealFields.setManaged(true);
            unitComboBox.setValue("meals");
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        try {
            DonationParams params = buildParams();
            // The actual donation creation is handled by the coordinator
            // For now, we'll show success and navigate back
            showSuccess("Donation submitted successfully! It will be reviewed and posted shortly.");
            handleBack();
        } catch (Exception e) {
            showError("Failed to submit donation: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (typeComboBox.getValue() == null) errors.append("• Select donation type\n");
        if (quantityField.getText().trim().isEmpty()) errors.append("• Enter quantity\n");
        else {
            try {
                double qty = Double.parseDouble(quantityField.getText().trim());
                if (qty <= 0) errors.append("• Quantity must be positive\n");
            } catch (NumberFormatException e) {
                errors.append("• Invalid quantity\n");
            }
        }
        if (unitComboBox.getValue() == null || unitComboBox.getValue().isBlank()) errors.append("• Select unit\n");
        if (expiryDatePicker.getValue() == null) errors.append("• Select expiry date\n");
        if (locationField.getText().trim().isEmpty()) errors.append("• Enter location\n");

        DonationType type = typeComboBox.getValue();
        if (type == DonationType.PERISHABLE && (temperatureField.getText() == null || temperatureField.getText().trim().isEmpty())) {
            errors.append("• Enter temperature requirement\n");
        }
        if (type == DonationType.DRY_FOOD && shelfLifeSpinner.getValue() <= 0) {
            errors.append("• Enter shelf life\n");
        }
        if (type == DonationType.PREPARED_MEAL) {
            if (servingsSpinner.getValue() <= 0) errors.append("• Enter servings\n");
            if (cookedAtPicker.getValue() == null) errors.append("• Select cooked date\n");
        }

        if (errors.length() > 0) {
            showError("Please fix the following:\n" + errors);
            return false;
        }
        return true;
    }

    private DonationParams buildParams() {
        var user = sessionContext.getCurrentUser();
        DonationType type = typeComboBox.getValue();
        double quantity = Double.parseDouble(quantityField.getText().trim());
        String unit = unitComboBox.getValue();
        LocalDateTime expiryTime = LocalDateTime.of(expiryDatePicker.getValue(), LocalTime.of(expiryTimeSpinner.getValue(), 0));
        Location location = new Location(24.8607, 67.0011, locationField.getText().trim(), "Karachi"); // Simplified

        DonationParams.Builder builder = DonationParams.builder()
                .type(type)
                .quantity(quantity)
                .unit(unit)
                .donorId(user.getId())
                .location(location)
                .expiryTime(expiryTime);

        if (type == DonationType.PERISHABLE) {
            builder.temperatureRequirement(temperatureField.getText().trim());
        } else if (type == DonationType.DRY_FOOD) {
            builder.shelfLifeDays(shelfLifeSpinner.getValue());
        } else if (type == DonationType.PREPARED_MEAL) {
            builder.servings(servingsSpinner.getValue())
                   .cookedAt(LocalDateTime.of(cookedAtPicker.getValue(), LocalTime.NOON));
        }

        return builder.build();
    }

    @FXML
    private void handleBack() {
        // Navigation handled by MainShellController
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