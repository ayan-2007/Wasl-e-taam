package com.waslataam.ui.controllers;

import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainShellController {
    @FXML private VBox sidebar;
    @FXML private Label logoLabel;
    @FXML private Label userInfoLabel;
    @FXML private VBox navButtons;
    @FXML private Button logoutButton;
    @FXML private StackPane contentArea;

    private static MainShellController instance;
    private final SessionContext sessionContext = SessionContext.getInstance();
    private final Map<String, Node> viewCache = new HashMap<>();

    public static MainShellController getInstance() {
        return instance;
    }

    public void navigateTo(String fxmlPath, String cacheKey) {
        loadView(fxmlPath, cacheKey);
    }

    @FXML
    public void initialize() {
        instance = this;
        setupUserInfo();
        setupNavigation();
        loadDefaultView();
    }

    private void setupUserInfo() {
        var user = sessionContext.getCurrentUser();
        if (user != null) {
            userInfoLabel.setText(user.getName() + " (" + user.getRole().getDisplayName() + ")");
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
        btn.setOnAction(e -> loadView(fxmlPath, cacheKey));
        navButtons.getChildren().add(btn);
    }

    private void loadDefaultView() {
        var role = sessionContext.getCurrentRole();
        String defaultView = switch (role) {
            case DONOR -> "/com/waslataam/ui/resources/fxml/DonorDashboard.fxml";
            case RECIPIENT -> "/com/waslataam/ui/resources/fxml/RecipientDashboard.fxml";
            case VOLUNTEER -> "/com/waslataam/ui/resources/fxml/VolunteerDashboard.fxml";
            case ADMIN -> "/com/waslataam/ui/resources/fxml/AdminDashboard.fxml";
            default -> "/com/waslataam/ui/resources/fxml/DonorDashboard.fxml";
        };
        loadView(defaultView, "default");
    }

    private void loadView(String fxmlPath, String cacheKey) {
        try {
            Node view = viewCache.computeIfAbsent(cacheKey, k -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    return loader.load();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load view: " + fxmlPath, e);
                }
            });
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            showError("Failed to load view: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        sessionContext.clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/waslataam/ui/resources/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 400, 500));
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