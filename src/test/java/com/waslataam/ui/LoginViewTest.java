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
        robot.lookup(".label").queryAll().stream()
                .filter(l -> l.getText().contains("Wasl-e-Ta'am"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Logo not found"));
        
        robot.lookup("#roleComboBox").queryAs(javafx.scene.control.ComboBox.class)
                .orElseThrow(() -> new AssertionError("Role combo box not found"));
        
        robot.lookup("#nameField").queryAs(javafx.scene.control.TextField.class)
                .orElseThrow(() -> new AssertionError("Name field not found"));
        
        robot.lookup("#emailField").queryAs(javafx.scene.control.TextField.class)
                .orElseThrow(() -> new AssertionError("Email field not found"));
        
        robot.lookup("#loginButton").queryAs(javafx.scene.control.Button.class)
                .orElseThrow(() -> new AssertionError("Login button not found"));
    }

    @Test
    void testLoginWithDonorRole(FxRobot robot) {
        robot.clickOn("#roleComboBox");
        robot.clickOn("Donor");
        robot.clickOn("#nameField").write("Test Donor");
        robot.clickOn("#emailField").write("donor@test.com");
        robot.clickOn("#loginButton");
        
        // Should load MainShell
        robot.lookup("#sidebar").query().orElseThrow(() -> new AssertionError("Sidebar not loaded"));
    }
}