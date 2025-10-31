package net.nicolamurtas.android.emulator.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SdkConfiguration domain object.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class SdkConfigurationTest {

    @Test
    void testSdkConfiguration_Builder() {
        Path sdkPath = Paths.get("/home/user/Android/sdk");
        List<String> packages = Arrays.asList("platform-tools", "build-tools;33.0.0");

        SdkConfiguration config = SdkConfiguration.builder()
            .sdkPath(sdkPath)
            .configured(true)
            .platformToolsVersion("34.0.0")
            .buildToolsVersion("33.0.0")
            .installedPackages(packages)
            .licensesAccepted(true)
            .build();

        assertEquals(sdkPath, config.getSdkPath());
        assertTrue(config.isConfigured());
        assertEquals("34.0.0", config.getPlatformToolsVersion());
        assertEquals("33.0.0", config.getBuildToolsVersion());
        assertEquals(packages, config.getInstalledPackages());
        assertTrue(config.isLicensesAccepted());
    }

    @Test
    void testSdkConfiguration_DefaultValues() {
        SdkConfiguration config = SdkConfiguration.builder().build();

        assertNull(config.getSdkPath());
        assertFalse(config.isConfigured());
        assertNull(config.getPlatformToolsVersion());
        assertNull(config.getBuildToolsVersion());
        assertNull(config.getInstalledPackages());
        assertFalse(config.isLicensesAccepted());
    }

    @Test
    void testSdkConfiguration_EmptyPackageList() {
        SdkConfiguration config = SdkConfiguration.builder()
            .installedPackages(Collections.emptyList())
            .build();

        assertNotNull(config.getInstalledPackages());
        assertTrue(config.getInstalledPackages().isEmpty());
    }

    @Test
    void testSdkConfiguration_SettersAndGetters() {
        Path sdkPath = Paths.get("/opt/android-sdk");
        SdkConfiguration config = new SdkConfiguration();

        config.setSdkPath(sdkPath);
        config.setConfigured(true);
        config.setPlatformToolsVersion("35.0.0");
        config.setBuildToolsVersion("34.0.0");
        config.setLicensesAccepted(true);

        assertEquals(sdkPath, config.getSdkPath());
        assertTrue(config.isConfigured());
        assertEquals("35.0.0", config.getPlatformToolsVersion());
        assertEquals("34.0.0", config.getBuildToolsVersion());
        assertTrue(config.isLicensesAccepted());
    }

    @Test
    void testSdkConfiguration_Equality() {
        Path sdkPath = Paths.get("/home/user/Android/sdk");
        List<String> packages = Arrays.asList("platform-tools");

        SdkConfiguration config1 = SdkConfiguration.builder()
            .sdkPath(sdkPath)
            .configured(true)
            .platformToolsVersion("34.0.0")
            .installedPackages(packages)
            .build();

        SdkConfiguration config2 = SdkConfiguration.builder()
            .sdkPath(sdkPath)
            .configured(true)
            .platformToolsVersion("34.0.0")
            .installedPackages(packages)
            .build();

        SdkConfiguration config3 = SdkConfiguration.builder()
            .sdkPath(Paths.get("/different/path"))
            .configured(true)
            .platformToolsVersion("34.0.0")
            .installedPackages(packages)
            .build();

        assertEquals(config1, config2);
        assertNotEquals(config1, config3);
    }

    @Test
    void testSdkConfiguration_HashCode() {
        Path sdkPath = Paths.get("/home/user/Android/sdk");

        SdkConfiguration config1 = SdkConfiguration.builder()
            .sdkPath(sdkPath)
            .configured(true)
            .build();

        SdkConfiguration config2 = SdkConfiguration.builder()
            .sdkPath(sdkPath)
            .configured(true)
            .build();

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testSdkConfiguration_ToString() {
        SdkConfiguration config = SdkConfiguration.builder()
            .sdkPath(Paths.get("/home/user/Android/sdk"))
            .configured(true)
            .platformToolsVersion("34.0.0")
            .build();

        String str = config.toString();
        assertNotNull(str);
        assertTrue(str.contains("34.0.0"));
        assertTrue(str.contains("true"));
    }

    @Test
    void testSdkConfiguration_NotConfigured() {
        SdkConfiguration config = SdkConfiguration.builder()
            .sdkPath(Paths.get("/home/user/Android/sdk"))
            .configured(false)
            .licensesAccepted(false)
            .build();

        assertFalse(config.isConfigured());
        assertFalse(config.isLicensesAccepted());
    }

    @Test
    void testSdkConfiguration_MultiplePackages() {
        List<String> packages = Arrays.asList(
            "platform-tools",
            "build-tools;33.0.0",
            "build-tools;34.0.0",
            "platforms;android-33",
            "platforms;android-34",
            "system-images;android-33;google_apis;x86_64"
        );

        SdkConfiguration config = SdkConfiguration.builder()
            .installedPackages(packages)
            .build();

        assertEquals(6, config.getInstalledPackages().size());
        assertTrue(config.getInstalledPackages().contains("platform-tools"));
        assertTrue(config.getInstalledPackages().contains("system-images;android-33;google_apis;x86_64"));
    }

    @Test
    void testSdkConfiguration_WindowsPath() {
        Path windowsPath = Paths.get("C:\\Users\\User\\AppData\\Local\\Android\\sdk");
        SdkConfiguration config = SdkConfiguration.builder()
            .sdkPath(windowsPath)
            .build();

        assertEquals(windowsPath, config.getSdkPath());
    }

    @Test
    void testSdkConfiguration_RelativePath() {
        Path relativePath = Paths.get("../Android/sdk");
        SdkConfiguration config = SdkConfiguration.builder()
            .sdkPath(relativePath)
            .build();

        assertEquals(relativePath, config.getSdkPath());
    }

    @Test
    void testSdkConfiguration_NullVersions() {
        SdkConfiguration config = SdkConfiguration.builder()
            .platformToolsVersion(null)
            .buildToolsVersion(null)
            .build();

        assertNull(config.getPlatformToolsVersion());
        assertNull(config.getBuildToolsVersion());
    }

    @Test
    void testSdkConfiguration_EmptyVersionStrings() {
        SdkConfiguration config = SdkConfiguration.builder()
            .platformToolsVersion("")
            .buildToolsVersion("")
            .build();

        assertEquals("", config.getPlatformToolsVersion());
        assertEquals("", config.getBuildToolsVersion());
    }
}
