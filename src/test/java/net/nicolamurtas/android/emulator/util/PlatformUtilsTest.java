package net.nicolamurtas.android.emulator.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlatformUtils.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class PlatformUtilsTest {

    @Test
    void testGetOperatingSystem() {
        PlatformUtils.OperatingSystem os = PlatformUtils.getOperatingSystem();
        assertNotNull(os);
        // Should be one of the known OSes
        assertTrue(os == PlatformUtils.OperatingSystem.WINDOWS ||
                   os == PlatformUtils.OperatingSystem.LINUX ||
                   os == PlatformUtils.OperatingSystem.MACOS ||
                   os == PlatformUtils.OperatingSystem.UNKNOWN);
    }

    @Test
    void testGetOperatingSystem_IsCached() {
        PlatformUtils.OperatingSystem os1 = PlatformUtils.getOperatingSystem();
        PlatformUtils.OperatingSystem os2 = PlatformUtils.getOperatingSystem();
        assertSame(os1, os2, "Operating system should be cached");
    }

    @Test
    void testIsWindows() {
        boolean isWindows = PlatformUtils.isWindows();
        String osName = System.getProperty("os.name").toLowerCase();
        assertEquals(osName.contains("win"), isWindows);
    }

    @Test
    void testIsLinux() {
        boolean isLinux = PlatformUtils.isLinux();
        String osName = System.getProperty("os.name").toLowerCase();
        assertEquals(osName.contains("nux") || osName.contains("nix"), isLinux);
    }

    @Test
    void testIsMacOS() {
        boolean isMacOS = PlatformUtils.isMacOS();
        String osName = System.getProperty("os.name").toLowerCase();
        assertEquals(osName.contains("mac"), isMacOS);
    }

    @Test
    void testGetDefaultSdkPath() {
        Path sdkPath = PlatformUtils.getDefaultSdkPath();
        assertNotNull(sdkPath);

        String userHome = System.getProperty("user.home");
        Path expected = Paths.get(userHome, "Android", "sdk");
        assertEquals(expected, sdkPath);
    }

    @Test
    void testGetExecutableExtension() {
        String extension = PlatformUtils.getExecutableExtension();
        assertNotNull(extension);

        if (PlatformUtils.isWindows()) {
            assertEquals(".bat", extension);
        } else {
            assertEquals("", extension);
        }
    }

    @Test
    void testGetBinaryExtension() {
        String extension = PlatformUtils.getBinaryExtension();
        assertNotNull(extension);

        if (PlatformUtils.isWindows()) {
            assertEquals(".exe", extension);
        } else {
            assertEquals("", extension);
        }
    }

    @Test
    void testMakeExecutable_NonExistentFile(@TempDir Path tempDir) {
        Path nonExistent = tempDir.resolve("nonexistent.sh");

        if (PlatformUtils.isWindows()) {
            // Should be no-op on Windows
            assertDoesNotThrow(() -> PlatformUtils.makeExecutable(nonExistent));
        } else {
            // Should throw on Unix-like systems
            assertThrows(IOException.class, () -> PlatformUtils.makeExecutable(nonExistent));
        }
    }

    @Test
    void testMakeExecutable_ExistingFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.sh");
        Files.writeString(testFile, "#!/bin/bash\necho test");

        if (PlatformUtils.isWindows()) {
            // Should be no-op on Windows
            assertDoesNotThrow(() -> PlatformUtils.makeExecutable(testFile));
        } else {
            // Should succeed on Unix-like systems
            assertDoesNotThrow(() -> PlatformUtils.makeExecutable(testFile));
            assertTrue(Files.isExecutable(testFile));
        }
    }

    @Test
    void testMakeDirectoryExecutable_NonDirectory(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, "test");

        if (PlatformUtils.isWindows()) {
            // Should be no-op on Windows
            assertDoesNotThrow(() -> PlatformUtils.makeDirectoryExecutable(file));
        } else {
            // Should throw on Unix-like systems
            assertThrows(IOException.class, () -> PlatformUtils.makeDirectoryExecutable(file));
        }
    }

    @Test
    void testMakeDirectoryExecutable_ValidDirectory(@TempDir Path tempDir) throws IOException {
        // Create subdirectory with files
        Path subdir = tempDir.resolve("scripts");
        Files.createDirectories(subdir);

        Path script1 = subdir.resolve("script1.sh");
        Path script2 = subdir.resolve("script2.sh");
        Files.writeString(script1, "#!/bin/bash\necho 1");
        Files.writeString(script2, "#!/bin/bash\necho 2");

        assertDoesNotThrow(() -> PlatformUtils.makeDirectoryExecutable(subdir));

        if (!PlatformUtils.isWindows()) {
            assertTrue(Files.isExecutable(script1));
            assertTrue(Files.isExecutable(script2));
        }
    }

    @Test
    void testIsPathWritable_ExistingWritableDirectory(@TempDir Path tempDir) {
        assertTrue(PlatformUtils.isPathWritable(tempDir));
    }

    @Test
    void testIsPathWritable_NonExistentPath(@TempDir Path tempDir) {
        Path nonExistent = tempDir.resolve("new-directory");
        assertTrue(PlatformUtils.isPathWritable(nonExistent));
    }

    @Test
    void testIsPathWritable_ExistingFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "test");
        assertTrue(PlatformUtils.isPathWritable(file));
    }

    @Test
    void testGetSdkToolsDownloadUrl() {
        String url = PlatformUtils.getSdkToolsDownloadUrl();
        assertNotNull(url);
        assertTrue(url.startsWith("https://dl.google.com/android/repository/"));
        assertTrue(url.endsWith(".zip"));

        // Verify it contains the correct OS identifier
        if (PlatformUtils.isWindows()) {
            assertTrue(url.contains("win"));
        } else if (PlatformUtils.isMacOS()) {
            assertTrue(url.contains("mac"));
        } else if (PlatformUtils.isLinux()) {
            assertTrue(url.contains("linux"));
        }
    }

    @Test
    void testGetSdkToolsFileName() {
        String filename = PlatformUtils.getSdkToolsFileName();
        assertNotNull(filename);
        assertTrue(filename.startsWith("commandlinetools-"));
        assertTrue(filename.endsWith(".zip"));

        // Verify it contains the correct OS identifier
        if (PlatformUtils.isWindows()) {
            assertEquals("commandlinetools-win.zip", filename);
        } else if (PlatformUtils.isMacOS()) {
            assertEquals("commandlinetools-mac.zip", filename);
        } else if (PlatformUtils.isLinux()) {
            assertEquals("commandlinetools-linux.zip", filename);
        }
    }

    @Test
    void testOperatingSystemEnum() {
        // Test that all enum values can be accessed
        assertNotNull(PlatformUtils.OperatingSystem.WINDOWS);
        assertNotNull(PlatformUtils.OperatingSystem.LINUX);
        assertNotNull(PlatformUtils.OperatingSystem.MACOS);
        assertNotNull(PlatformUtils.OperatingSystem.UNKNOWN);

        // Test valueOf
        assertEquals(PlatformUtils.OperatingSystem.WINDOWS,
                    PlatformUtils.OperatingSystem.valueOf("WINDOWS"));
        assertEquals(PlatformUtils.OperatingSystem.LINUX,
                    PlatformUtils.OperatingSystem.valueOf("LINUX"));
        assertEquals(PlatformUtils.OperatingSystem.MACOS,
                    PlatformUtils.OperatingSystem.valueOf("MACOS"));
        assertEquals(PlatformUtils.OperatingSystem.UNKNOWN,
                    PlatformUtils.OperatingSystem.valueOf("UNKNOWN"));
    }

    @Test
    void testOperatingSystemEnum_Values() {
        PlatformUtils.OperatingSystem[] values = PlatformUtils.OperatingSystem.values();
        assertEquals(4, values.length);
    }

    @Test
    void testGetDefaultSdkPath_NotNull() {
        Path sdkPath = PlatformUtils.getDefaultSdkPath();
        assertNotNull(sdkPath);
        assertFalse(sdkPath.toString().isEmpty());
    }

    @Test
    void testExtensions_Consistency() {
        // Both extensions should be non-null
        assertNotNull(PlatformUtils.getExecutableExtension());
        assertNotNull(PlatformUtils.getBinaryExtension());

        // On the same OS, repeated calls should return same value
        String ext1 = PlatformUtils.getExecutableExtension();
        String ext2 = PlatformUtils.getExecutableExtension();
        assertEquals(ext1, ext2);
    }

    @Test
    void testMakeDirectoryExecutable_EmptyDirectory(@TempDir Path tempDir) {
        // Empty directory should not throw
        assertDoesNotThrow(() -> PlatformUtils.makeDirectoryExecutable(tempDir));
    }

    @Test
    void testIsPathWritable_SystemRoot() {
        // Try to write to system root (should typically fail unless running as root/admin)
        Path systemRoot = Paths.get("/");
        // This might succeed or fail depending on permissions, but shouldn't throw
        assertDoesNotThrow(() -> PlatformUtils.isPathWritable(systemRoot));
    }
}
