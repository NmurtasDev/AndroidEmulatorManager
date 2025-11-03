package net.nicolamurtas.android.emulator.util;

/**
 * Utility class for formatting device names for display.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public final class DeviceNameFormatter {

    private DeviceNameFormatter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Formats device name for display.
     * Example: "pixel_7" -> "Pixel 7", "pixel" -> "Pixel"
     *
     * @param deviceName Raw device name (e.g., "pixel_7")
     * @return Formatted device name (e.g., "Pixel 7")
     */
    public static String formatDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isEmpty()) {
            return deviceName;
        }

        // Replace underscores with spaces and capitalize words
        String[] parts = deviceName.replace("_", " ").split(" ");
        StringBuilder formatted = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                formatted.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    formatted.append(part.substring(1));
                }
                formatted.append(" ");
            }
        }

        return formatted.toString().trim();
    }

    /**
     * Validates AVD name to ensure it doesn't contain spaces or invalid characters.
     * AVD names should only contain letters, numbers, underscores, and hyphens.
     *
     * @param name AVD name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAvdName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        // Check for spaces
        if (name.contains(" ")) {
            return false;
        }

        // AVD names should only contain: letters, numbers, underscores, hyphens
        // Pattern: ^[a-zA-Z0-9_-]+$
        return name.matches("^[a-zA-Z0-9_-]+$");
    }
}
