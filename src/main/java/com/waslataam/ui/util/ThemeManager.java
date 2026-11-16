package com.waslataam.ui.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;

/**
 * Applies the application stylesheet and the optional dark token set to every
 * scene. JavaFX has no prefers-color-scheme media query, so the in-app toggle
 * is the supported appearance switch.
 */
public final class ThemeManager {
    public static final String THEME_RESOURCE =
            "/com/waslataam/ui/resources/css/theme.css";

    private static final BooleanProperty DARK = new SimpleBooleanProperty(false);

    private ThemeManager() {
    }

    public static void apply(Scene scene) {
        String theme = ThemeManager.class.getResource(THEME_RESOURCE).toExternalForm();
        if (!scene.getStylesheets().contains(theme)) {
            scene.getStylesheets().add(theme);
        }
        applyMode(scene);
    }

    public static void toggle(Scene scene) {
        DARK.set(!DARK.get());
        applyMode(scene);
    }

    public static boolean isDark() {
        return DARK.get();
    }

    public static BooleanProperty darkProperty() {
        return DARK;
    }

    public static String stylesheetUrl() {
        return ThemeManager.class.getResource(THEME_RESOURCE).toExternalForm();
    }

    private static void applyMode(Scene scene) {
        if (scene.getRoot() == null) {
            return;
        }
        scene.getRoot().getStyleClass().remove("dark");
        if (DARK.get()) {
            scene.getRoot().getStyleClass().add("dark");
        }
    }
}
