package com.androidemulatormanager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CommandExecutor {
    
    public static boolean executeCommand(String[] command, String input, String sdkPath, Consumer<String> logger) {
        return executeCommandWithEnv(command, input, sdkPath, logger);
    }
    
    public static boolean executeCommandWithEnv(String[] command, String input, String sdkPath, Consumer<String> logger) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.environment().put("ANDROID_HOME", sdkPath);
            pb.environment().put("ANDROID_SDK_ROOT", sdkPath);
            
            // Add SKIP_JDK_VERSION_CHECK to bypass Java version requirement
            pb.environment().put("SKIP_JDK_VERSION_CHECK", "true");
            
            Process process = pb.start();
            
            if (input != null && !input.isEmpty()) {
                try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                    writer.print(input);
                    writer.flush();
                }
            }
            
            CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.accept(line);
                    }
                } catch (Exception e) {
                    logger.accept("Error reading output: " + e.getMessage());
                }
            });
            
            CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.accept("ERROR: " + line);
                    }
                } catch (Exception e) {
                    logger.accept("Error reading error stream: " + e.getMessage());
                }
            });
            
            return process.waitFor() == 0;
            
        } catch (Exception e) {
            logger.accept("Command execution error: " + e.getMessage());
            return false;
        }
    }
}