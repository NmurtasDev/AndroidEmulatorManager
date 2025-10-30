package com.androidemulatormanager;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SdkManager {
    // Using older command line tools compatible with Java 11 (version 10406996)
    private static final String CMDTOOLS_WIN_URL = "https://dl.google.com/android/repository/commandlinetools-win-10406996_latest.zip";
    private static final String CMDTOOLS_LINUX_URL = "https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip";
    private static final String CMDTOOLS_MAC_URL = "https://dl.google.com/android/repository/commandlinetools-mac-10406996_latest.zip";
    
    private final LocalizationManager localization;
    private final Consumer<String> logger;
    private final Consumer<ProgressInfo> progressCallback;
    
    public SdkManager(LocalizationManager localization, Consumer<String> logger, Consumer<ProgressInfo> progressCallback) {
        this.localization = localization;
        this.logger = logger;
        this.progressCallback = progressCallback;
    }
    
    public CompletableFuture<Void> downloadAndSetupSdk(String sdkPath) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.accept(localization.getMessage("log.download.setup.start"));
                logger.accept(localization.getMessage("log.sdk.path", sdkPath));
                
                String os = System.getProperty("os.name").toLowerCase();
                String downloadUrl;
                String zipFileName;
                
                if (os.contains("win")) {
                    downloadUrl = CMDTOOLS_WIN_URL;
                    zipFileName = "commandlinetools-win.zip";
                    logger.accept(localization.getMessage("log.os.detected.windows"));
                } else if (os.contains("mac")) {
                    downloadUrl = CMDTOOLS_MAC_URL;
                    zipFileName = "commandlinetools-mac.zip";
                    logger.accept(localization.getMessage("log.os.detected.macos"));
                } else {
                    downloadUrl = CMDTOOLS_LINUX_URL;
                    zipFileName = "commandlinetools-linux.zip";
                    logger.accept(localization.getMessage("log.os.detected.linux"));
                }
                
                Path sdkDir = Paths.get(sdkPath);
                Files.createDirectories(sdkDir);
                
                progressCallback.accept(new ProgressInfo(5, localization.getMessage("progress.initializing")));
                
                logger.accept(localization.getMessage("log.download.from", downloadUrl));
                Path downloadedFile = sdkDir.resolve(zipFileName);
                
                downloadFile(downloadUrl, downloadedFile);
                
                progressCallback.accept(new ProgressInfo(50, localization.getMessage("progress.extracting")));
                logger.accept(localization.getMessage("log.extraction"));
                
                Path cmdlineToolsPath = sdkDir.resolve("cmdline-tools");
                Files.createDirectories(cmdlineToolsPath);
                
                extractZip(downloadedFile, cmdlineToolsPath);
                
                Path extractedCmdTools = cmdlineToolsPath.resolve("cmdline-tools");
                Path latestPath = cmdlineToolsPath.resolve("latest");
                
                if (Files.exists(extractedCmdTools)) {
                    if (Files.exists(latestPath)) {
                        deleteDirectory(latestPath);
                    }
                    Files.move(extractedCmdTools, latestPath);
                    logger.accept(localization.getMessage("log.folder.renamed"));
                }
                
                progressCallback.accept(new ProgressInfo(70, localization.getMessage("progress.setting.permissions")));
                
                if (!os.contains("win")) {
                    makeFilesExecutable(latestPath.resolve("bin"));
                }
                
                Files.deleteIfExists(downloadedFile);
                
                progressCallback.accept(new ProgressInfo(80, localization.getMessage("progress.installing.components")));
                logger.accept(localization.getMessage("log.download.completed"));
                
                installSdkComponents(sdkPath);
                
                progressCallback.accept(new ProgressInfo(100, localization.getMessage("progress.completed")));
                logger.accept(localization.getMessage("log.setup.completed"));
                
            } catch (Exception e) {
                logger.accept(localization.getMessage("log.download.error", e.getMessage()));
                e.printStackTrace();
            }
        });
    }
    
    public void setupSdk(String sdkPath) {
        CompletableFuture.runAsync(() -> {
            try {
                logger.accept(localization.getMessage("log.setup.start"));
                
                String sdkManagerPath = getSdkManagerPath(sdkPath);
                if (sdkManagerPath == null) {
                    logger.accept(localization.getMessage("log.cmdtools.not.found"));
                    logger.accept(localization.getMessage("log.cmdtools.use.download"));
                    return;
                }
                
                installSdkComponents(sdkPath);
                
            } catch (Exception e) {
                logger.accept(localization.getMessage("log.setup.error", e.getMessage()));
                e.printStackTrace();
            }
        });
    }
    
    public String getSdkManagerPath(String sdkPath) {
        String os = System.getProperty("os.name").toLowerCase();
        String executable = os.contains("win") ? "sdkmanager.bat" : "sdkmanager";
        Path sdkManagerPath = Paths.get(sdkPath, "cmdline-tools", "latest", "bin", executable);
        
        if (Files.exists(sdkManagerPath)) {
            return sdkManagerPath.toString();
        }
        return null;
    }
    
    public String getAvdManagerPath(String sdkPath) {
        String os = System.getProperty("os.name").toLowerCase();
        String executable = os.contains("win") ? "avdmanager.bat" : "avdmanager";
        Path avdManagerPath = Paths.get(sdkPath, "cmdline-tools", "latest", "bin", executable);
        
        if (Files.exists(avdManagerPath)) {
            return avdManagerPath.toString();
        }
        return null;
    }
    
    private void downloadFile(String urlString, Path outputPath) throws IOException {
        URL url = new URL(urlString);
        try (InputStream in = url.openStream()) {
            long fileSize = url.openConnection().getContentLengthLong();
            
            try (FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                byte[] buffer = new byte[8192];
                long totalBytesRead = 0;
                int bytesRead;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    if (fileSize > 0) {
                        int progress = (int) ((totalBytesRead * 40) / fileSize) + 5;
                        String progressText = localization.getMessage("progress.download.format",
                            String.format("%.1f", totalBytesRead / 1024.0 / 1024.0),
                            String.format("%.1f", fileSize / 1024.0 / 1024.0));
                        progressCallback.accept(new ProgressInfo(progress, progressText));
                    }
                }
            }
        }
        logger.accept(localization.getMessage("log.download.file.completed", outputPath.getFileName()));
    }
    
    private void extractZip(Path zipPath, Path destPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = destPath.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }
    
    private void makeFilesExecutable(Path binPath) {
        try {
            if (Files.exists(binPath)) {
                Files.walk(binPath)
                     .filter(Files::isRegularFile)
                     .forEach(file -> {
                         try {
                             Runtime.getRuntime().exec("chmod +x " + file.toString()).waitFor();
                         } catch (Exception e) {
                             logger.accept(localization.getMessage("log.permissions.file.error", file.getFileName()));
                         }
                     });
                logger.accept(localization.getMessage("log.permissions.set"));
            }
        } catch (Exception e) {
            logger.accept(localization.getMessage("log.permissions.error", e.getMessage()));
        }
    }
    
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                 .sorted((a, b) -> b.compareTo(a))
                 .forEach(p -> {
                     try {
                         Files.delete(p);
                     } catch (IOException e) {
                         logger.accept(localization.getMessage("log.cannot.delete", p));
                     }
                 });
        }
    }
    
    private void installSdkComponents(String sdkPath) {
        try {
            String sdkManagerPath = getSdkManagerPath(sdkPath);
            if (sdkManagerPath == null) {
                logger.accept(localization.getMessage("log.sdkmanager.not.found"));
                return;
            }
            
            logger.accept(localization.getMessage("log.licenses.accepting"));
            CommandExecutor.executeCommandWithEnv(new String[]{sdkManagerPath, "--licenses"}, 
                "y\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\n", sdkPath, logger);
            
            String[] components = {
                "platform-tools",
                "platforms;android-33",
                "platforms;android-34",
                "system-images;android-33;google_apis;x86_64",
                "system-images;android-34;google_apis;x86_64",
                "emulator",
                "build-tools;34.0.0"
            };
            
            for (String component : components) {
                logger.accept(localization.getMessage("log.installing.component", component));
                CommandExecutor.executeCommandWithEnv(new String[]{sdkManagerPath, component}, null, sdkPath, logger);
            }
            
            logger.accept(localization.getMessage("log.components.completed"));
            
        } catch (Exception e) {
            logger.accept(localization.getMessage("log.components.error", e.getMessage()));
        }
    }
    
    public static class ProgressInfo {
        public final int value;
        public final String text;
        
        public ProgressInfo(int value, String text) {
            this.value = value;
            this.text = text;
        }
    }
}