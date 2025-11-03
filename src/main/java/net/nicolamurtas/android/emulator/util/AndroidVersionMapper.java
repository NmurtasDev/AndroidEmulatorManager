package net.nicolamurtas.android.emulator.util;

/**
 * Utility class for mapping Android API levels to human-readable version names.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public final class AndroidVersionMapper {

    private AndroidVersionMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts API level to Android version name.
     *
     * @param apiLevel The API level as a string (e.g., "35", "34")
     * @return Android version name (e.g., "Android 15", "Android 14")
     */
    public static String getAndroidVersionName(String apiLevel) {
        if (apiLevel == null || apiLevel.equals("Unknown")) {
            return "Android (Unknown)";
        }

        return switch (apiLevel) {
            case "36" -> "Android 16";
            case "35" -> "Android 15";
            case "34" -> "Android 14";
            case "33" -> "Android 13";
            case "32" -> "Android 12L";
            case "31" -> "Android 12";
            case "30" -> "Android 11";
            case "29" -> "Android 10";
            case "28" -> "Android 9";
            case "27" -> "Android 8.1";
            case "26" -> "Android 8.0";
            case "25" -> "Android 7.1";
            case "24" -> "Android 7.0";
            case "23" -> "Android 6.0";
            case "22" -> "Android 5.1";
            case "21" -> "Android 5.0";
            default -> "Android API " + apiLevel;
        };
    }

    /**
     * Extracts API level from AVD config.ini file path.
     * More reliable than parsing the target string.
     *
     * @param configContent Content of the config.ini file
     * @return API level as a string, or "Unknown" if not found
     */
    public static String extractApiLevelFromConfig(String configContent) {
        if (configContent == null) {
            return "Unknown";
        }

        // Look for image.sysdir.1 = system-images/android-35/google_apis/x86_64/
        for (String line : configContent.split("\n")) {
            line = line.trim();
            if (line.startsWith("image.sysdir.1")) {
                // Parse "image.sysdir.1 = system-images/android-35/google_apis/x86_64/"
                int equalPos = line.indexOf('=');
                if (equalPos > 0) {
                    String sysdir = line.substring(equalPos + 1).trim();
                    // Extract API number from: system-images/android-35/google_apis/x86_64/
                    String[] parts = sysdir.split("/");
                    for (String part : parts) {
                        if (part.startsWith("android-")) {
                            return part.substring(8); // Remove "android-" prefix
                        }
                    }
                }
            }
        }

        return "Unknown";
    }

    /**
     * Extracts API level from target string (fallback method).
     *
     * @param target Target string (e.g., "Android X.Y (API level Z)")
     * @return API level as a string, or "Unknown" if not found
     */
    public static String extractApiLevelFromTarget(String target) {
        if (target == null) {
            return "Unknown";
        }

        // Target format: "Android X.Y (API level Z)" or similar
        if (target.contains("API level")) {
            int start = target.indexOf("API level") + 10;
            int end = target.indexOf(")", start);
            if (end > start) {
                return target.substring(start, end).trim();
            }
        }

        // Try to extract just the number
        String[] parts = target.split("\\s+");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                return part;
            }
        }

        return "Unknown";
    }
}
