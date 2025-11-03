package net.nicolamurtas.android.emulator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AndroidEmulatorManager main class.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class AndroidEmulatorManagerTest {

    @Test
    void testMainClassExists() {
        // Verify the main class exists and can be loaded
        assertDoesNotThrow(() -> {
            Class<?> mainClass = Class.forName("net.nicolamurtas.android.emulator.AndroidEmulatorManager");
            assertNotNull(mainClass);
        });
    }

    @Test
    void testMainMethodExists() throws NoSuchMethodException {
        // Verify the main method exists with correct signature
        Class<?> mainClass = AndroidEmulatorManager.class;
        var mainMethod = mainClass.getMethod("main", String[].class);
        assertNotNull(mainMethod);
        assertEquals(void.class, mainMethod.getReturnType());
    }

    @Test
    void testMainMethodIsPublicStatic() throws NoSuchMethodException {
        var mainMethod = AndroidEmulatorManager.class.getMethod("main", String[].class);
        assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()));
    }

    @Test
    void testClassIsPublic() {
        assertTrue(java.lang.reflect.Modifier.isPublic(AndroidEmulatorManager.class.getModifiers()));
    }

    @Test
    void testClassHasNoArgsConstructor() {
        assertDoesNotThrow(() -> {
            var constructor = AndroidEmulatorManager.class.getDeclaredConstructor();
            assertNotNull(constructor);
        });
    }
}
