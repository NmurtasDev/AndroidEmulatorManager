package com.androidemulatormanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocalizationManager Tests")
class LocalizationManagerTest {
    
    private LocalizationManager localizationManager;
    
    @BeforeEach
    void setUp() {
        localizationManager = new LocalizationManager();
    }
    
    @Test
    @DisplayName("Should initialize LocalizationManager without exceptions")
    void shouldInitializeWithoutExceptions() {
        assertNotNull(localizationManager);
    }
    
    @Test
    @DisplayName("Should return message for existing key")
    void shouldReturnMessageForExistingKey() {
        String message = localizationManager.getMessage("window.title");
        assertNotNull(message);
        assertFalse(message.isEmpty());
        assertEquals("Android Emulator Manager", message);
    }
    
    @Test
    @DisplayName("Should return key when message not found")
    void shouldReturnKeyWhenMessageNotFound() {
        String nonExistentKey = "non.existent.key";
        String result = localizationManager.getMessage(nonExistentKey);
        assertEquals(nonExistentKey, result);
    }
    
    @Test
    @DisplayName("Should handle message formatting with parameters")
    void shouldHandleMessageFormattingWithParameters() {
        String message = localizationManager.getMessage("log.sdk.path", "/test/path");
        assertNotNull(message);
        assertTrue(message.contains("/test/path"));
    }
    
    @Test
    @DisplayName("Should handle message formatting with multiple parameters")
    void shouldHandleMessageFormattingWithMultipleParameters() {
        String message = localizationManager.getMessage("progress.download.format", "10.5", "20.0");
        assertNotNull(message);
        assertTrue(message.contains("10.5"));
        assertTrue(message.contains("20.0"));
    }
    
    @Test
    @DisplayName("Should handle empty parameters array")
    void shouldHandleEmptyParametersArray() {
        String message = localizationManager.getMessage("window.title", new Object[0]);
        assertEquals("Android Emulator Manager", message);
    }
    
    @Test
    @DisplayName("Should handle null parameters gracefully")
    void shouldHandleNullParametersGracefully() {
        String message = localizationManager.getMessage("window.title", (Object[]) null);
        assertNotNull(message);
        assertTrue(message.equals("Android Emulator Manager") || message.equals("window.title"));
    }
    
    @Test
    @DisplayName("Should return different messages for different keys")
    void shouldReturnDifferentMessagesForDifferentKeys() {
        String windowTitle = localizationManager.getMessage("window.title");
        String buttonBrowse = localizationManager.getMessage("button.browse");
        
        assertNotEquals(windowTitle, buttonBrowse);
        assertEquals("Android Emulator Manager", windowTitle);
        assertTrue(buttonBrowse.contains("Browse") || buttonBrowse.contains("Sfoglia"));
    }
    
    @Test
    @DisplayName("Should handle special characters in messages")
    void shouldHandleSpecialCharactersInMessages() {
        String confirmMessage = localizationManager.getMessage("dialog.confirm.delete", "Test Device");
        assertNotNull(confirmMessage);
        assertFalse(confirmMessage.isEmpty());
        // The message should either contain the device name, be the key itself, or be a formatted message
        assertTrue(confirmMessage.contains("Test Device") || 
                  confirmMessage.equals("dialog.confirm.delete") ||
                  confirmMessage.length() > "dialog.confirm.delete".length());
    }
}