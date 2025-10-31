package net.nicolamurtas.android.emulator;

import net.nicolamurtas.android.emulator.controller.MainController;
import net.nicolamurtas.android.emulator.util.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Main entry point for Android Emulator Manager application.
 *
 * A modern Java 21 application for managing Android SDK and emulators.
 * This class serves as the application entry point, delegating all logic
 * to the MainController following MVC pattern.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public class AndroidEmulatorManager {
    private static final Logger logger = LoggerFactory.getLogger(AndroidEmulatorManager.class);

    public static void main(String[] args) {
        logger.info("Starting Android Emulator Manager v3.0");
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("Operating System: {}", PlatformUtils.getOperatingSystem());

        SwingUtilities.invokeLater(() -> {
            MainController controller = new MainController();
            controller.show();
        });
    }
}
