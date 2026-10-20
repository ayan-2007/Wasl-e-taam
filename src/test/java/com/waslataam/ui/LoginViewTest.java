package com.waslataam.ui;

import com.waslataam.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class LoginViewTest {
    
    @Start
    private void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    void testLoginViewLoads(FxRobot robot) {
        // Verify login view elements are present
        boolean logoFound = robot.lookup(".label").queryAll().stream()
                .filter(javafx.scene.control.Label.class::isInstance)
                .map(javafx.scene.control.Label.class::cast)
                .anyMatch(l -> l.getText() != null && l.getText().contains("Wasl-e-Ta'am"));
        org.junit.jupiter.api.Assertions.assertTrue(logoFound, "Logo not found");
        
        org.junit.jupiter.api.Assertions.assertNotNull(robot.lookup("#roleComboBox").queryAs(javafx.scene.control.ComboBox.class));
        org.junit.jupiter.api.Assertions.assertNotNull(robot.lookup("#nameField").queryAs(javafx.scene.control.TextField.class));
        org.junit.jupiter.api.Assertions.assertNotNull(robot.lookup("#emailField").queryAs(javafx.scene.control.TextField.class));
        org.junit.jupiter.api.Assertions.assertNotNull(robot.lookup("#loginButton").queryAs(javafx.scene.control.Button.class));
    }

    @Test
    void testLoginWithDonorRole(FxRobot robot) {
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
        
        // Should load MainShell
        org.junit.jupiter.api.Assertions.assertNotNull(robot.lookup("#sidebar").query());
    }
}