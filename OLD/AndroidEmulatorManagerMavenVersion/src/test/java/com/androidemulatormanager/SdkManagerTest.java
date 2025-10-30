package com.androidemulatormanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("SdkManager Tests")
class SdkManagerTest {
    
    @Mock
    private LocalizationManager mockLocalizationManager;
    
    @Mock
    private Consumer<String> mockLogger;
    
    @Mock
    private Consumer<SdkManager.ProgressInfo> mockProgressCallback;
    
    private SdkManager sdkManager;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(mockLocalizationManager.getMessage(anyString())).thenReturn("Test message");
        when(mockLocalizationManager.getMessage(anyString(), any())).thenReturn("Test message with param");
        
        sdkManager = new SdkManager(mockLocalizationManager, mockLogger, mockProgressCallback);
    }
    
    @Test
    @DisplayName("Should initialize SdkManager without exceptions")
    void shouldInitializeSdkManagerWithoutExceptions() {
        assertNotNull(sdkManager);
    }
    
    @Test
    @DisplayName("Should return null for non-existent SDK manager path")
    void shouldReturnNullForNonExistentSdkManagerPath() {
        String nonExistentPath = tempDir.resolve("non-existent").toString();
        
        String sdkManagerPath = sdkManager.getSdkManagerPath(nonExistentPath);
        
        assertNull(sdkManagerPath);
    }
    
    @Test
    @DisplayName("Should return null for non-existent AVD manager path")
    void shouldReturnNullForNonExistentAvdManagerPath() {
        String nonExistentPath = tempDir.resolve("non-existent").toString();
        
        String avdManagerPath = sdkManager.getAvdManagerPath(nonExistentPath);
        
        assertNull(avdManagerPath);
    }
    
    @Test
    @DisplayName("Should return SDK manager path when it exists on Windows")
    void shouldReturnSdkManagerPathWhenItExistsOnWindows() throws Exception {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Windows 10");
            
            Path cmdlineToolsPath = tempDir.resolve("cmdline-tools").resolve("latest").resolve("bin");
            Files.createDirectories(cmdlineToolsPath);
            Path sdkManagerPath = cmdlineToolsPath.resolve("sdkmanager.bat");
            Files.createFile(sdkManagerPath);
            
            String result = sdkManager.getSdkManagerPath(tempDir.toString());
            
            assertNotNull(result);
            assertTrue(result.endsWith("sdkmanager.bat"));
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }
    
    @Test
    @DisplayName("Should return SDK manager path when it exists on Linux")
    void shouldReturnSdkManagerPathWhenItExistsOnLinux() throws Exception {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Linux");
            
            Path cmdlineToolsPath = tempDir.resolve("cmdline-tools").resolve("latest").resolve("bin");
            Files.createDirectories(cmdlineToolsPath);
            Path sdkManagerPath = cmdlineToolsPath.resolve("sdkmanager");
            Files.createFile(sdkManagerPath);
            
            String result = sdkManager.getSdkManagerPath(tempDir.toString());
            
            assertNotNull(result);
            assertTrue(result.endsWith("sdkmanager"));
            assertFalse(result.endsWith(".bat"));
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }
    
    @Test
    @DisplayName("Should return AVD manager path when it exists on Windows")
    void shouldReturnAvdManagerPathWhenItExistsOnWindows() throws Exception {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Windows 10");
            
            Path cmdlineToolsPath = tempDir.resolve("cmdline-tools").resolve("latest").resolve("bin");
            Files.createDirectories(cmdlineToolsPath);
            Path avdManagerPath = cmdlineToolsPath.resolve("avdmanager.bat");
            Files.createFile(avdManagerPath);
            
            String result = sdkManager.getAvdManagerPath(tempDir.toString());
            
            assertNotNull(result);
            assertTrue(result.endsWith("avdmanager.bat"));
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }
    
    @Test
    @DisplayName("Should return AVD manager path when it exists on macOS")
    void shouldReturnAvdManagerPathWhenItExistsOnMacOS() throws Exception {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Mac OS X");
            
            Path cmdlineToolsPath = tempDir.resolve("cmdline-tools").resolve("latest").resolve("bin");
            Files.createDirectories(cmdlineToolsPath);
            Path avdManagerPath = cmdlineToolsPath.resolve("avdmanager");
            Files.createFile(avdManagerPath);
            
            String result = sdkManager.getAvdManagerPath(tempDir.toString());
            
            assertNotNull(result);
            assertTrue(result.endsWith("avdmanager"));
            assertFalse(result.endsWith(".bat"));
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }
    
    @Test
    @DisplayName("Should call setupSdk and log appropriate messages")
    void shouldCallSetupSdkAndLogAppropriateMessages() throws Exception {
        String testSdkPath = tempDir.toString();
        
        sdkManager.setupSdk(testSdkPath);
        
        Thread.sleep(100);
        
        verify(mockLogger, atLeastOnce()).accept(anyString());
        verify(mockLocalizationManager, atLeastOnce()).getMessage("log.setup.start");
    }
    
    @Test
    @DisplayName("Should handle empty SDK path gracefully")
    void shouldHandleEmptySdkPathGracefully() {
        assertDoesNotThrow(() -> {
            sdkManager.setupSdk("");
        });
        
        // Give some time for async operation
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
    
    @Test
    @DisplayName("Should handle null SDK path gracefully")
    void shouldHandleNullSdkPathGracefully() {
        assertDoesNotThrow(() -> {
            sdkManager.setupSdk(null);
        });
    }
    
    @Test
    @DisplayName("ProgressInfo should store value and text correctly")
    void progressInfoShouldStoreValueAndTextCorrectly() {
        int testValue = 50;
        String testText = "Test progress";
        
        SdkManager.ProgressInfo progressInfo = new SdkManager.ProgressInfo(testValue, testText);
        
        assertEquals(testValue, progressInfo.value);
        assertEquals(testText, progressInfo.text);
    }
    
    @Test
    @DisplayName("Should accept progress callback calls during download simulation")
    void shouldAcceptProgressCallbackCallsDuringDownloadSimulation() throws Exception {
        String testSdkPath = tempDir.toString();
        
        sdkManager.downloadAndSetupSdk(testSdkPath);
        
        Thread.sleep(200);
        
        verify(mockProgressCallback, atLeastOnce()).accept(any(SdkManager.ProgressInfo.class));
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
}