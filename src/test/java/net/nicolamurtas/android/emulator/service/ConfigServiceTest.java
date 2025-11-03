package net.nicolamurtas.android.emulator.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigService.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
class ConfigServiceTest {

    private ConfigService configService;
    private Path originalConfigFile;

    @BeforeEach
    void setUp() throws IOException {
        // Backup existing config file if it exists
        originalConfigFile = Paths.get("android_emulator_config.properties");
        if (Files.exists(originalConfigFile)) {
            Files.move(originalConfigFile,
                      Paths.get("android_emulator_config.properties.backup"));
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test config file
        Path configFile = Paths.get("android_emulator_config.properties");
        Files.deleteIfExists(configFile);

        // Restore original config file
        Path backup = Paths.get("android_emulator_config.properties.backup");
        if (Files.exists(backup)) {
            Files.move(backup, originalConfigFile);
        }
    }

    @Test
    void testConfigService_CanBeInstantiated() {
        assertDoesNotThrow(() -> {
            configService = new ConfigService();
            assertNotNull(configService);
        });
    }

    @Test
    void testGetSdkPath_DefaultValue() {
        configService = new ConfigService();
        Path sdkPath = configService.getSdkPath();
        assertNotNull(sdkPath);
        // Should return default SDK path when not configured
        assertTrue(sdkPath.toString().contains("Android"));
    }

    @Test
    void testSetSdkPath_AndGet(@TempDir Path tempDir) {
        configService = new ConfigService();

        Path testPath = tempDir.resolve("android-sdk");
        configService.setSdkPath(testPath);
        configService.saveConfig();

        // Create new instance to verify persistence
        ConfigService newService = new ConfigService();
        assertEquals(testPath, newService.getSdkPath());
    }

    @Test
    void testSaveConfig_CreatesFile() {
        configService = new ConfigService();
        configService.setSdkPath(Paths.get("/test/path"));
        configService.saveConfig();

        Path configFile = Paths.get("android_emulator_config.properties");
        assertTrue(Files.exists(configFile));
    }

    @Test
    void testGetValue_ExistingKey() {
        configService = new ConfigService();
        configService.setValue("test.key", "test.value");

        var value = configService.getValue("test.key");
        assertTrue(value.isPresent());
        assertEquals("test.value", value.get());
    }

    @Test
    void testGetValue_NonExistingKey() {
        configService = new ConfigService();

        var value = configService.getValue("non.existing.key");
        assertFalse(value.isPresent());
    }

    @Test
    void testSetValue_AndRetrieve() {
        configService = new ConfigService();

        configService.setValue("custom.property", "custom.value");
        var retrieved = configService.getValue("custom.property");

        assertTrue(retrieved.isPresent());
        assertEquals("custom.value", retrieved.get());
    }

    @Test
    void testRemoveValue() {
        configService = new ConfigService();

        configService.setValue("remove.me", "value");
        assertTrue(configService.getValue("remove.me").isPresent());

        configService.removeValue("remove.me");
        assertFalse(configService.getValue("remove.me").isPresent());
    }

    @Test
    void testIsSdkConfigured_NotConfigured() {
        configService = new ConfigService();
        // With default path that doesn't exist, should return false
        assertFalse(configService.isSdkConfigured());
    }

    @Test
    void testIsSdkConfigured_WithPlatformTools(@TempDir Path tempDir) throws IOException {
        configService = new ConfigService();

        // Create minimal SDK structure with platform-tools
        Path sdkPath = tempDir.resolve("android-sdk");
        Path platformTools = sdkPath.resolve("platform-tools");
        Files.createDirectories(platformTools);

        configService.setSdkPath(sdkPath);
        assertTrue(configService.isSdkConfigured());
    }

    @Test
    void testIsSdkConfigured_WithCmdlineTools(@TempDir Path tempDir) throws IOException {
        configService = new ConfigService();

        // Create minimal SDK structure with cmdline-tools
        Path sdkPath = tempDir.resolve("android-sdk");
        Path cmdlineTools = sdkPath.resolve("cmdline-tools").resolve("latest");
        Files.createDirectories(cmdlineTools);

        configService.setSdkPath(sdkPath);
        assertTrue(configService.isSdkConfigured());
    }

    @Test
    void testSetValue_OverwriteExisting() {
        configService = new ConfigService();

        configService.setValue("key", "value1");
        assertEquals("value1", configService.getValue("key").get());

        configService.setValue("key", "value2");
        assertEquals("value2", configService.getValue("key").get());
    }

    @Test
    void testSaveAndLoad_Persistence(@TempDir Path tempDir) {
        // First instance sets and saves values
        configService = new ConfigService();
        configService.setSdkPath(tempDir.resolve("sdk"));
        configService.setValue("test.property", "test.value");
        configService.saveConfig();

        // Second instance loads from file
        ConfigService newService = new ConfigService();
        assertEquals(tempDir.resolve("sdk"), newService.getSdkPath());
        assertEquals("test.value", newService.getValue("test.property").get());
    }

    @Test
    void testGetSdkPath_EmptyString() {
        configService = new ConfigService();
        configService.setValue("sdk.path", "");

        // Empty string should return default path
        Path sdkPath = configService.getSdkPath();
        assertNotNull(sdkPath);
        assertTrue(sdkPath.toString().contains("Android"));
    }

    @Test
    void testMultipleValues() {
        configService = new ConfigService();

        configService.setValue("key1", "value1");
        configService.setValue("key2", "value2");
        configService.setValue("key3", "value3");

        assertEquals("value1", configService.getValue("key1").get());
        assertEquals("value2", configService.getValue("key2").get());
        assertEquals("value3", configService.getValue("key3").get());
    }

    @Test
    void testRemoveNonExistentValue() {
        configService = new ConfigService();

        // Should not throw when removing non-existent key
        assertDoesNotThrow(() -> configService.removeValue("non.existent"));
    }

    @Test
    void testSdkPathWithSpaces(@TempDir Path tempDir) {
        configService = new ConfigService();

        Path pathWithSpaces = tempDir.resolve("path with spaces");
        configService.setSdkPath(pathWithSpaces);

        assertEquals(pathWithSpaces, configService.getSdkPath());
    }

    @Test
    void testSdkPathWithSpecialCharacters(@TempDir Path tempDir) {
        configService = new ConfigService();

        Path pathWithSpecial = tempDir.resolve("path-with_special.chars");
        configService.setSdkPath(pathWithSpecial);

        assertEquals(pathWithSpecial, configService.getSdkPath());
    }
}
