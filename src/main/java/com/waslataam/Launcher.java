package com.waslataam;

/**
 * Plain entry point used by the packaged JAR and IDE run configurations.
 *
 * <p>The JDK launcher refuses to start a main class that extends
 * {@link javafx.application.Application} when JavaFX is on the classpath
 * instead of the module path ("JavaFX runtime components are missing").
 * Delegating from a non-Application class avoids that check while keeping
 * {@link Main} as the real JavaFX application.</p>
 */
public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}
