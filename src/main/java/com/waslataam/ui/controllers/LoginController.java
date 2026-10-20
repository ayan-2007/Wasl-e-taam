package com.waslataam.ui.controllers;

import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;
import com.waslataam.factory.UserFactory;
import com.waslataam.factory.UserParams;
import com.waslataam.domain.Location;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class LoginController {
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final UserFactory userFactory = new UserFactory();
    private final SessionContext sessionContext = SessionContext.getInstance();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginController.class);

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll(UserRole.values());
        roleComboBox.setPromptText("Select your role");
    }

    @FXML
    private void handleLogin() {
        UserRole role = roleComboBox.getValue() != null ? roleComboBox.getValue() : roleComboBox.getSelectionModel().getSelectedItem();
        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";

        log.info("handleLogin called with role={}, name={}, email={}", role, name, email);

        if (role == null || name.isEmpty() || email.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        try {
            // Check if user exists, otherwise create new
            User existingUser = findUserByEmail(email);
            User user;
            
            if (existingUser != null) {
                if (existingUser.getRole() != role) {
                    showError("User exists with different role: " + existingUser.getRole().getDisplayName());
                    return;
                }
                user = existingUser;
            } else {
                // Create new user with default location
                Location defaultLocation = new Location(24.8607, 67.0011, "Karachi, Pakistan", "Karachi");
                UserParams params = UserParams.builder()
                        .role(role)
                        .name(name)
                        .email(email)
                        .location(defaultLocation)
                        .organizationName(role == UserRole.DONOR ? name + " Organization" : null)
                        .capacityKg(role == UserRole.RECIPIENT ? 1000 : 0)
                        .reliabilityScore(role == UserRole.RECIPIENT ? 0.8 : 0.0)
                        .vehicleType(role == UserRole.VOLUNTEER ? com.waslataam.domain.VehicleType.CAR : null)
                        .build();
                user = userFactory.create(params);
            }

            sessionContext.setCurrentUser(user);
            loadMainShell();
        } catch (Exception e) {
            log.error("Login failed with exception", e);
            showError("Login failed: " + e.getMessage());
        }
    }

    private User findUserByEmail(String email) {
        // In a real app, this would query the user repository
        // For now, return null to always create new users
        return null;
    }

    private void loadMainShell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/waslataam/ui/resources/fxml/MainShell.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Wasl-e-Ta'am - " + sessionContext.getCurrentUser().getName());
            stage.show();
        } catch (Exception e) {
            log.error("Failed to load main shell", e);
            showError("Failed to load main shell: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}