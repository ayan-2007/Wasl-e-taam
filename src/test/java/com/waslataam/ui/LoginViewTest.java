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
        org.junit.jupiter.api.Assertions.assertNotNull(robot.lookup("#passwordField").queryAs(javafx.scene.control.PasswordField.class));
        org.junit.jupiter.api.Assertions.assertNotNull(robot.lookup("#loginButton").queryAs(javafx.scene.control.Button.class));
    }

    @Test
    void testLoginWithDemoDonorAccount(FxRobot robot) {
        javafx.scene.control.TextField emailField = 
                robot.lookup("#emailField").queryAs(javafx.scene.control.TextField.class);
        javafx.scene.control.PasswordField passwordField =
                robot.lookup("#passwordField").queryAs(javafx.scene.control.PasswordField.class);
        javafx.scene.control.Button loginButton = 
                robot.lookup("#loginButton").queryAs(javafx.scene.control.Button.class);

        robot.interact(() -> {
            emailField.setText("donor@wasl.demo");
            passwordField.setText(com.waslataam.demo.DemoAccounts.PASSWORD);
            loginButton.fire();
        });
        
        // Should load MainShell
        org.junit.jupiter.api.Assertions.assertNotNull(robot.lookup("#sidebar").query());
    }
}