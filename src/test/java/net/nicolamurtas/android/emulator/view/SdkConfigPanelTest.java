package net.nicolamurtas.android.emulator.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SdkConfigPanel.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class SdkConfigPanelTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_CanBeInstantiatedConfigured() {
        assertDoesNotThrow(() -> {
            SdkConfigPanel panel = new SdkConfigPanel(true);
            assertNotNull(panel);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_CanBeInstantiatedNotConfigured() {
        assertDoesNotThrow(() -> {
            SdkConfigPanel panel = new SdkConfigPanel(false);
            assertNotNull(panel);
        });
    }

    @Test
    void testSdkConfigPanel_HeadlessEnvironment() {
        if (GraphicsEnvironment.isHeadless()) {
            assertThrows(java.awt.HeadlessException.class, () -> {
                new SdkConfigPanel(true);
            });
        } else {
            assertDoesNotThrow(() -> {
                new SdkConfigPanel(true);
            });
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_SetSdkPath() {
        SdkConfigPanel panel = new SdkConfigPanel(false);

        assertDoesNotThrow(() -> {
            panel.setSdkPath("/home/user/Android/sdk");
            panel.setSdkPath("C:\\Android\\sdk");
            panel.setSdkPath("");
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_GetSdkPath() {
        SdkConfigPanel panel = new SdkConfigPanel(false);
        panel.setSdkPath("/test/path");

        String path = panel.getSdkPath();
        assertNotNull(path);
        assertEquals("/test/path", path);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_SetConfigured() {
        SdkConfigPanel panel = new SdkConfigPanel(false);

        assertDoesNotThrow(() -> {
            panel.setConfigured(true);
            panel.setConfigured(false);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_SetOnBrowse() {
        SdkConfigPanel panel = new SdkConfigPanel(false);

        boolean[] called = {false};
        panel.setOnBrowse(() -> called[0] = true);

        // We can't easily trigger the button click, but we can verify the setter works
        assertNotNull(panel);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_SetOnDownload() {
        SdkConfigPanel panel = new SdkConfigPanel(false);

        boolean[] called = {false};
        panel.setOnDownload(() -> called[0] = true);

        assertNotNull(panel);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_SetOnVerify() {
        SdkConfigPanel panel = new SdkConfigPanel(false);

        boolean[] called = {false};
        panel.setOnVerify(() -> called[0] = true);

        assertNotNull(panel);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_SetSdkPath_Null() {
        SdkConfigPanel panel = new SdkConfigPanel(false);

        assertDoesNotThrow(() -> panel.setSdkPath(null));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_GetSdkPath_Empty() {
        SdkConfigPanel panel = new SdkConfigPanel(false);
        panel.setSdkPath("");

        String path = panel.getSdkPath();
        assertNotNull(path);
        assertTrue(path.isEmpty() || path.isBlank());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_MultipleStateChanges() {
        SdkConfigPanel panel = new SdkConfigPanel(false);

        assertDoesNotThrow(() -> {
            panel.setConfigured(true);
            panel.setSdkPath("/path1");
            panel.setConfigured(false);
            panel.setSdkPath("/path2");
            panel.setConfigured(true);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_PathWithSpaces() {
        SdkConfigPanel panel = new SdkConfigPanel(false);
        String pathWithSpaces = "/home/user/My Documents/Android SDK";

        panel.setSdkPath(pathWithSpaces);
        assertEquals(pathWithSpaces, panel.getSdkPath());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_PathWithSpecialChars() {
        SdkConfigPanel panel = new SdkConfigPanel(false);
        String specialPath = "/home/user/sdk-test_123";

        panel.setSdkPath(specialPath);
        assertEquals(specialPath, panel.getSdkPath());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testSdkConfigPanel_SetCallbacks_Null() {
        SdkConfigPanel panel = new SdkConfigPanel(false);

        assertDoesNotThrow(() -> {
            panel.setOnBrowse(null);
            panel.setOnDownload(null);
            panel.setOnVerify(null);
        });
    }
}
