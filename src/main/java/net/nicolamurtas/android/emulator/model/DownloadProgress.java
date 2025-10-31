package net.nicolamurtas.android.emulator.model;

import lombok.Value;

/**
 * Immutable value object representing download progress.
 * Used for tracking SDK component downloads.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
@Value
public class DownloadProgress {
    /**
     * Download completion percentage (0-100)
     */
    int percentage;

    /**
     * Number of bytes downloaded so far
     */
    long bytesDownloaded;

    /**
     * Total number of bytes to download
     */
    long totalBytes;

    /**
     * Name of the file currently being downloaded
     */
    String currentFile;

    /**
     * Human-readable status message
     */
    String message;
}
