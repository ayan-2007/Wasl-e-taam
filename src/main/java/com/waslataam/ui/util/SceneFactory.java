package com.waslataam.ui.util;

import javafx.scene.Parent;
import javafx.scene.Scene;

/** Creates scenes with the theme already attached. */
public final class SceneFactory {
    private SceneFactory() {
    }

    public static Scene create(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        ThemeManager.apply(scene);
        return scene;
    }
}
