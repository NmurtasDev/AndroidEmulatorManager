package net.nicolamurtas.android.emulator.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmulatorStatus enum.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class EmulatorStatusTest {

    @Test
    void testAllStatusValuesExist() {
        EmulatorStatus[] statuses = EmulatorStatus.values();
        assertEquals(5, statuses.length);
    }

    @Test
    void testStatusValueOf() {
        assertEquals(EmulatorStatus.STOPPED, EmulatorStatus.valueOf("STOPPED"));
        assertEquals(EmulatorStatus.STARTING, EmulatorStatus.valueOf("STARTING"));
        assertEquals(EmulatorStatus.RUNNING, EmulatorStatus.valueOf("RUNNING"));
        assertEquals(EmulatorStatus.STOPPING, EmulatorStatus.valueOf("STOPPING"));
        assertEquals(EmulatorStatus.ERROR, EmulatorStatus.valueOf("ERROR"));
    }

    @Test
    void testStatusNames() {
        assertEquals("STOPPED", EmulatorStatus.STOPPED.name());
        assertEquals("STARTING", EmulatorStatus.STARTING.name());
        assertEquals("RUNNING", EmulatorStatus.RUNNING.name());
        assertEquals("STOPPING", EmulatorStatus.STOPPING.name());
        assertEquals("ERROR", EmulatorStatus.ERROR.name());
    }

    @Test
    void testStatusOrdinal() {
        assertEquals(0, EmulatorStatus.STOPPED.ordinal());
        assertEquals(1, EmulatorStatus.STARTING.ordinal());
        assertEquals(2, EmulatorStatus.RUNNING.ordinal());
        assertEquals(3, EmulatorStatus.STOPPING.ordinal());
        assertEquals(4, EmulatorStatus.ERROR.ordinal());
    }

    @Test
    void testInvalidValueOfThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EmulatorStatus.valueOf("INVALID");
        });
    }

    @Test
    void testEnumEquality() {
        EmulatorStatus status1 = EmulatorStatus.RUNNING;
        EmulatorStatus status2 = EmulatorStatus.RUNNING;
        EmulatorStatus status3 = EmulatorStatus.STOPPED;

        assertEquals(status1, status2);
        assertNotEquals(status1, status3);
        assertSame(status1, status2); // Enums are singletons
    }

    @Test
    void testEnumInSwitchStatement() {
        // Test that enum can be used in switch statements
        String result = switch (EmulatorStatus.RUNNING) {
            case STOPPED -> "not running";
            case STARTING -> "starting up";
            case RUNNING -> "active";
            case STOPPING -> "shutting down";
            case ERROR -> "failed";
        };

        assertEquals("active", result);
    }
}
