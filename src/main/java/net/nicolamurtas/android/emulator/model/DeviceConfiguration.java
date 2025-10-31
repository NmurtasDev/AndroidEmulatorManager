package net.nicolamurtas.android.emulator.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Configuration details for an Android Virtual Device.
 * Contains hardware specifications and features.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
@Data
@Builder
public class DeviceConfiguration {
    /**
     * Device type (e.g., "pixel_7", "pixel_8")
     */
    private String deviceType;

    /**
     * CPU architecture (e.g., "x86_64", "arm64-v8a")
     */
    private String cpuArch;

    /**
     * RAM size in megabytes
     */
    private int ramMb;

    /**
     * Internal storage size in megabytes
     */
    private int internalStorageMb;

    /**
     * Screen resolution (e.g., "1080x2400")
     */
    private String screenResolution;

    /**
     * List of hardware features enabled (e.g., "GPS", "Camera")
     */
    private List<String> hardwareFeatures;

    /**
     * API level (e.g., "35", "34")
     */
    private String apiLevel;

    /**
     * Android version name (e.g., "Android 15", "Android 14")
     */
    private String androidVersion;
}
