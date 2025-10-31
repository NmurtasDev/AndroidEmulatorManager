package net.nicolamurtas.android.emulator.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DownloadProgress value object.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class DownloadProgressTest {

    @Test
    void testDownloadProgress_Creation() {
        DownloadProgress progress = new DownloadProgress(
            50,
            5000000L,
            10000000L,
            "platform-tools.zip",
            "Downloading platform tools..."
        );

        assertEquals(50, progress.getPercentage());
        assertEquals(5000000L, progress.getBytesDownloaded());
        assertEquals(10000000L, progress.getTotalBytes());
        assertEquals("platform-tools.zip", progress.getCurrentFile());
        assertEquals("Downloading platform tools...", progress.getMessage());
    }

    @Test
    void testDownloadProgress_ZeroProgress() {
        DownloadProgress progress = new DownloadProgress(
            0,
            0L,
            10000000L,
            "starting",
            "Initializing download..."
        );

        assertEquals(0, progress.getPercentage());
        assertEquals(0L, progress.getBytesDownloaded());
        assertEquals(10000000L, progress.getTotalBytes());
    }

    @Test
    void testDownloadProgress_CompleteProgress() {
        DownloadProgress progress = new DownloadProgress(
            100,
            10000000L,
            10000000L,
            "platform-tools.zip",
            "Download complete"
        );

        assertEquals(100, progress.getPercentage());
        assertEquals(10000000L, progress.getBytesDownloaded());
        assertEquals(10000000L, progress.getTotalBytes());
    }

    @Test
    void testDownloadProgress_Equality() {
        DownloadProgress progress1 = new DownloadProgress(
            50, 5000000L, 10000000L, "file.zip", "Downloading..."
        );
        DownloadProgress progress2 = new DownloadProgress(
            50, 5000000L, 10000000L, "file.zip", "Downloading..."
        );
        DownloadProgress progress3 = new DownloadProgress(
            60, 6000000L, 10000000L, "file.zip", "Downloading..."
        );

        assertEquals(progress1, progress2);
        assertNotEquals(progress1, progress3);
    }

    @Test
    void testDownloadProgress_HashCode() {
        DownloadProgress progress1 = new DownloadProgress(
            50, 5000000L, 10000000L, "file.zip", "Downloading..."
        );
        DownloadProgress progress2 = new DownloadProgress(
            50, 5000000L, 10000000L, "file.zip", "Downloading..."
        );

        assertEquals(progress1.hashCode(), progress2.hashCode());
    }

    @Test
    void testDownloadProgress_ToString() {
        DownloadProgress progress = new DownloadProgress(
            50, 5000000L, 10000000L, "file.zip", "Downloading..."
        );

        String str = progress.toString();
        assertNotNull(str);
        assertTrue(str.contains("50"));
        assertTrue(str.contains("5000000"));
        assertTrue(str.contains("10000000"));
    }

    @Test
    void testDownloadProgress_NullMessage() {
        DownloadProgress progress = new DownloadProgress(
            50, 5000000L, 10000000L, "file.zip", null
        );

        assertNull(progress.getMessage());
    }

    @Test
    void testDownloadProgress_NullCurrentFile() {
        DownloadProgress progress = new DownloadProgress(
            50, 5000000L, 10000000L, null, "Downloading..."
        );

        assertNull(progress.getCurrentFile());
    }

    @Test
    void testDownloadProgress_EmptyStrings() {
        DownloadProgress progress = new DownloadProgress(
            50, 5000000L, 10000000L, "", ""
        );

        assertEquals("", progress.getCurrentFile());
        assertEquals("", progress.getMessage());
    }

    @Test
    void testDownloadProgress_LargeValues() {
        DownloadProgress progress = new DownloadProgress(
            100,
            Long.MAX_VALUE,
            Long.MAX_VALUE,
            "huge-file.zip",
            "Downloading huge file"
        );

        assertEquals(100, progress.getPercentage());
        assertEquals(Long.MAX_VALUE, progress.getBytesDownloaded());
        assertEquals(Long.MAX_VALUE, progress.getTotalBytes());
    }

    @Test
    void testDownloadProgress_NegativePercentage() {
        // Even though it doesn't make sense, the value object should store whatever is given
        DownloadProgress progress = new DownloadProgress(
            -1, 0L, 10000000L, "file.zip", "Error"
        );

        assertEquals(-1, progress.getPercentage());
    }

    @Test
    void testDownloadProgress_PercentageOverHundred() {
        DownloadProgress progress = new DownloadProgress(
            150, 15000000L, 10000000L, "file.zip", "Over capacity"
        );

        assertEquals(150, progress.getPercentage());
    }
}
