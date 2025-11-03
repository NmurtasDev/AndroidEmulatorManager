package net.nicolamurtas.android.emulator.util;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ThemeUtils utility class.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class ThemeUtilsTest {

    @Test
    void testIsDarkTheme_DoesNotThrow() {
        // This test just verifies the method doesn't throw exceptions
        // Actual result depends on UIManager state
        assertDoesNotThrow(() -> ThemeUtils.isDarkTheme());
    }

    @Test
    void testGetHeaderBackgroundColor_ReturnsNonNull() {
        // Verify method returns a non-null color
        Color headerColor = ThemeUtils.getHeaderBackgroundColor();
        assertNotNull(headerColor);
    }

    @Test
    void testGetHeaderBackgroundColor_DoesNotThrow() {
        // Verify method doesn't throw exceptions
        assertDoesNotThrow(() -> ThemeUtils.getHeaderBackgroundColor());
    }

    @Test
    void testColors_SuccessColor() {
        Color success = ThemeUtils.Colors.SUCCESS;
        assertNotNull(success);
        assertEquals(76, success.getRed());
        assertEquals(175, success.getGreen());
        assertEquals(80, success.getBlue());
        assertEquals(255, success.getAlpha());
    }

    @Test
    void testColors_WarningColor() {
        Color warning = ThemeUtils.Colors.WARNING;
        assertNotNull(warning);
        assertEquals(255, warning.getRed());
        assertEquals(152, warning.getGreen());
        assertEquals(0, warning.getBlue());
        assertEquals(255, warning.getAlpha());
    }

    @Test
    void testColors_ErrorColor() {
        Color error = ThemeUtils.Colors.ERROR;
        assertNotNull(error);
        assertEquals(244, error.getRed());
        assertEquals(67, error.getGreen());
        assertEquals(54, error.getBlue());
        assertEquals(255, error.getAlpha());
    }

    @Test
    void testColors_InfoColor() {
        Color info = ThemeUtils.Colors.INFO;
        assertNotNull(info);
        assertEquals(33, info.getRed());
        assertEquals(150, info.getGreen());
        assertEquals(243, info.getBlue());
        assertEquals(255, info.getAlpha());
    }

    @Test
    void testColors_AllColorsAreOpaque() {
        // All standard colors should be fully opaque (alpha = 255)
        assertEquals(255, ThemeUtils.Colors.SUCCESS.getAlpha());
        assertEquals(255, ThemeUtils.Colors.WARNING.getAlpha());
        assertEquals(255, ThemeUtils.Colors.ERROR.getAlpha());
        assertEquals(255, ThemeUtils.Colors.INFO.getAlpha());
    }

    @Test
    void testColors_MaterialDesignCompliance() {
        // These colors follow Material Design color palette
        // Green 500
        assertEquals(new Color(76, 175, 80), ThemeUtils.Colors.SUCCESS);

        // Orange 500
        assertEquals(new Color(255, 152, 0), ThemeUtils.Colors.WARNING);

        // Red 500
        assertEquals(new Color(244, 67, 54), ThemeUtils.Colors.ERROR);

        // Blue 500
        assertEquals(new Color(33, 150, 243), ThemeUtils.Colors.INFO);
    }

    @Test
    void testUtilityClassCannotBeInstantiated() {
        // Verify constructor throws exception
        try {
            var constructor = ThemeUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
        } catch (NoSuchMethodException e) {
            fail("ThemeUtils should have a private constructor");
        }
    }

    @Test
    void testColorsClassCannotBeInstantiated() {
        // Verify Colors inner class constructor throws exception
        try {
            var constructor = ThemeUtils.Colors.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
        } catch (NoSuchMethodException e) {
            fail("ThemeUtils.Colors should have a private constructor");
        }
    }

    @Test
    void testHeaderBackgroundColor_ConsistentBetweenCalls() {
        // Multiple calls should return consistent results
        Color color1 = ThemeUtils.getHeaderBackgroundColor();
        Color color2 = ThemeUtils.getHeaderBackgroundColor();

        // Colors should be equal (same RGB values)
        assertEquals(color1.getRed(), color2.getRed());
        assertEquals(color1.getGreen(), color2.getGreen());
        assertEquals(color1.getBlue(), color2.getBlue());
    }
}
