package net.nicolamurtas.android.emulator.service;

import net.nicolamurtas.android.emulator.util.PlatformUtils;
import net.nicolamurtas.android.emulator.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing Android Virtual Devices (AVDs) and emulators.
 */
public class EmulatorService {
    private static final Logger logger = LoggerFactory.getLogger(EmulatorService.class);

    private final Path sdkPath;
    private final Map<String, Process> runningEmulators;

    public EmulatorService(Path sdkPath) {
        this.sdkPath = sdkPath;
        this.runningEmulators = new ConcurrentHashMap<>();
    }

    /**
     * AVD information record.
     */
    public record AvdInfo(String name, String target, String path) {}

    /**
     * Creates a new Android Virtual Device.
     *
     * @param name AVD name
     * @param apiLevel API level (e.g., "33")
     * @param deviceType Device type (e.g., "pixel_5")
     * @return true if creation was successful
     */
    public boolean createAvd(String name, String apiLevel, String deviceType)
            throws IOException, InterruptedException {

        logger.info("Creating AVD: name={}, api={}, device={}", name, apiLevel, deviceType);

        Path avdManagerPath = getAvdManagerPath();
        if (avdManagerPath == null) {
            throw new IOException("avdmanager not found in SDK: " + sdkPath);
        }

        String systemImage = "system-images;android-" + apiLevel + ";google_apis;x86_64";

        Map<String, String> env = Map.of(
            "ANDROID_HOME", sdkPath.toString(),
            "ANDROID_SDK_ROOT", sdkPath.toString()
        );

        ProcessExecutor.ExecutionResult result = ProcessExecutor.execute(
            sdkPath,
            env,
            10,
            writer -> writer.println("no"), // Don't create custom hardware profile
            avdManagerPath.toString(),
            "create", "avd",
            "-n", name,
            "-k", systemImage,
            "-d", deviceType
        );

        if (result.isSuccess()) {
            logger.info("AVD created successfully: {}", name);
            return true;
        } else {
            logger.error("Failed to create AVD: {}", name);
            logger.error("Errors: {}", result.errors());
            return false;
        }
    }

    /**
     * Lists all available AVDs.
     */
    public List<AvdInfo> listAvds() throws IOException, InterruptedException {
        logger.debug("Listing available AVDs");

        Path avdManagerPath = getAvdManagerPath();
        if (avdManagerPath == null || !Files.exists(avdManagerPath)) {
            logger.warn("avdmanager not found, returning empty list");
            return List.of();
        }

        Map<String, String> env = Map.of(
            "ANDROID_HOME", sdkPath.toString(),
            "ANDROID_SDK_ROOT", sdkPath.toString()
        );

        ProcessExecutor.ExecutionResult result = ProcessExecutor.execute(
            sdkPath, env, 5, null,
            avdManagerPath.toString(), "list", "avd"
        );

        return parseAvdList(result.output());
    }

    /**
     * Parses AVD list output.
     */
    private List<AvdInfo> parseAvdList(List<String> output) {
        List<AvdInfo> avds = new ArrayList<>();
        String currentName = null;
        String currentTarget = null;
        String currentPath = null;

        for (String line : output) {
            line = line.trim();

            if (line.startsWith("Name:")) {
                currentName = line.substring(5).trim();
            } else if (line.startsWith("Target:")) {
                currentTarget = line.substring(7).trim();
            } else if (line.startsWith("Path:")) {
                currentPath = line.substring(5).trim();

                // End of AVD entry
                if (currentName != null) {
                    avds.add(new AvdInfo(currentName, currentTarget, currentPath));
                    currentName = null;
                    currentTarget = null;
                    currentPath = null;
                }
            }
        }

        logger.debug("Found {} AVDs", avds.size());
        return avds;
    }

