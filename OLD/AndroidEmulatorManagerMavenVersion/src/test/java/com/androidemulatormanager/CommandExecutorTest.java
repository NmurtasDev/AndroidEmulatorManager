package com.androidemulatormanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("CommandExecutor Tests")
class CommandExecutorTest {
    
    @Mock
    private Consumer<String> mockLogger;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("Should handle null command gracefully")
    void shouldHandleNullCommandGracefully() {
        boolean result = CommandExecutor.executeCommand(null, null, "/test/sdk", mockLogger);
        
        assertFalse(result);
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
    
    @Test
    @DisplayName("Should handle empty command array gracefully")
    void shouldHandleEmptyCommandArrayGracefully() {
        boolean result = CommandExecutor.executeCommand(new String[]{}, null, "/test/sdk", mockLogger);
        
        assertFalse(result);
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
    
    @Test
    @DisplayName("Should handle null SDK path gracefully")
    void shouldHandleNullSdkPathGracefully() {
        String[] command = {"echo", "test"};
        
        assertDoesNotThrow(() -> {
            CommandExecutor.executeCommand(command, null, null, mockLogger);
        });
    }
    
    @Test
    @DisplayName("Should handle null logger gracefully")
    void shouldHandleNullLoggerGracefully() {
        String[] command = {"echo", "test"};
        
        assertDoesNotThrow(() -> {
            CommandExecutor.executeCommand(command, null, "/test/sdk", null);
        });
    }
    
    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("Should execute simple Windows command successfully")
    void shouldExecuteSimpleWindowsCommandSuccessfully() {
        String[] command = {"cmd", "/c", "echo test"};
        
        boolean result = CommandExecutor.executeCommand(command, null, "/test/sdk", mockLogger);
        
        assertTrue(result);
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
    
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    @DisplayName("Should execute simple Unix command successfully")
    void shouldExecuteSimpleUnixCommandSuccessfully() {
        String[] command = {"echo", "test"};
        
        boolean result = CommandExecutor.executeCommand(command, null, "/test/sdk", mockLogger);
        
        assertTrue(result);
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
    
    @Test
    @DisplayName("Should handle command with input")
    void shouldHandleCommandWithInput() {
        String[] command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            command = new String[]{"cmd", "/c", "more"};
        } else {
            command = new String[]{"cat"};
        }
        String input = "test input\n";
        
        assertDoesNotThrow(() -> {
            CommandExecutor.executeCommand(command, input, "/test/sdk", mockLogger);
        });
    }
    
    @Test
    @DisplayName("Should handle command with empty input")
    void shouldHandleCommandWithEmptyInput() {
        String[] command = {"echo", "test"};
        String input = "";
        
        assertDoesNotThrow(() -> {
            CommandExecutor.executeCommand(command, input, "/test/sdk", mockLogger);
        });
    }
    
    @Test
    @DisplayName("Should handle command with multiline input")
    void shouldHandleCommandWithMultilineInput() {
        String[] command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            command = new String[]{"cmd", "/c", "more"};
        } else {
            command = new String[]{"cat"};
        }
        String input = "line1\nline2\nline3\n";
        
        assertDoesNotThrow(() -> {
            CommandExecutor.executeCommand(command, input, "/test/sdk", mockLogger);
        });
    }
    
    @Test
    @DisplayName("Should return false for invalid command")
    void shouldReturnFalseForInvalidCommand() {
        String[] command = {"nonexistentcommand12345"};
        
        boolean result = CommandExecutor.executeCommand(command, null, "/test/sdk", mockLogger);
        
        assertFalse(result);
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
    
    @Test
    @DisplayName("Should handle command that exits with non-zero code")
    void shouldHandleCommandThatExitsWithNonZeroCode() {
        String[] command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            command = new String[]{"cmd", "/c", "exit 1"};
        } else {
            command = new String[]{"sh", "-c", "exit 1"};
        }
        
        boolean result = CommandExecutor.executeCommand(command, null, "/test/sdk", mockLogger);
        
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should set environment variables correctly")
    void shouldSetEnvironmentVariablesCorrectly() {
        String[] command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            command = new String[]{"cmd", "/c", "echo %ANDROID_HOME%"};
        } else {
            command = new String[]{"sh", "-c", "echo $ANDROID_HOME"};
        }
        String testSdkPath = "/test/android/sdk";
        
        boolean result = CommandExecutor.executeCommand(command, null, testSdkPath, mockLogger);
        
        assertTrue(result);
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
    
    @Test
    @DisplayName("Should handle long-running command gracefully")
    void shouldHandleLongRunningCommandGracefully() {
        String[] command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            command = new String[]{"cmd", "/c", "ping -n 1 127.0.0.1"};
        } else {
            command = new String[]{"sleep", "0.1"};
        }
        
        long startTime = System.currentTimeMillis();
        boolean result = CommandExecutor.executeCommand(command, null, "/test/sdk", mockLogger);
        long endTime = System.currentTimeMillis();
        
        assertTrue(result);
        assertTrue(endTime - startTime >= 50); // Command should take some time
    }
    
    @Test
    @DisplayName("Should handle command with special characters")
    void shouldHandleCommandWithSpecialCharacters() {
        String[] command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            command = new String[]{"cmd", "/c", "echo Special chars: !@#$%"};
        } else {
            command = new String[]{"echo", "Special chars: !@#$%"};
        }
        
        boolean result = CommandExecutor.executeCommand(command, null, "/test/sdk", mockLogger);
        
        assertTrue(result);
        verify(mockLogger, atLeastOnce()).accept(anyString());
    }
}