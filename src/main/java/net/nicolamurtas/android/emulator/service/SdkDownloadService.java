package net.nicolamurtas.android.emulator.service;

import net.nicolamurtas.android.emulator.util.PlatformUtils;
import net.nicolamurtas.android.emulator.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service for downloading and installing Android SDK.
 */
public class SdkDownloadService {
    private static final Logger logger = LoggerFactory.getLogger(SdkDownloadService.class);

    private static final String[] SDK_COMPONENTS = {
        "platform-tools",
        "platforms;android-33",
        "platforms;android-34",
        "system-images;android-33;google_apis;x86_64",
        "system-images;android-34;google_apis;x86_64",
        "emulator",
        "build-tools;34.0.0"
    };

    /**
     * Downloads and installs the Android SDK to the specified path.
     *
     * @param sdkPath Target SDK installation path
     * @param progressCallback Callback for progress updates (progress 0-100, message)
     * @throws IOException If download or extraction fails
     */
    public void downloadAndInstallSdk(Path sdkPath, BiConsumer<Integer, String> progressCallback)
            throws IOException, InterruptedException {

        logger.info("Starting SDK download and installation to: {}", sdkPath);

        // Create SDK directory
        Files.createDirectories(sdkPath);
        updateProgress(progressCallback, 5, "Inizializzazione download...");

        // Download command line tools
        String downloadUrl = PlatformUtils.getSdkToolsDownloadUrl();
        String fileName = PlatformUtils.getSdkToolsFileName();
        Path downloadedFile = sdkPath.resolve(fileName);

        logger.info("Downloading SDK tools from: {}", downloadUrl);
        downloadFile(downloadUrl, downloadedFile, progressCallback);

        updateProgress(progressCallback, 50, "Estrazione archivio...");
        logger.info("Extracting archive...");

        // Extract archive
        Path cmdlineToolsPath = sdkPath.resolve("cmdline-tools");
        Files.createDirectories(cmdlineToolsPath);
        extractZip(downloadedFile, cmdlineToolsPath);

        // Rename cmdline-tools/cmdline-tools to cmdline-tools/latest
        Path extractedCmdTools = cmdlineToolsPath.resolve("cmdline-tools");
        Path latestPath = cmdlineToolsPath.resolve("latest");

        if (Files.exists(extractedCmdTools)) {
            if (Files.exists(latestPath)) {
                deleteDirectory(latestPath);
            }
            Files.move(extractedCmdTools, latestPath);
            logger.info("Renamed cmdline-tools directory to 'latest'");
        }

        updateProgress(progressCallback, 70, "Configurazione permessi...");

        // Make files executable on Unix-like systems
        if (!PlatformUtils.isWindows()) {
            Path binPath = latestPath.resolve("bin");
            PlatformUtils.makeDirectoryExecutable(binPath);
        }

        // Clean up downloaded archive
        Files.deleteIfExists(downloadedFile);

        updateProgress(progressCallback, 80, "Installazione componenti SDK...");
        logger.info("Installing SDK components...");

        // Install SDK components
        installSdkComponents(sdkPath, progressCallback);

        updateProgress(progressCallback, 100, "Completato!");
        logger.info("SDK installation completed successfully");
    }

    /**
     * Downloads a file from a URL with progress reporting.
     */
    private void downloadFile(String urlString, Path outputPath,
                            BiConsumer<Integer, String> progressCallback) throws IOException {
        URL url = new URL(urlString);

        try (InputStream in = url.openStream();
             FileOutputStream out = new FileOutputStream(outputPath.toFile())) {

            long fileSize = url.openConnection().getContentLengthLong();
            byte[] buffer = new byte[8192];
            long totalBytesRead = 0;
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                if (fileSize > 0) {
                    int progress = (int) ((totalBytesRead * 40) / fileSize) + 5; // 5-45%
                    String progressText = String.format("Download: %.1f MB / %.1f MB",
                        totalBytesRead / 1024.0 / 1024.0,
                        fileSize / 1024.0 / 1024.0);
                    updateProgress(progressCallback, progress, progressText);
                }
            }
        }

        logger.info("Download completed: {}", outputPath.getFileName());
    }

    /**
     * Extracts a ZIP archive.
     */
    private void extractZip(Path zipPath, Path destPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = destPath.resolve(entry.getName());

                // Security check: prevent zip slip vulnerability
                if (!entryPath.normalize().startsWith(destPath.normalize())) {
                    throw new IOException("Zip entry is outside target directory: " + entry.getName());
                }

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

    /**
     * Installs SDK components using sdkmanager.
     */
    private void installSdkComponents(Path sdkPath, BiConsumer<Integer, String> progressCallback)
            throws IOException, InterruptedException {

        Path sdkManagerPath = getSdkManagerPath(sdkPath);

        if (sdkManagerPath == null || !Files.exists(sdkManagerPath)) {
            throw new IOException("sdkmanager not found in: " + sdkPath);
        }

        Map<String, String> env = Map.of(
            "ANDROID_HOME", sdkPath.toString(),
            "ANDROID_SDK_ROOT", sdkPath.toString()
        );

        // Accept licenses
        logger.info("Accepting SDK licenses...");
        ProcessExecutor.ExecutionResult licenseResult = ProcessExecutor.execute(
            sdkPath,
            env,
            10,
            writer -> {
                // Send 'y' multiple times to accept all licenses
                for (int i = 0; i < 20; i++) {
                    writer.println("y");
                }
            },
            sdkManagerPath.toString(), "--licenses"
        );

        if (!licenseResult.isSuccess()) {
            logger.warn("License acceptance may have failed, continuing anyway");
        }

        // Install each component
        int componentIndex = 0;
        for (String component : SDK_COMPONENTS) {
            logger.info("Installing component: {}", component);
            int progress = 80 + (componentIndex * 15 / SDK_COMPONENTS.length);
            updateProgress(progressCallback, progress, "Installazione " + component + "...");

            ProcessExecutor.ExecutionResult result = ProcessExecutor.execute(
                sdkPath,
                env,
                30, // 30 minutes timeout per component
                null,
                sdkManagerPath.toString(), component
            );

            if (result.isSuccess()) {
                logger.info("Successfully installed: {}", component);
            } else {
                logger.error("Failed to install component: {}", component);
                logger.error("Errors: {}", result.errors());
            }

            componentIndex++;
        }

        logger.info("SDK components installation completed");
    }

    /**
     * Gets the path to sdkmanager executable.
     */
    private Path getSdkManagerPath(Path sdkPath) {
        String executable = "sdkmanager" + PlatformUtils.getExecutableExtension();
        return sdkPath.resolve("cmdline-tools")
            .resolve("latest")
            .resolve("bin")
            .resolve(executable);
    }

    /**
     * Deletes a directory recursively.
     */
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted((a, b) -> b.compareTo(a)) // Reverse order for proper deletion
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        logger.warn("Failed to delete: {}", p, e);
                    }
                });
        }
    }

    /**
     * Helper to update progress safely.
     */
    private void updateProgress(BiConsumer<Integer, String> callback, int progress, String message) {
        if (callback != null) {
            callback.accept(progress, message);
        }
    }
}
