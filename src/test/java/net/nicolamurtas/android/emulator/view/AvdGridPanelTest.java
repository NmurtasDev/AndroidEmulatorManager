package net.nicolamurtas.android.emulator.view;

import net.nicolamurtas.android.emulator.service.EmulatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.awt.GraphicsEnvironment;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AvdGridPanel.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class AvdGridPanelTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_CanBeInstantiated() {
        assertDoesNotThrow(() -> {
            AvdGridPanel panel = new AvdGridPanel();
            assertNotNull(panel);
        });
    }

    @Test
    void testAvdGridPanel_HeadlessEnvironment() {
        if (GraphicsEnvironment.isHeadless()) {
            assertThrows(java.awt.HeadlessException.class, () -> {
                new AvdGridPanel();
            });
        } else {
            assertDoesNotThrow(() -> {
                new AvdGridPanel();
            });
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_UpdateAvdList_EmptyList() {
        AvdGridPanel panel = new AvdGridPanel();

        assertDoesNotThrow(() -> {
            panel.updateAvdList(Collections.emptyList());
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_UpdateAvdList_Null() {
        AvdGridPanel panel = new AvdGridPanel();

        assertDoesNotThrow(() -> {
            panel.updateAvdList(null);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_SetOnCreateAvd() {
        AvdGridPanel panel = new AvdGridPanel();

        boolean[] called = {false};
        panel.setOnCreateAvd(() -> called[0] = true);

        assertNotNull(panel);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_SetOnRefresh() {
        AvdGridPanel panel = new AvdGridPanel();

        boolean[] called = {false};
        panel.setOnRefresh(() -> called[0] = true);

        assertNotNull(panel);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_SetOnStartEmulator() {
        AvdGridPanel panel = new AvdGridPanel();

        String[] capturedName = {null};
        panel.setOnStartEmulator(name -> capturedName[0] = name);

        assertNotNull(panel);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_SetOnStopEmulator() {
        AvdGridPanel panel = new AvdGridPanel();

        String[] capturedName = {null};
        panel.setOnStopEmulator(name -> capturedName[0] = name);

        assertNotNull(panel);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_SetOnRenameAvd() {
        AvdGridPanel panel = new AvdGridPanel();

        String[] capturedName = {null};
        panel.setOnRenameAvd(name -> capturedName[0] = name);

        assertNotNull(panel);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_SetOnDeleteAvd() {
        AvdGridPanel panel = new AvdGridPanel();

        String[] capturedName = {null};
        panel.setOnDeleteAvd(name -> capturedName[0] = name);

        assertNotNull(panel);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_SetEmulatorService() {
        AvdGridPanel panel = new AvdGridPanel();

        assertDoesNotThrow(() -> {
            panel.setEmulatorService(null);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_GetAllAvds_Empty() {
        AvdGridPanel panel = new AvdGridPanel();

        List<EmulatorService.AvdInfo> avds = panel.getAllAvds();
        assertNotNull(avds);
        assertTrue(avds.isEmpty());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_SetAllCallbacks_Null() {
        AvdGridPanel panel = new AvdGridPanel();

        assertDoesNotThrow(() -> {
            panel.setOnCreateAvd(null);
            panel.setOnRefresh(null);
            panel.setOnStartEmulator(null);
            panel.setOnStopEmulator(null);
            panel.setOnRenameAvd(null);
            panel.setOnDeleteAvd(null);
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_UpdateAvdList_Multiple() {
        AvdGridPanel panel = new AvdGridPanel();

        assertDoesNotThrow(() -> {
            panel.updateAvdList(Collections.emptyList());
            panel.updateAvdList(Collections.emptyList());
            panel.updateAvdList(Collections.emptyList());
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testAvdGridPanel_MultipleCallbackSettings() {
        AvdGridPanel panel = new AvdGridPanel();

        assertDoesNotThrow(() -> {
            panel.setOnCreateAvd(() -> {});
            panel.setOnCreateAvd(() -> {});
            panel.setOnCreateAvd(null);
        });
    }
}
