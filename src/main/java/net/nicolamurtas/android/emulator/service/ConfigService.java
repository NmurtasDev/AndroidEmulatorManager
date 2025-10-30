package net.nicolamurtas.android.emulator.service;

import net.nicolamurtas.android.emulator.util.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * Service for managing application configuration persistence.
 */
public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private static final String CONFIG_FILE = "android_emulator_config.properties";
    private static final String SDK_PATH_KEY = "sdk.path";

    private final Path configFilePath;
    private final Properties properties;

    public ConfigService() {
        this.configFilePath = Paths.get(CONFIG_FILE);
        this.properties = new Properties();
        loadConfig();
    }

    /**
     * Loads configuration from file.
     */
    private void loadConfig() {
        if (!Files.exists(configFilePath)) {
            logger.info("Configuration file not found, using defaults");
            return;
        }

        try (InputStream input = Files.newInputStream(configFilePath)) {
            properties.load(input);
            logger.info("Configuration loaded from: {}", configFilePath);
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
        }
    }

    /**
     * Saves configuration to file.
     */
    public void saveConfig() {
        try (OutputStream output = Files.newOutputStream(configFilePath)) {
            properties.store(output, "Android Emulator Manager Configuration");
            logger.info("Configuration saved to: {}", configFilePath);
        } catch (IOException e) {
            logger.error("Failed to save configuration", e);
        }
    }

    /**
     * Gets the configured SDK path, or the default if not set.
     */
    public Path getSdkPath() {
        String pathString = properties.getProperty(SDK_PATH_KEY);
        if (pathString != null && !pathString.isEmpty()) {
            return Paths.get(pathString);
        }
        return PlatformUtils.getDefaultSdkPath();
    }

    /**
     * Sets the SDK path.
     */
    public void setSdkPath(Path path) {
        properties.setProperty(SDK_PATH_KEY, path.toString());
        logger.info("SDK path set to: {}", path);
    }

    /**
     * Gets a configuration value.
     */
    public Optional<String> getValue(String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }

    /**
     * Sets a configuration value.
     */
    public void setValue(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Removes a configuration value.
     */
    public void removeValue(String key) {
        properties.remove(key);
    }

    /**
     * Checks if SDK is configured and valid.
     */
    public boolean isSdkConfigured() {
        Path sdkPath = getSdkPath();
        if (!Files.exists(sdkPath)) {
            return false;
        }

        // Check for essential SDK components
        Path cmdlineTools = sdkPath.resolve("cmdline-tools").resolve("latest");
        Path platformTools = sdkPath.resolve("platform-tools");

        return Files.exists(cmdlineTools) || Files.exists(platformTools);
    }
}
