package net.nicolamurtas.android.emulator.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MainController.
 *
 * Note: Most tests verify basic instantiation and structure.
 * Full integration testing would require a headless display
 * environment or extensive mocking.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class MainControllerTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainController_CanBeInstantiated() {
        // This test only runs if a display is available
        assertDoesNotThrow(() -> {
            MainController controller = new MainController();
            assertNotNull(controller);
            assertNotNull(controller.getView());
        });
    }

    @Test
    void testMainController_InstantiationWithoutDisplay() {
        // In headless mode, controller creation might fail due to Swing dependencies
        // This is expected behavior and not a bug
        // We just verify it doesn't throw unexpected exceptions types

        try {
            MainController controller = new MainController();
            // If we get here, display was available
            assertNotNull(controller);
        } catch (java.awt.HeadlessException e) {
            // Expected in headless environments (CI/CD)
            assertTrue(GraphicsEnvironment.isHeadless());
        } catch (Exception e) {
            // Any other exception should be investigated
            fail("Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Test
    void testMainController_ViewNotNull() {
        try {
            MainController controller = new MainController();
            assertNotNull(controller.getView(), "View should not be null after initialization");
        } catch (java.awt.HeadlessException e) {
            // Expected in headless environments
            assertTrue(GraphicsEnvironment.isHeadless());
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainController_ShowDoesNotThrow() {
        assertDoesNotThrow(() -> {
            MainController controller = new MainController();
            controller.show();
            // Note: We can't easily verify the window is visible without
            // complex GUI testing frameworks
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainController_ViewComponentsInitialized() {
        MainController controller = new MainController();
        var view = controller.getView();

        assertNotNull(view.getSdkConfigPanel(), "SDK config panel should be initialized");
        assertNotNull(view.getAvdGridPanel(), "AVD grid panel should be initialized");
        assertNotNull(view.getLogPanel(), "Log panel should be initialized");
    }

    @Test
    void testMainController_HeadlessEnvironmentDetection() {
        boolean isHeadless = GraphicsEnvironment.isHeadless();

        if (isHeadless) {
            assertThrows(java.awt.HeadlessException.class, () -> {
                new MainController();
            }, "Should throw HeadlessException in headless environment");
        } else {
            assertDoesNotThrow(() -> {
                MainController controller = new MainController();
                assertNotNull(controller);
            }, "Should not throw in non-headless environment");
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testMainController_MultipleInstances() {
        // Test that multiple controllers can be created
        assertDoesNotThrow(() -> {
            MainController controller1 = new MainController();
            MainController controller2 = new MainController();

            assertNotNull(controller1);
            assertNotNull(controller2);
            assertNotSame(controller1, controller2);
            assertNotSame(controller1.getView(), controller2.getView());
        });
    }

    @Test
    void testMainController_ClassStructure() {
        // Verify class has required methods
        assertDoesNotThrow(() -> {
            var showMethod = MainController.class.getMethod("show");
            var getViewMethod = MainController.class.getMethod("getView");

            assertNotNull(showMethod);
            assertNotNull(getViewMethod);

            assertEquals(void.class, showMethod.getReturnType());
            assertNotNull(getViewMethod.getReturnType());
        });
    }

    @Test
    void testMainController_HasPublicConstructor() {
        var constructors = MainController.class.getConstructors();
        assertTrue(constructors.length > 0, "Should have at least one public constructor");

        boolean hasNoArgsConstructor = false;
        for (var constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                hasNoArgsConstructor = true;
                break;
            }
        }
        assertTrue(hasNoArgsConstructor, "Should have a no-args constructor");
    }
}
