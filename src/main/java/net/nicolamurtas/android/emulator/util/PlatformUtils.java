package net.nicolamurtas.android.emulator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for platform-specific operations and OS detection.
 */
public class PlatformUtils {
    private static final Logger logger = LoggerFactory.getLogger(PlatformUtils.class);

    public enum OperatingSystem {
        WINDOWS,
        LINUX,
        MACOS,
        UNKNOWN
    }

    private static OperatingSystem cachedOS;

    /**
     * Detects the current operating system.
     */
    public static OperatingSystem getOperatingSystem() {
        if (cachedOS != null) {
            return cachedOS;
        }

        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            cachedOS = OperatingSystem.WINDOWS;
        } else if (osName.contains("mac")) {
            cachedOS = OperatingSystem.MACOS;
        } else if (osName.contains("nux") || osName.contains("nix")) {
            cachedOS = OperatingSystem.LINUX;
        } else {
            cachedOS = OperatingSystem.UNKNOWN;
        }

        logger.info("Detected operating system: {}", cachedOS);
        return cachedOS;
    }

    /**
     * Returns true if running on Windows.
     */
    public static boolean isWindows() {
        return getOperatingSystem() == OperatingSystem.WINDOWS;
    }

    /**
     * Returns true if running on Linux.
     */
    public static boolean isLinux() {
        return getOperatingSystem() == OperatingSystem.LINUX;
    }

    /**
     * Returns true if running on macOS.
     */
    public static boolean isMacOS() {
        return getOperatingSystem() == OperatingSystem.MACOS;
    }

    /**
     * Gets the default Android SDK installation path for the current OS.
     */
    public static Path getDefaultSdkPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "Android", "sdk");
    }

    /**
     * Gets the appropriate executable extension for the current OS.
     * Returns ".bat" for Windows, empty string for Unix-like systems.
     */
    public static String getExecutableExtension() {
        return isWindows() ? ".bat" : "";
    }

    /**
     * Gets the executable suffix (e.g., ".exe" for Windows binaries).
     */
    public static String getBinaryExtension() {
        return isWindows() ? ".exe" : "";
    }

    /**
     * Makes a file executable on Unix-like systems.
     * No-op on Windows.
     */
    public static void makeExecutable(Path filePath) throws IOException {
        if (isWindows()) {
            return; // Windows doesn't need chmod
        }

        if (!Files.exists(filePath)) {
            throw new IOException("File does not exist: " + filePath);
        }

        try {
            // Use ProcessBuilder for secure execution
            ProcessBuilder pb = new ProcessBuilder("chmod", "+x", filePath.toString());
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Failed to make file executable: " + filePath);
            }

            logger.debug("Made file executable: {}", filePath);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while making file executable", e);
        }
    }

    /**
     * Makes all files in a directory executable recursively.
     */
    public static void makeDirectoryExecutable(Path directory) throws IOException {
        if (isWindows()) {
            return;
        }

        if (!Files.isDirectory(directory)) {
            throw new IOException("Not a directory: " + directory);
        }

        Files.walk(directory)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try {
                    makeExecutable(file);
                } catch (IOException e) {
                    logger.warn("Failed to make file executable: {}", file, e);
                }
            });
    }

    /**
     * Validates that a path is writable.
     */
    public static boolean isPathWritable(Path path) {
        try {
            if (Files.exists(path)) {
                return Files.isWritable(path);
            } else {
                // Try to create the directory to verify write permissions
                Files.createDirectories(path);
                boolean writable = Files.isWritable(path);
                Files.deleteIfExists(path);
                return writable;
            }
        } catch (IOException e) {
            logger.debug("Path is not writable: {}", path, e);
            return false;
        }
    }

    /**
     * Gets the Android SDK Command Line Tools download URL for the current OS.
     */
    public static String getSdkToolsDownloadUrl() {
        return switch (getOperatingSystem()) {
            case WINDOWS -> "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip";
            case MACOS -> "https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip";
            case LINUX -> "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip";
            default -> throw new UnsupportedOperationException("Unsupported operating system");
        };
    }

    /**
     * Gets the filename for the downloaded SDK tools archive.
     */
    public static String getSdkToolsFileName() {
        return switch (getOperatingSystem()) {
            case WINDOWS -> "commandlinetools-win.zip";
            case MACOS -> "commandlinetools-mac.zip";
            case LINUX -> "commandlinetools-linux.zip";
            default -> "commandlinetools.zip";
        };
    }
}
