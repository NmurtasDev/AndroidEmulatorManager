package net.nicolamurtas.android.emulator.model;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;

/**
 * Configuration for the Android SDK.
 * Contains SDK path, installed packages, and tool versions.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
@Data
@Builder
public class SdkConfiguration {
    /**
     * Path to the Android SDK directory
     */
    private Path sdkPath;

    /**
     * Whether the SDK is properly configured and available
     */
    private boolean configured;

    /**
     * Platform tools version (e.g., "35.0.0")
     */
    private String platformToolsVersion;

    /**
     * Build tools version (e.g., "35.0.0")
     */
    private String buildToolsVersion;

    /**
     * List of installed SDK packages
     */
    private List<String> installedPackages;

    /**
     * Whether Android SDK licenses have been accepted
     */
    private boolean licensesAccepted;
}
