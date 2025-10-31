package net.nicolamurtas.android.emulator.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MainView.
 *
 * Note: Most tests are conditional on having a display available,
 * as MainView is a Swing component that requires a graphics environment.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class MainViewTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_CanBeInstantiatedWhenSdkConfigured() {
        assertDoesNotThrow(() -> {
            MainView view = new MainView(true);
            assertNotNull(view);
            assertNotNull(view.getSdkConfigPanel());
            assertNotNull(view.getAvdGridPanel());
            assertNotNull(view.getLogPanel());
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_CanBeInstantiatedWhenSdkNotConfigured() {
        assertDoesNotThrow(() -> {
            MainView view = new MainView(false);
            assertNotNull(view);
            assertNotNull(view.getSdkConfigPanel());
            assertNotNull(view.getAvdGridPanel());
            assertNotNull(view.getLogPanel());
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_ComponentsNotNull() {
        MainView view = new MainView(true);

        assertNotNull(view.getSdkConfigPanel(), "SDK config panel should not be null");
        assertNotNull(view.getAvdGridPanel(), "AVD grid panel should not be null");
        assertNotNull(view.getLogPanel(), "Log panel should not be null");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_LogFunctionality() {
        MainView view = new MainView(true);

        assertDoesNotThrow(() -> {
            view.log("Test message 1");
            view.log("Test message 2");
            view.log("Test message 3");
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_ProgressBarFunctionality() {
        MainView view = new MainView(true);

        assertDoesNotThrow(() -> {
            view.showProgress(true);
            view.updateProgress(50, "Test progress");
            view.showProgress(false);
        });
    }

    @Test
    void testMainView_HeadlessEnvironmentHandling() {
        if (GraphicsEnvironment.isHeadless()) {
            assertThrows(java.awt.HeadlessException.class, () -> {
                new MainView(true);
            });
        } else {
            // If not headless, should work fine
            assertDoesNotThrow(() -> {
                new MainView(true);
            });
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_LogEmptyMessage() {
        MainView view = new MainView(true);
        assertDoesNotThrow(() -> view.log(""));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_LogNullMessage() {
        MainView view = new MainView(true);
        assertDoesNotThrow(() -> view.log(null));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_ProgressWithZeroValue() {
        MainView view = new MainView(true);
        assertDoesNotThrow(() -> {
            view.showProgress(true);
            view.updateProgress(0, "Starting");
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_ProgressWithHundredValue() {
        MainView view = new MainView(true);
        assertDoesNotThrow(() -> {
            view.showProgress(true);
            view.updateProgress(100, "Complete");
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_MultipleLogCalls() {
        MainView view = new MainView(true);
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                view.log("Message " + i);
            }
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainView_ProgressUpdatesSequentially() {
        MainView view = new MainView(true);
        assertDoesNotThrow(() -> {
            view.showProgress(true);
            for (int i = 0; i <= 100; i += 10) {
                view.updateProgress(i, "Progress: " + i + "%");
            }
            view.showProgress(false);
        });
    }
}