    /**
     * Starts an emulator with the given AVD name.
     *
     * @param avdName Name of the AVD to start
     * @return The emulator process
     */
    public Process startEmulator(String avdName) throws IOException {
        logger.info("Starting emulator: {}", avdName);

        if (runningEmulators.containsKey(avdName)) {
            Process existing = runningEmulators.get(avdName);
            if (existing.isAlive()) {
                logger.warn("Emulator {} is already running", avdName);
                return existing;
            } else {
                runningEmulators.remove(avdName);
            }
        }

        Path emulatorPath = getEmulatorPath();
        if (emulatorPath == null || !Files.exists(emulatorPath)) {
            throw new IOException("Emulator executable not found in SDK: " + sdkPath);
        }

        Map<String, String> env = Map.of(
            "ANDROID_HOME", sdkPath.toString(),
            "ANDROID_SDK_ROOT", sdkPath.toString()
        );

        Process process = ProcessExecutor.executeAsync(
            sdkPath, env,
            emulatorPath.toString(), "-avd", avdName
        );

        runningEmulators.put(avdName, process);
        logger.info("Emulator {} started (PID: {})", avdName, process.pid());

        return process;
    }

    /**
     * Stops a running emulator.
     */
    public void stopEmulator(String avdName) {
        logger.info("Stopping emulator: {}", avdName);

        Process process = runningEmulators.get(avdName);
        if (process != null) {
            ProcessExecutor.killProcess(process);
            runningEmulators.remove(avdName);
            logger.info("Emulator {} stopped", avdName);
        } else {
            logger.warn("No running emulator found for: {}", avdName);
        }
    }

    /**
     * Stops all running emulators.
     */
    public void stopAllEmulators() {
        logger.info("Stopping all running emulators");
        new ArrayList<>(runningEmulators.keySet()).forEach(this::stopEmulator);
    }

    /**
     * Deletes an AVD.
     */
    public boolean deleteAvd(String avdName) throws IOException, InterruptedException {
        logger.info("Deleting AVD: {}", avdName);

        // Stop emulator if running
        stopEmulator(avdName);

        Path avdManagerPath = getAvdManagerPath();
        if (avdManagerPath == null) {
            throw new IOException("avdmanager not found in SDK: " + sdkPath);
        }

        Map<String, String> env = Map.of(
            "ANDROID_HOME", sdkPath.toString(),
            "ANDROID_SDK_ROOT", sdkPath.toString()
        );

        ProcessExecutor.ExecutionResult result = ProcessExecutor.execute(
            sdkPath, env, 5, null,
            avdManagerPath.toString(), "delete", "avd", "-n", avdName
        );

        if (result.isSuccess()) {
            logger.info("AVD deleted successfully: {}", avdName);
            return true;
        } else {
            logger.error("Failed to delete AVD: {}", avdName);
            logger.error("Errors: {}", result.errors());
            return false;
        }
    }

    /**
     * Gets the list of running emulators.
     */
    public Map<String, Process> getRunningEmulators() {
        // Clean up dead processes
        runningEmulators.entrySet().removeIf(entry -> !entry.getValue().isAlive());
        return Collections.unmodifiableMap(runningEmulators);
    }

    /**
     * Checks if an emulator is running.
     */
    public boolean isEmulatorRunning(String avdName) {
        Process process = runningEmulators.get(avdName);
        return process != null && process.isAlive();
    }

    /**
     * Gets the path to avdmanager executable.
     */
    private Path getAvdManagerPath() {
        String executable = "avdmanager" + PlatformUtils.getExecutableExtension();
        Path path = sdkPath.resolve("cmdline-tools")
            .resolve("latest")
            .resolve("bin")
            .resolve(executable);

        return Files.exists(path) ? path : null;
    }

    /**
     * Gets the path to emulator executable.
     */
    private Path getEmulatorPath() {
        String executable = "emulator" + PlatformUtils.getBinaryExtension();
        Path path = sdkPath.resolve("emulator").resolve(executable);

        return Files.exists(path) ? path : null;
    }
}
