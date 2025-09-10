package com.androidemulatormanager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class EmulatorManager {
    private final LocalizationManager localization;
    private final Consumer<String> logger;
    private Process emulatorProcess;
    
    public EmulatorManager(LocalizationManager localization, Consumer<String> logger) {
        this.localization = localization;
        this.logger = logger;
    }
    
    public CompletableFuture<List<String>> refreshAvdList(String sdkPath, String avdManagerPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (sdkPath.isEmpty() || avdManagerPath == null) {
                    return new ArrayList<>();
                }
                
                ProcessBuilder pb = new ProcessBuilder(avdManagerPath, "list", "avd");
                pb.environment().put("ANDROID_HOME", sdkPath);
                pb.environment().put("ANDROID_SDK_ROOT", sdkPath);
                
                Process process = pb.start();
                
                List<String> avds = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().startsWith("Name:")) {
                            String avdName = line.split(":", 2)[1].trim();
                            avds.add(avdName);
                        }
                    }
                }
                
                return avds;
                
            } catch (Exception e) {
                logger.accept(localization.getMessage("log.refresh.error", e.getMessage()));
                return new ArrayList<>();
            }
        });
    }
    
    public CompletableFuture<Boolean> createAvd(String sdkPath, String avdManagerPath, String name, String apiLevel, String deviceType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (sdkPath.isEmpty() || avdManagerPath == null) {
                    return false;
                }
                
                String systemImage = "system-images;android-" + apiLevel + ";google_apis;x86_64";
                
                logger.accept(localization.getMessage("log.creating.avd", name));
                
                String[] command = {
                    avdManagerPath, "create", "avd",
                    "-n", name,
                    "-k", systemImage,
                    "-d", deviceType
                };
                
                if (CommandExecutor.executeCommand(command, "no\n", sdkPath, logger)) {
                    logger.accept(localization.getMessage("log.avd.created", name));
                    return true;
                } else {
                    logger.accept(localization.getMessage("log.avd.creation.error"));
                    return false;
                }
                
            } catch (Exception e) {
                logger.accept(localization.getMessage("log.command.error", e.getMessage()));
                return false;
            }
        });
    }
    
    public void startEmulator(String sdkPath, String avdName) {
        CompletableFuture.runAsync(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                String executable = os.contains("win") ? "emulator.exe" : "emulator";
                String emulatorPath = Paths.get(sdkPath, "emulator", executable).toString();
                
                ProcessBuilder pb = new ProcessBuilder(emulatorPath, "-avd", avdName);
                pb.environment().put("ANDROID_HOME", sdkPath);
                pb.environment().put("ANDROID_SDK_ROOT", sdkPath);
                
                logger.accept(localization.getMessage("log.emulator.starting", avdName));
                emulatorProcess = pb.start();
                logger.accept(localization.getMessage("log.emulator.started", avdName, emulatorProcess.pid()));
                
            } catch (Exception e) {
                logger.accept(localization.getMessage("log.emulator.start.error", e.getMessage()));
            }
        });
    }
    
    public void stopEmulator() {
        if (emulatorProcess != null && emulatorProcess.isAlive()) {
            emulatorProcess.destroy();
            logger.accept(localization.getMessage("log.emulator.stopped"));
            emulatorProcess = null;
        }
    }
    
    public boolean isEmulatorRunning() {
        return emulatorProcess != null && emulatorProcess.isAlive();
    }
    
    public CompletableFuture<Boolean> deleteAvd(String sdkPath, String avdManagerPath, String avdName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (avdManagerPath == null) {
                    logger.accept(localization.getMessage("log.sdkmanager.not.found"));
                    return false;
                }
                
                logger.accept(localization.getMessage("log.deleting.avd", avdName));
                
                String[] command = {avdManagerPath, "delete", "avd", "-n", avdName};
                
                if (CommandExecutor.executeCommand(command, null, sdkPath, logger)) {
                    logger.accept(localization.getMessage("log.avd.deleted", avdName));
                    return true;
                } else {
                    logger.accept(localization.getMessage("log.avd.deletion.error"));
                    return false;
                }
                
            } catch (Exception e) {
                logger.accept(localization.getMessage("log.command.error", e.getMessage()));
                return false;
            }
        });
    }
}