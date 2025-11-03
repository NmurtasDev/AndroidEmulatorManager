package net.nicolamurtas.android.emulator.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LogPanel.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class LogPanelTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_CanBeInstantiated() {
        assertDoesNotThrow(() -> {
            LogPanel logPanel = new LogPanel();
            assertNotNull(logPanel);
        });
    }

    @Test
    void testLogPanel_HeadlessEnvironment() {
        if (GraphicsEnvironment.isHeadless()) {
            assertThrows(java.awt.HeadlessException.class, () -> {
                new LogPanel();
            });
        } else {
            assertDoesNotThrow(() -> {
                new LogPanel();
            });
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_AddLog() {
        LogPanel logPanel = new LogPanel();
        assertDoesNotThrow(() -> {
            logPanel.addLog("Test message");
            logPanel.addLog("Another message");
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_AddLog_NullMessage() {
        LogPanel logPanel = new LogPanel();
        assertDoesNotThrow(() -> logPanel.addLog(null));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_AddLog_EmptyMessage() {
        LogPanel logPanel = new LogPanel();
        assertDoesNotThrow(() -> logPanel.addLog(""));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_ClearLogs() {
        LogPanel logPanel = new LogPanel();
        logPanel.addLog("Message 1");
        logPanel.addLog("Message 2");

        assertDoesNotThrow(() -> logPanel.clearLogs());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_SetOnClear() {
        LogPanel logPanel = new LogPanel();

        boolean[] called = {false};
        logPanel.setOnClear(() -> called[0] = true);

        // Trigger clear manually
        assertDoesNotThrow(() -> logPanel.clearLogs());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_MultipleMessages() {
        LogPanel logPanel = new LogPanel();

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                logPanel.addLog("Message " + i);
            }
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_LongMessage() {
        LogPanel logPanel = new LogPanel();

        String longMessage = "A".repeat(1000);
        assertDoesNotThrow(() -> logPanel.addLog(longMessage));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_ClearEmpty() {
        LogPanel logPanel = new LogPanel();
        // Clearing empty logs should not throw
        assertDoesNotThrow(() -> logPanel.clearLogs());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".*")
    void testLogPanel_SpecialCharacters() {
        LogPanel logPanel = new LogPanel();

        assertDoesNotThrow(() -> {
            logPanel.addLog("Message with émojis 🚀");
            logPanel.addLog("Message with tabs\t\tand\nnewlines");
            logPanel.addLog("Message with special chars: @#$%^&*()");
        });
    }
}
