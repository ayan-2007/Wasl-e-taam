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
        robot.clickOn("#roleComboBox");
        robot.clickOn("Donor");
        robot.clickOn("#nameField").write("Test Donor");
        robot.clickOn("#emailField").write("donor@test.com");
        robot.clickOn("#loginButton");
        
        // Verify donor dashboard loads
        robot.lookup("#welcomeLabel").query().orElseThrow(() -> new AssertionError("Welcome label not found"));
        
        // Navigate to post donation
        robot.clickOn("#postDonationButton");
        
        // Fill donation form
        robot.clickOn("#typeComboBox");
        robot.clickOn("PERISHABLE");
        robot.clickOn("#quantityField").write("25");
        robot.clickOn("#unitComboBox");
        robot.clickOn("kg");
        robot.clickOn("#locationField").write("Test Location, Karachi");
        robot.clickOn("#temperatureField").write("2-8°C");
        
        // Submit
        robot.clickOn("#submitButton");
        
        // Verify success alert appears
        robot.lookup(".alert").query().orElseThrow(() -> new AssertionError("Success alert not shown"));
    }
}