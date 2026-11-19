package com.waslataam.ui.controllers;

import com.waslataam.domain.DonationType;
import com.waslataam.domain.Location;
import com.waslataam.factory.DonationFactory;
import com.waslataam.factory.DonationParams;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PostDonationFormController {
    @FXML private ComboBox<DonationType> typeComboBox;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private DatePicker expiryDatePicker;
    @FXML private Spinner<Integer> expiryTimeSpinner;
    @FXML private TextField locationField;
    @FXML private VBox typeDetailsSection;
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
        expiryTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        shelfLifeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 1));
        servingsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));

        // Sensible defaults so a demo can be posted in a few clicks.
        expiryDatePicker.setValue(LocalDate.now().plusDays(1));
        cookedAtPicker.setValue(LocalDate.now());
        temperatureField.setText("2–8°C, refrigerated");
    }

    @FXML
    private void handleTypeChange() {
        DonationType type = typeComboBox.getValue();
        boolean hasType = type != null;
        typeDetailsSection.setVisible(hasType);
        typeDetailsSection.setManaged(hasType);

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
            com.waslataam.coordination.DonationCoordinator coordinator = com.waslataam.Main.getCoordinator();
            if (coordinator != null) {
                coordinator.postDonation(params);
            }
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
            else if (cookedAtPicker.getValue().isAfter(LocalDate.now())) {
                errors.append("• Cooked date cannot be in the future\n");
            }
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
                   .cookedAt(cookedAtFor(cookedAtPicker.getValue()));
        }

        return builder.build();
    }

    /**
     * PreparedMealDonation rejects a cooked-at time in the future. If the donor
     * picked today, use a time a few minutes ago instead of noon (which may
     * still be ahead of the clock during a morning demo).
     */
    private static LocalDateTime cookedAtFor(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date == null || date.isEqual(today)) {
            return LocalDateTime.now().minusMinutes(10);
        }
        return LocalDateTime.of(date, LocalTime.NOON);
    }

    @FXML
    private void handleBack() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navigateTo("/com/waslataam/ui/resources/fxml/DonorDashboard.fxml", "donorDashboard");
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}