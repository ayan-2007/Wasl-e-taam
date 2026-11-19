package com.waslataam.ui.controllers;

import com.waslataam.auth.AuthException;
import com.waslataam.auth.AuthService;
import com.waslataam.demo.DemoAccounts;
import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;
import com.waslataam.ui.util.SceneFactory;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {
    @FXML private Label formTitleLabel;
    @FXML private Label formSubtitleLabel;
    @FXML private VBox demoBox;
    @FXML private ComboBox<DemoAccounts> demoAccountComboBox;
    @FXML private VBox roleRow;
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private VBox nameRow;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private VBox confirmRow;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button loginButton;
    @FXML private Button modeToggleButton;
    @FXML private Label errorLabel;

    private final SessionContext sessionContext = SessionContext.getInstance();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginController.class);

    private boolean signupMode;

    @FXML
    public void initialize() {
        demoAccountComboBox.getItems().setAll(DemoAccounts.all());
        roleComboBox.getItems().setAll(UserRole.DONOR, UserRole.RECIPIENT, UserRole.VOLUNTEER);
        roleComboBox.setValue(UserRole.DONOR);
        configureMode(false);
    }

    @FXML
    private void handleModeToggle() {
        configureMode(!signupMode);
    }

    private void configureMode(boolean signup) {
        this.signupMode = signup;

        formTitleLabel.setText(signup ? "Create your account" : "Sign in");
        formSubtitleLabel.setText(signup
                ? "Join as a donor, recipient or volunteer. Your account is stored locally so you can sign in again later."
                : "Sign in with the email and password you registered. Demo accounts are ready to explore every role.");
        loginButton.setText(signup ? "Create account" : "Sign in");
        modeToggleButton.setText(signup
                ? "Already registered? Sign in"
                : "New here? Create an account");

        setRowVisible(demoBox, !signup);
        setRowVisible(roleRow, signup);
        setRowVisible(nameRow, signup);
        setRowVisible(confirmRow, signup);

        if (signup) {
            if (roleComboBox.getValue() == null) {
                roleComboBox.setValue(UserRole.DONOR);
            }
        }
        clearError();
        if (signup) {
            nameField.requestFocus();
        } else {
            emailField.requestFocus();
        }
    }

    private static void setRowVisible(VBox row, boolean visible) {
        row.setVisible(visible);
        row.setManaged(visible);
    }

    @FXML
    private void handleDemoAccountSelected() {
        DemoAccounts demo = demoAccountComboBox.getValue();
        if (demo == null) {
            return;
        }
        emailField.setText(demo.email());
        passwordField.setText(DemoAccounts.PASSWORD);
        clearError();
    }

    @FXML
    private void handleLogin() {
        clearError();
        if (signupMode) {
            handleSignup();
        } else {
            handleSignIn();
        }
    }

    private void handleSignIn() {
        String email = text(emailField);
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Enter your email and password.");
            return;
        }

        AuthService authService = com.waslataam.Main.getAuthService();
        if (authService == null) {
            showError("Authentication is not available. Restart the application.");
            return;
        }

        try {
            User user = authService.login(email, password);
            sessionContext.setCurrentUser(user);
            loadMainShell();
        } catch (AuthException e) {
            passwordField.clear();
            showError(e.getMessage());
            passwordField.requestFocus();
        } catch (Exception e) {
            log.error("Sign in failed", e);
            showError("Sign in failed: " + e.getMessage());
        }
    }

    private void handleSignup() {
        UserRole role = roleComboBox.getValue();
        String name = text(nameField);
        String email = text(emailField);
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        AuthService authService = com.waslataam.Main.getAuthService();
        if (authService == null) {
            showError("Authentication is not available. Restart the application.");
            return;
        }

        try {
            User user = authService.register(name, email, password, confirm, role, null);
            sessionContext.setCurrentUser(user);
            log.info("New account created and signed in: {}", email);
            loadMainShell();
        } catch (AuthException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            log.error("Registration failed", e);
            showError("Registration failed: " + e.getMessage());
        }
    }

    private void loadMainShell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/waslataam/ui/resources/fxml/MainShell.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(SceneFactory.create(root, 1280, 840));
            stage.setTitle("Wasl-e-Ta'am - " + sessionContext.getCurrentUser().getName());
            stage.show();
        } catch (Exception e) {
            log.error("Failed to load main shell", e);
            showError("Failed to load main shell: " + e.getMessage());
        }
    }

    private static String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
