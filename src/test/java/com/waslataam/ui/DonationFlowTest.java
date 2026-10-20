package com.waslataam.ui;

import com.waslataam.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class DonationFlowTest {
    
    @Start
    private void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    void testFullDonationFlow(FxRobot robot) {
        // Login as Donor
        javafx.scene.control.ComboBox<com.waslataam.domain.UserRole> roleCombo = 
                robot.lookup("#roleComboBox").queryAs(javafx.scene.control.ComboBox.class);
        javafx.scene.control.TextField nameField = 
                robot.lookup("#nameField").queryAs(javafx.scene.control.TextField.class);
        javafx.scene.control.TextField emailField = 
                robot.lookup("#emailField").queryAs(javafx.scene.control.TextField.class);
        javafx.scene.control.Button loginButton = 
                robot.lookup("#loginButton").queryAs(javafx.scene.control.Button.class);

        robot.interact(() -> {
            roleCombo.setValue(com.waslataam.domain.UserRole.DONOR);
            roleCombo.getSelectionModel().select(com.waslataam.domain.UserRole.DONOR);
            nameField.setText("Test Donor");
            emailField.setText("donor@test.com");
            loginButton.fire();
        });
        
        // Verify donor dashboard loads
        org.junit.jupiter.api.Assertions.assertNotNull(robot.lookup("#welcomeLabel").query());
        
        // Navigate to post donation
        javafx.scene.control.Button postBtn = robot.lookup("#postDonationButton").queryAs(javafx.scene.control.Button.class);
        robot.interact(postBtn::fire);
        
        // Fill donation form
        javafx.scene.control.ComboBox<com.waslataam.domain.DonationType> typeCombo = 
                robot.lookup("#typeComboBox").queryAs(javafx.scene.control.ComboBox.class);
        javafx.scene.control.TextField qtyField = 
                robot.lookup("#quantityField").queryAs(javafx.scene.control.TextField.class);
        javafx.scene.control.ComboBox<String> unitCombo = 
                robot.lookup("#unitComboBox").queryAs(javafx.scene.control.ComboBox.class);
        javafx.scene.control.DatePicker datePicker = 
                robot.lookup("#expiryDatePicker").queryAs(javafx.scene.control.DatePicker.class);
        javafx.scene.control.TextField locField = 
                robot.lookup("#locationField").queryAs(javafx.scene.control.TextField.class);
        javafx.scene.control.TextField tempField = 
                robot.lookup("#temperatureField").queryAs(javafx.scene.control.TextField.class);
        javafx.scene.control.Button submitBtn = 
                robot.lookup("#submitButton").queryAs(javafx.scene.control.Button.class);

        robot.interact(() -> {
            typeCombo.setValue(com.waslataam.domain.DonationType.PERISHABLE);
            typeCombo.getSelectionModel().select(com.waslataam.domain.DonationType.PERISHABLE);
            qtyField.setText("25");
            unitCombo.setValue("kg");
            unitCombo.getSelectionModel().select("kg");
            datePicker.setValue(java.time.LocalDate.now().plusDays(2));
            locField.setText("Test Location, Karachi");
            tempField.setText("2-8°C");
            submitBtn.fire();
        });
        
        // Verify donation was created and saved in repository
        com.waslataam.coordination.DonationCoordinator coordinator = com.waslataam.Main.getCoordinator();
        org.junit.jupiter.api.Assertions.assertNotNull(coordinator);
        java.util.List<com.waslataam.domain.Donation> all = coordinator.getDonationRepository().findAll();
        org.junit.jupiter.api.Assertions.assertFalse(all.isEmpty(), "Donation should be saved in repository");
    }
}