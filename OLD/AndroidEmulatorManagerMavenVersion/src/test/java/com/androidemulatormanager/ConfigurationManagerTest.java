package com.androidemulatormanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("ConfigurationManager Tests")
class ConfigurationManagerTest {
    
    @Mock
    private LocalizationManager mockLocalizationManager;
    
    private ConfigurationManager configurationManager;
    private static final String TEST_CONFIG_FILE = "android_emulator_config.properties";
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockLocalizationManager.getMessage(anyString(), anyString()))
            .thenReturn("Test error message");
        
        cleanupTestConfigFile();
        configurationManager = new ConfigurationManager(mockLocalizationManager);
    }
    
    @AfterEach
    void tearDown() {
        cleanupTestConfigFile();
    }
    
    private void cleanupTestConfigFile() {
        try {
            Files.deleteIfExists(Paths.get(TEST_CONFIG_FILE));
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    @DisplayName("Should initialize with empty SDK path when no config file exists")
    void shouldInitializeWithEmptySdkPathWhenNoConfigFileExists() {
        assertEquals("", configurationManager.getSdkPath());
    }
    
    @Test
    @DisplayName("Should set and get SDK path")
    void shouldSetAndGetSdkPath() {
        String testPath = "/test/sdk/path";
        
        configurationManager.setSdkPath(testPath);
        
        assertEquals(testPath, configurationManager.getSdkPath());
    }
    
    @Test
    @DisplayName("Should persist SDK path to file when set")
    void shouldPersistSdkPathToFileWhenSet() {
        String testPath = "/test/sdk/path";
        
        configurationManager.setSdkPath(testPath);
        
        File configFile = new File(TEST_CONFIG_FILE);
        assertTrue(configFile.exists(), "Config file should be created");
    }
    
    @Test
    @DisplayName("Should load SDK path from existing config file")
    void shouldLoadSdkPathFromExistingConfigFile() {
        String testPath = "/existing/sdk/path";
        
        configurationManager.setSdkPath(testPath);
        
        ConfigurationManager newConfigManager = new ConfigurationManager(mockLocalizationManager);
        
        assertEquals(testPath, newConfigManager.getSdkPath());
    }
    
    @Test
    @DisplayName("Should handle null SDK path gracefully")
    void shouldHandleNullSdkPathGracefully() {
        assertDoesNotThrow(() -> {
            configurationManager.setSdkPath(null);
            String path = configurationManager.getSdkPath();
            assertNotNull(path);
        });
    }
    
    @Test
    @DisplayName("Should handle empty SDK path")
    void shouldHandleEmptySdkPath() {
        configurationManager.setSdkPath("");
        
        assertEquals("", configurationManager.getSdkPath());
    }
    
    @Test
    @DisplayName("Should handle SDK path with spaces")
    void shouldHandleSdkPathWithSpaces() {
        String testPath = "/path with spaces/sdk";
        
        configurationManager.setSdkPath(testPath);
        
        assertEquals(testPath, configurationManager.getSdkPath());
    }
    
    @Test
    @DisplayName("Should handle very long SDK paths")
    void shouldHandleVeryLongSdkPaths() {
        StringBuilder longPath = new StringBuilder("/very/long/path");
        for (int i = 0; i < 100; i++) {
            longPath.append("/directory").append(i);
        }
        String testPath = longPath.toString();
        
        configurationManager.setSdkPath(testPath);
        
        assertEquals(testPath, configurationManager.getSdkPath());
    }
    
    @Test
    @DisplayName("Should override previous SDK path when setting new one")
    void shouldOverridePreviousSdkPathWhenSettingNewOne() {
        String firstPath = "/first/path";
        String secondPath = "/second/path";
        
        configurationManager.setSdkPath(firstPath);
        assertEquals(firstPath, configurationManager.getSdkPath());
        
        configurationManager.setSdkPath(secondPath);
        assertEquals(secondPath, configurationManager.getSdkPath());
    }
    
    @Test
    @DisplayName("Should handle Windows-style paths")
    void shouldHandleWindowsStylePaths() {
        String windowsPath = "C:\\Users\\Test\\Android\\sdk";
        
        configurationManager.setSdkPath(windowsPath);
        
        assertEquals(windowsPath, configurationManager.getSdkPath());
    }
}