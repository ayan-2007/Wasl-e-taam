package com.waslataam.ui.controllers;

import com.waslataam.domain.Admin;
import com.waslataam.domain.AdminPermission;
import com.waslataam.domain.User;
import com.waslataam.security.AdminGuardrails;
import com.waslataam.ui.util.SceneFactory;
import com.waslataam.ui.util.ThemeManager;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainShellController {
    @FXML private VBox sidebar;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private VBox navButtons;
    @FXML private Button themeButton;
    @FXML private Button logoutButton;
    @FXML private StackPane contentArea;

    private static MainShellController instance;
    private final SessionContext sessionContext = SessionContext.getInstance();

    public static MainShellController getInstance() {
        return instance;
    }

    public void navigateTo(String fxmlPath, String cacheKey) {
        // Re-check the administrator permission on every navigation: a button
        // left over from a previous session must not open a guarded screen.
        AdminPermission required = AdminGuardrails.requiredPermission(fxmlPath);
        if (required != null && !AdminGuardrails.hasPermission(sessionContext.getCurrentUser(), required)) {
            showError("Access denied. Your administrator account does not have the “"
                    + required.getDisplayName() + "” permission.");
            return;
        }
        loadView(fxmlPath);
    }

    @FXML
    public void initialize() {
        instance = this;
        setupUserInfo();
        setupNavigation();
        themeButton.setText(ThemeManager.isDark() ? "Light mode" : "Dark mode");
        loadDefaultView();
    }

    private void setupUserInfo() {
        User user = sessionContext.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getName());
            if (user instanceof Admin admin) {
                userRoleLabel.setText("Admin · " + AdminGuardrails.describeScope(admin));
            } else {
                userRoleLabel.setText(user.getRole().getDisplayName());
            }
        }
    }

    private void setupNavigation() {
        var role = sessionContext.getCurrentRole();
        navButtons.getChildren().clear();

        if (role == com.waslataam.domain.UserRole.DONOR) {
            addNavButton("My Donations", "/com/waslataam/ui/resources/fxml/DonorDashboard.fxml", "donorDashboard");
            addNavButton("Post Donation", "/com/waslataam/ui/resources/fxml/PostDonationForm.fxml", "postDonation");
        } else if (role == com.waslataam.domain.UserRole.RECIPIENT) {
            addNavButton("Available Donations", "/com/waslataam/ui/resources/fxml/RecipientDashboard.fxml", "recipientDashboard");
        } else if (role == com.waslataam.domain.UserRole.VOLUNTEER) {
            addNavButton("My Pickups", "/com/waslataam/ui/resources/fxml/VolunteerDashboard.fxml", "volunteerDashboard");
        } else if (role == com.waslataam.domain.UserRole.ADMIN) {
            addNavButton("Dashboard", "/com/waslataam/ui/resources/fxml/AdminDashboard.fxml", "adminDashboard");
            addNavButton("Strategy Switcher", "/com/waslataam/ui/resources/fxml/StrategySwitcherView.fxml", "strategySwitcher");
            addNavButton("Audit Report", "/com/waslataam/ui/resources/fxml/AuditReportView.fxml", "auditReport");
            addNavButton("Impact Dashboard", "/com/waslataam/ui/resources/fxml/ImpactDashboardView.fxml", "impactDashboard");
        }
    }

    private void addNavButton(String text, String fxmlPath, String cacheKey) {
        Button btn = new Button(text);
        btn.getStyleClass().add("button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> navigateTo(fxmlPath, cacheKey));
        navButtons.getChildren().add(btn);
    }

    private void loadDefaultView() {
        var role = sessionContext.getCurrentRole();
        String defaultView = switch (role) {
            case DONOR -> "/com/waslataam/ui/resources/fxml/DonorDashboard.fxml";
            case RECIPIENT -> "/com/waslataam/ui/resources/fxml/RecipientDashboard.fxml";
            case VOLUNTEER -> "/com/waslataam/ui/resources/fxml/VolunteerDashboard.fxml";
            case ADMIN -> "/com/waslataam/ui/resources/fxml/AdminDashboard.fxml";
            case null -> "/com/waslataam/ui/resources/fxml/DonorDashboard.fxml";
        };
        navigateTo(defaultView, "default");
    }

    /**
     * Loads a fresh view on every navigation. The old cache served stale
     * dashboards (for example, a donation posted after the donor dashboard was
     * first opened would not appear until restart).
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            showError("Failed to load view: " + e.getMessage());
        }
    }

    @FXML
    private void handleThemeToggle() {
        if (themeButton.getScene() != null) {
            ThemeManager.toggle(themeButton.getScene());
            themeButton.setText(ThemeManager.isDark() ? "Light mode" : "Dark mode");
        }
    }

    @FXML
    private void handleLogout() {
        sessionContext.clear();
        instance = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/waslataam/ui/resources/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(SceneFactory.create(root, 1080, 720));
            stage.setTitle("Wasl-e-Ta'am - Login");
            stage.show();
        } catch (IOException e) {
            showError("Failed to load login view: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}