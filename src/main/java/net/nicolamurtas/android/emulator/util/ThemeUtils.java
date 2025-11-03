package net.nicolamurtas.android.emulator.util;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for theme detection and color management.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public final class ThemeUtils {

    private ThemeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Detects if the current system theme is dark.
     * Uses panel background brightness to determine theme.
     *
     * @return true if dark theme, false otherwise
     */
    public static boolean isDarkTheme() {
        Color bg = UIManager.getColor("Panel.background");
        if (bg == null) {
            return false;
        }

        // Calculate perceived brightness using standard formula
        int brightness = (int) Math.sqrt(
            bg.getRed() * bg.getRed() * 0.241 +
            bg.getGreen() * bg.getGreen() * 0.691 +
            bg.getBlue() * bg.getBlue() * 0.068
        );

        return brightness < 130; // Dark theme if brightness < 130
    }

    /**
     * Gets a header background color based on the current theme.
     * Returns a slightly darker color for light themes and brighter for dark themes.
     *
     * @return Header background color
     */
    public static Color getHeaderBackgroundColor() {
        Color panelBg = UIManager.getColor("Panel.background");
        Color headerBg = panelBg != null ?
            (isDarkTheme() ? panelBg.brighter() : panelBg.darker()) :
            new Color(240, 240, 240);
        return headerBg;
    }

    /**
     * Standard color constants for UI elements.
     */
    public static final class Colors {
        public static final Color SUCCESS = new Color(76, 175, 80);
        public static final Color WARNING = new Color(255, 152, 0);
        public static final Color ERROR = new Color(244, 67, 54);
        public static final Color INFO = new Color(33, 150, 243);

        private Colors() {
            throw new UnsupportedOperationException("Constants class cannot be instantiated");
        }
    }
}
