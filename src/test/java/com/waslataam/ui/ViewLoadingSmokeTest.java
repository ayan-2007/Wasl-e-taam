package com.waslataam.ui;

import com.waslataam.Main;
import com.waslataam.domain.Admin;
import com.waslataam.domain.Location;
import com.waslataam.viewmodel.SessionContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Loads every FXML view so broken fx:id / onAction wiring fails the build,
 * not the first person who opens a screen.
 */
@ExtendWith(ApplicationExtension.class)
class ViewLoadingSmokeTest {

    private static final List<String> VIEWS = List.of(
            "/com/waslataam/ui/resources/fxml/LoginView.fxml",
            "/com/waslataam/ui/resources/fxml/DonorDashboard.fxml",
            "/com/waslataam/ui/resources/fxml/PostDonationForm.fxml",
            "/com/waslataam/ui/resources/fxml/RecipientDashboard.fxml",
            "/com/waslataam/ui/resources/fxml/AvailableDonationsView.fxml",
            "/com/waslataam/ui/resources/fxml/VolunteerDashboard.fxml",
            "/com/waslataam/ui/resources/fxml/AdminDashboard.fxml",
            "/com/waslataam/ui/resources/fxml/StrategySwitcherView.fxml",
            "/com/waslataam/ui/resources/fxml/AuditReportView.fxml",
            "/com/waslataam/ui/resources/fxml/ImpactDashboardView.fxml",
            "/com/waslataam/ui/resources/fxml/MainShell.fxml"
    );

    @Start
    private void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    void everyViewLoads(FxRobot robot) {
        SessionContext.getInstance().setCurrentUser(new Admin(
                UUID.randomUUID(),
                "Smoke Test Admin",
                "admin@smoke.test",
                new Location(24.8607, 67.0011, "Karachi, Pakistan", "Karachi"),
                java.util.EnumSet.noneOf(com.waslataam.domain.AdminPermission.class)
        ));

        for (String path : VIEWS) {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            robot.interact(() -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource(path));
                    Assertions.assertNotNull(root, "FXML returned no root: " + path);
                } catch (IOException | RuntimeException e) {
                    failure.set(e);
                }
            });
            if (failure.get() != null) {
                throw new AssertionError("Failed to load " + path, failure.get());
            }
        }
    }
}
