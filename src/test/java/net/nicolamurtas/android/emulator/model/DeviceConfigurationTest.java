package net.nicolamurtas.android.emulator.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeviceConfiguration domain object.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class DeviceConfigurationTest {

    @Test
    void testDeviceConfiguration_Builder() {
        List<String> features = Arrays.asList("android.hardware.wifi", "android.hardware.bluetooth");

        DeviceConfiguration device = DeviceConfiguration.builder()
            .deviceType("pixel_7")
            .cpuArch("x86_64")
            .ramMb(4096)
            .internalStorageMb(8192)
            .screenResolution("1080x2400")
            .hardwareFeatures(features)
            .apiLevel("34")
            .androidVersion("Android 14")
            .build();

        assertEquals("pixel_7", device.getDeviceType());
        assertEquals("x86_64", device.getCpuArch());
        assertEquals(4096, device.getRamMb());
        assertEquals(8192, device.getInternalStorageMb());
        assertEquals("1080x2400", device.getScreenResolution());
        assertEquals(features, device.getHardwareFeatures());
        assertEquals("34", device.getApiLevel());
        assertEquals("Android 14", device.getAndroidVersion());
    }

    @Test
    void testDeviceConfiguration_DefaultValues() {
        DeviceConfiguration device = DeviceConfiguration.builder().build();

        assertNull(device.getDeviceType());
        assertNull(device.getCpuArch());
        assertEquals(0, device.getRamMb());
        assertEquals(0, device.getInternalStorageMb());
        assertNull(device.getScreenResolution());
        assertNull(device.getHardwareFeatures());
        assertNull(device.getApiLevel());
        assertNull(device.getAndroidVersion());
    }

    @Test
    void testDeviceConfiguration_EmptyFeaturesList() {
        DeviceConfiguration device = DeviceConfiguration.builder()
            .hardwareFeatures(Collections.emptyList())
            .build();

        assertNotNull(device.getHardwareFeatures());
        assertTrue(device.getHardwareFeatures().isEmpty());
    }

    @Test
    void testDeviceConfiguration_SettersAndGetters() {
        DeviceConfiguration device = new DeviceConfiguration();
        List<String> features = Arrays.asList("android.hardware.camera");

        device.setDeviceType("pixel_5");
        device.setCpuArch("arm64-v8a");
        device.setRamMb(2048);
        device.setInternalStorageMb(4096);
        device.setScreenResolution("1080x1920");
        device.setHardwareFeatures(features);
        device.setApiLevel("33");
        device.setAndroidVersion("Android 13");

        assertEquals("pixel_5", device.getDeviceType());
        assertEquals("arm64-v8a", device.getCpuArch());
        assertEquals(2048, device.getRamMb());
        assertEquals(4096, device.getInternalStorageMb());
        assertEquals("1080x1920", device.getScreenResolution());
        assertEquals(features, device.getHardwareFeatures());
        assertEquals("33", device.getApiLevel());
        assertEquals("Android 13", device.getAndroidVersion());
    }

    @Test
    void testDeviceConfiguration_Equality() {
        List<String> features = Arrays.asList("android.hardware.wifi");

        DeviceConfiguration device1 = DeviceConfiguration.builder()
            .deviceType("pixel_7")
            .cpuArch("x86_64")
            .ramMb(4096)
            .apiLevel("34")
            .hardwareFeatures(features)
            .build();

        DeviceConfiguration device2 = DeviceConfiguration.builder()
            .deviceType("pixel_7")
            .cpuArch("x86_64")
            .ramMb(4096)
            .apiLevel("34")
            .hardwareFeatures(features)
            .build();

        DeviceConfiguration device3 = DeviceConfiguration.builder()
            .deviceType("pixel_5")
            .cpuArch("x86_64")
            .ramMb(4096)
            .apiLevel("34")
            .hardwareFeatures(features)
            .build();

        assertEquals(device1, device2);
        assertNotEquals(device1, device3);
    }

    @Test
    void testDeviceConfiguration_HashCode() {
        DeviceConfiguration device1 = DeviceConfiguration.builder()
            .deviceType("pixel_7")
            .cpuArch("x86_64")
            .ramMb(4096)
            .build();

        DeviceConfiguration device2 = DeviceConfiguration.builder()
            .deviceType("pixel_7")
            .cpuArch("x86_64")
            .ramMb(4096)
            .build();

        assertEquals(device1.hashCode(), device2.hashCode());
    }

    @Test
    void testDeviceConfiguration_ToString() {
        DeviceConfiguration device = DeviceConfiguration.builder()
            .deviceType("pixel_7")
            .cpuArch("x86_64")
            .ramMb(4096)
            .apiLevel("34")
            .build();

        String str = device.toString();
        assertNotNull(str);
        assertTrue(str.contains("pixel_7"));
        assertTrue(str.contains("x86_64"));
        assertTrue(str.contains("4096"));
        assertTrue(str.contains("34"));
    }

    @Test
    void testDeviceConfiguration_LowEndDevice() {
        DeviceConfiguration device = DeviceConfiguration.builder()
            .deviceType("low_end")
            .cpuArch("x86")
            .ramMb(512)
            .internalStorageMb(1024)
            .screenResolution("480x800")
            .apiLevel("21")
            .androidVersion("Android 5.0")
            .build();

        assertEquals(512, device.getRamMb());
        assertEquals(1024, device.getInternalStorageMb());
        assertEquals("Android 5.0", device.getAndroidVersion());
    }

    @Test
    void testDeviceConfiguration_HighEndDevice() {
        DeviceConfiguration device = DeviceConfiguration.builder()
            .deviceType("pixel_8_pro")
            .cpuArch("arm64-v8a")
            .ramMb(12288)
            .internalStorageMb(262144)
            .screenResolution("1440x3120")
            .apiLevel("35")
            .androidVersion("Android 15")
            .build();

        assertEquals(12288, device.getRamMb());
        assertEquals(262144, device.getInternalStorageMb());
        assertEquals("Android 15", device.getAndroidVersion());
    }

    @Test
    void testDeviceConfiguration_MultipleHardwareFeatures() {
        List<String> features = Arrays.asList(
            "android.hardware.wifi",
            "android.hardware.bluetooth",
            "android.hardware.camera",
            "android.hardware.gps",
            "android.hardware.nfc",
            "android.hardware.fingerprint"
        );

        DeviceConfiguration device = DeviceConfiguration.builder()
            .hardwareFeatures(features)
            .build();

        assertEquals(6, device.getHardwareFeatures().size());
        assertTrue(device.getHardwareFeatures().contains("android.hardware.wifi"));
        assertTrue(device.getHardwareFeatures().contains("android.hardware.fingerprint"));
    }

    @Test
    void testDeviceConfiguration_DifferentArchitectures() {
        String[] architectures = {"x86", "x86_64", "arm64-v8a", "armeabi-v7a"};

        for (String arch : architectures) {
            DeviceConfiguration device = DeviceConfiguration.builder()
                .cpuArch(arch)
                .build();

            assertEquals(arch, device.getCpuArch());
        }
    }

    @Test
    void testDeviceConfiguration_VariousResolutions() {
        String[] resolutions = {
            "480x800",      // WVGA
            "720x1280",     // HD
            "1080x1920",    // Full HD
            "1440x2560",    // QHD
            "1440x3120"     // QHD+
        };

        for (String resolution : resolutions) {
            DeviceConfiguration device = DeviceConfiguration.builder()
                .screenResolution(resolution)
                .build();

            assertEquals(resolution, device.getScreenResolution());
        }
    }

    @Test
    void testDeviceConfiguration_AllApiLevels() {
        for (int apiLevel = 21; apiLevel <= 35; apiLevel++) {
            DeviceConfiguration device = DeviceConfiguration.builder()
                .apiLevel(String.valueOf(apiLevel))
                .build();

            assertEquals(String.valueOf(apiLevel), device.getApiLevel());
        }
    }

    @Test
    void testDeviceConfiguration_NullValues() {
        DeviceConfiguration device = DeviceConfiguration.builder()
            .deviceType(null)
            .cpuArch(null)
            .screenResolution(null)
            .hardwareFeatures(null)
            .apiLevel(null)
            .androidVersion(null)
            .build();

        assertNull(device.getDeviceType());
        assertNull(device.getCpuArch());
        assertNull(device.getScreenResolution());
        assertNull(device.getHardwareFeatures());
        assertNull(device.getApiLevel());
        assertNull(device.getAndroidVersion());
    }

    @Test
    void testDeviceConfiguration_NegativeValues() {
        DeviceConfiguration device = DeviceConfiguration.builder()
            .ramMb(-1)
            .internalStorageMb(-1)
            .build();

        assertEquals(-1, device.getRamMb());
        assertEquals(-1, device.getInternalStorageMb());
    }

    @Test
    void testDeviceConfiguration_ZeroValues() {
        DeviceConfiguration device = DeviceConfiguration.builder()
            .ramMb(0)
            .internalStorageMb(0)
            .build();

        assertEquals(0, device.getRamMb());
        assertEquals(0, device.getInternalStorageMb());
    }
}
