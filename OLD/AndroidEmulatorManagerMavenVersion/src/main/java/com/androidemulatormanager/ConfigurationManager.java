package com.androidemulatormanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConfigurationManager {
    private static final String CONFIG_FILE = "android_emulator_config.properties";
    private String sdkPath = "";
    private final LocalizationManager localization;
    
    public ConfigurationManager(LocalizationManager localization) {
        this.localization = localization;
        loadConfig();
    }
    
    public String getSdkPath() {
        return sdkPath;
    }
    
    public void setSdkPath(String sdkPath) {
        this.sdkPath = sdkPath != null ? sdkPath : "";
        saveConfig();
    }
    
    private void saveConfig() {
        Properties props = new Properties();
        props.setProperty("sdk_path", sdkPath);
        
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Android Emulator Manager Configuration");
        } catch (Exception e) {
            System.err.println(localization.getMessage("log.config.save.error", e.getMessage()));
        }
    }
    
    private void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
            sdkPath = props.getProperty("sdk_path", "");
        } catch (Exception e) {
            // Ignore loading errors
        }
    }
}