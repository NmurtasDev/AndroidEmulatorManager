package com.androidemulatormanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("EmulatorManager Tests")
class EmulatorManagerTest {
    
    @Mock
    private LocalizationManager mockLocalizationManager;
    
    @Mock
    private Consumer<String> mockLogger;
    
    private EmulatorManager emulatorManager;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(mockLocalizationManager.getMessage(anyString())).thenReturn("Test message");
        when(mockLocalizationManager.getMessage(anyString(), any())).thenReturn("Test message with param");
        
        emulatorManager = new EmulatorManager(mockLocalizationManager, mockLogger);
    }
    
    @Test
    @DisplayName("Should initialize EmulatorManager without exceptions")
    void shouldInitializeEmulatorManagerWithoutExceptions() {
        assertNotNull(emulatorManager);
    }
    
    @Test
    @DisplayName("Should return empty list when SDK path is empty")
    void shouldReturnEmptyListWhenSdkPathIsEmpty() throws Exception {
        CompletableFuture<List<String>> future = emulatorManager.refreshAvdList("", null);
        
        List<String> result = future.get(1, TimeUnit.SECONDS);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Should return empty list when AVD manager path is null")
    void shouldReturnEmptyListWhenAvdManagerPathIsNull() throws Exception {
        CompletableFuture<List<String>> future = emulatorManager.refreshAvdList("somePath", null);
        
        List<String> result = future.get(1, TimeUnit.SECONDS);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle refresh AVD list with valid paths but no AVDs")
    void shouldHandleRefreshAvdListWithValidPathsButNoAvds() throws Exception {
        String testSdkPath = tempDir.toString();
        String testAvdManagerPath = "dummy";
        
        CompletableFuture<List<String>> future = emulatorManager.refreshAvdList(testSdkPath, testAvdManagerPath);
        
        List<String> result = future.get(2, TimeUnit.SECONDS);
        
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should return false when creating AVD with empty SDK path")
    void shouldReturnFalseWhenCreatingAvdWithEmptySdkPath() throws Exception {
        CompletableFuture<Boolean> future = emulatorManager.createAvd("", null, "testAvd", "33", "pixel");
        
        Boolean result = future.get(1, TimeUnit.SECONDS);
        
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should return false when creating AVD with null AVD manager path")
    void shouldReturnFalseWhenCreatingAvdWithNullAvdManagerPath() throws Exception {
        CompletableFuture<Boolean> future = emulatorManager.createAvd("somePath", null, "testAvd", "33", "pixel");
        
        Boolean result = future.get(1, TimeUnit.SECONDS);
        
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should log messages when creating AVD")
    void shouldLogMessagesWhenCreatingAvd() throws Exception {
        String testSdkPath = tempDir.toString();
        String testAvdManagerPath = "dummy";
        
        CompletableFuture<Boolean> future = emulatorManager.createAvd(testSdkPath, testAvdManagerPath, "testAvd", "33", "pixel");
        
        future.get(2, TimeUnit.SECONDS);
        
        verify(mockLogger, atLeastOnce()).accept(anyString());
        verify(mockLocalizationManager, atLeastOnce()).getMessage("log.creating.avd", "testAvd");
    }
    
    @Test
    @DisplayName("Should handle start emulator with valid parameters")
    void shouldHandleStartEmulatorWithValidParameters() {
        String testSdkPath = tempDir.toString();
        String testAvdName = "testAvd";
        
        assertDoesNotThrow(() -> {
            emulatorManager.startEmulator(testSdkPath, testAvdName);
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
    @DisplayName("Should handle start emulator with null parameters")
    void shouldHandleStartEmulatorWithNullParameters() {
        assertDoesNotThrow(() -> {
            emulatorManager.startEmulator(null, null);
        });
    }
    
    @Test
    @DisplayName("Should return false for isEmulatorRunning initially")
    void shouldReturnFalseForIsEmulatorRunningInitially() {
        assertFalse(emulatorManager.isEmulatorRunning());
    }
    
    @Test
    @DisplayName("Should handle stop emulator when no emulator is running")
    void shouldHandleStopEmulatorWhenNoEmulatorIsRunning() {
        assertDoesNotThrow(() -> {
            emulatorManager.stopEmulator();
        });
        
        assertFalse(emulatorManager.isEmulatorRunning());
    }
    
    @Test
    @DisplayName("Should return false when deleting AVD with null AVD manager path")
    void shouldReturnFalseWhenDeletingAvdWithNullAvdManagerPath() throws Exception {
        CompletableFuture<Boolean> future = emulatorManager.deleteAvd("somePath", null, "testAvd");
        
        Boolean result = future.get(1, TimeUnit.SECONDS);
        
        assertFalse(result);
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
    
    @Test
    @DisplayName("Should log messages when deleting AVD")
    void shouldLogMessagesWhenDeletingAvd() throws Exception {
        String testSdkPath = tempDir.toString();
        String testAvdManagerPath = "dummy";
        String testAvdName = "testAvd";
        
        CompletableFuture<Boolean> future = emulatorManager.deleteAvd(testSdkPath, testAvdManagerPath, testAvdName);
        
        future.get(2, TimeUnit.SECONDS);
        
        verify(mockLogger, atLeastOnce()).accept(anyString());
        verify(mockLocalizationManager, atLeastOnce()).getMessage("log.deleting.avd", testAvdName);
    }
    
    @Test
    @DisplayName("Should handle exceptions gracefully during AVD operations")
    void shouldHandleExceptionsGracefullyDuringAvdOperations() throws Exception {
        String testSdkPath = tempDir.toString();
        String testAvdManagerPath = "nonExistentPath";
        
        assertDoesNotThrow(() -> {
            CompletableFuture<Boolean> createFuture = emulatorManager.createAvd(testSdkPath, testAvdManagerPath, "test", "33", "pixel");
            CompletableFuture<Boolean> deleteFuture = emulatorManager.deleteAvd(testSdkPath, testAvdManagerPath, "test");
            CompletableFuture<List<String>> refreshFuture = emulatorManager.refreshAvdList(testSdkPath, testAvdManagerPath);
            
            createFuture.get(2, TimeUnit.SECONDS);
            deleteFuture.get(2, TimeUnit.SECONDS);
            refreshFuture.get(2, TimeUnit.SECONDS);
        });
    }
    
    @Test
    @DisplayName("Should handle empty AVD name in operations")
    void shouldHandleEmptyAvdNameInOperations() throws Exception {
        String testSdkPath = tempDir.toString();
        String testAvdManagerPath = "dummy";
        
        CompletableFuture<Boolean> createFuture = emulatorManager.createAvd(testSdkPath, testAvdManagerPath, "", "33", "pixel");
        CompletableFuture<Boolean> deleteFuture = emulatorManager.deleteAvd(testSdkPath, testAvdManagerPath, "");
        
        assertDoesNotThrow(() -> {
            createFuture.get(2, TimeUnit.SECONDS);
            deleteFuture.get(2, TimeUnit.SECONDS);
        });
    }
}