package net.nicolamurtas.android.emulator.controller;

import net.nicolamurtas.android.emulator.service.ConfigService;
import net.nicolamurtas.android.emulator.service.EmulatorService;
import net.nicolamurtas.android.emulator.service.SdkDownloadService;
import net.nicolamurtas.android.emulator.util.PlatformUtils;
import net.nicolamurtas.android.emulator.view.AvdGridPanel;
import net.nicolamurtas.android.emulator.view.DialogFactory;
import net.nicolamurtas.android.emulator.view.MainView;
import net.nicolamurtas.android.emulator.view.SdkConfigPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Main controller for the Android Emulator Manager application.
 * Coordinates between views and services, handling all business logic.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final MainView view;
    private final ConfigService configService;
    private final SdkDownloadService sdkDownloadService;
    private EmulatorService emulatorService;

    public MainController() {
        this.configService = new ConfigService();
        this.sdkDownloadService = new SdkDownloadService();

        // Initialize emulator service if SDK is configured
        Path sdkPath = configService.getSdkPath();
        if (Files.exists(sdkPath)) {
            this.emulatorService = new EmulatorService(sdkPath);
        }

        // Create view
        this.view = new MainView(configService.isSdkConfigured());

        // Initialize view components
        initializeView();

        // Setup event handlers
        setupEventHandlers();

        // Load initial data
        loadConfiguration();
        refreshAvdList();

        logger.info("Android Emulator Manager controller initialized");
    }

    private void initializeView() {
        // Set window close handler
        view.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleWindowClosing();
            }
        });
    }

    private void setupEventHandlers() {
        SdkConfigPanel sdkPanel = view.getSdkConfigPanel();
        AvdGridPanel avdPanel = view.getAvdGridPanel();

        // SDK panel actions
        sdkPanel.setOnBrowse(this::handleBrowseSdk);
        sdkPanel.setOnDownload(this::handleDownloadSdk);
        sdkPanel.setOnVerify(this::handleVerifySdk);

        // AVD panel actions
        avdPanel.setOnCreateAvd(this::handleCreateAvd);
        avdPanel.setOnRefresh(this::refreshAvdList);
        avdPanel.setOnStartEmulator(this::handleStartEmulator);
        avdPanel.setOnStopEmulator(this::handleStopEmulator);
        avdPanel.setOnRenameAvd(this::handleRenameAvd);
        avdPanel.setOnDeleteAvd(this::handleDeleteAvd);

        // Set emulator service in AVD panel for status checking
        if (emulatorService != null) {
            avdPanel.setEmulatorService(emulatorService);
        }
    }

    private void loadConfiguration() {
        Path sdkPath = configService.getSdkPath();
        view.getSdkConfigPanel().setSdkPath(sdkPath.toString());
    }

    private void handleBrowseSdk() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Android SDK Directory");

        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            view.getSdkConfigPanel().setSdkPath(path.toString());
            configService.setSdkPath(path);
            configService.saveConfig();
            emulatorService = new EmulatorService(path);
            view.getAvdGridPanel().setEmulatorService(emulatorService);
            view.getSdkConfigPanel().setConfigured(true);
            view.log("SDK path set to: " + path);
        }
    }

    private void handleDownloadSdk() {
        String pathText = view.getSdkConfigPanel().getSdkPath().trim();
        if (pathText.isEmpty()) {
            pathText = PlatformUtils.getDefaultSdkPath().toString();
            view.getSdkConfigPanel().setSdkPath(pathText);
        }

        Path sdkPath = Path.of(pathText);

        // Show license agreement first
        if (!DialogFactory.showLicenseAgreementDialog(view)) {
            view.log("SDK download cancelled: License not accepted");
            return;
        }

        // Show component selection dialog
        List<String> selectedComponents = DialogFactory.showSdkComponentSelectionDialog(view);
        if (selectedComponents == null || selectedComponents.isEmpty()) {
            view.log("SDK download cancelled by user");
            return;
        }

        new Thread(() -> {
            try {
                view.showProgress(true);
                view.log("=== Starting SDK Download ===");
                view.log("Target path: " + sdkPath);
                view.log("Selected components: " + selectedComponents.size());

                sdkDownloadService.downloadAndInstallSdk(sdkPath, selectedComponents,
                    (progress, message) -> {
                        view.updateProgress(progress, message);
                        view.log(message);
                    });

                configService.setSdkPath(sdkPath);
                configService.saveConfig();
                emulatorService = new EmulatorService(sdkPath);
                view.getAvdGridPanel().setEmulatorService(emulatorService);
                view.getSdkConfigPanel().setConfigured(true);

                view.log("=== SDK Installation Completed Successfully ===");
                JOptionPane.showMessageDialog(view,
                    "SDK downloaded and installed successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                logger.error("SDK download failed", e);
                view.log("ERROR: " + e.getMessage());
                JOptionPane.showMessageDialog(view,
                    "SDK download failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                view.showProgress(false);
            }
        }).start();
    }

    private void handleVerifySdk() {
        if (configService.isSdkConfigured()) {
            JOptionPane.showMessageDialog(view,
                "SDK is properly configured!",
                "SDK Verification", JOptionPane.INFORMATION_MESSAGE);
            view.log("SDK verification: OK");
        } else {
            JOptionPane.showMessageDialog(view,
                "SDK is not properly configured. Please download SDK first.",
                "SDK Verification", JOptionPane.WARNING_MESSAGE);
            view.log("SDK verification: FAILED");
        }
    }

    private void handleCreateAvd() {
        if (emulatorService == null) {
            JOptionPane.showMessageDialog(view,
                "Please configure SDK first",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DialogFactory.AvdCreationParams params = DialogFactory.showCreateAvdDialog(view);
        if (params == null) {
            return; // User cancelled or invalid input
        }

        new Thread(() -> {
            try {
                view.log("Creating AVD: " + params.name + " (API " + params.apiLevel + ")");
                view.showProgress(true);

                boolean success = emulatorService.createAvd(
                    params.name,
                    params.apiLevel,
                    params.device,
                    (progress, message) -> {
                        view.updateProgress(progress, message);
                        view.log(message);
                    }
                );

                if (success) {
                    view.log("AVD created successfully");
                    refreshAvdList();
                } else {
                    view.log("ERROR: Failed to create AVD");
                }
            } catch (Exception e) {
                logger.error("Failed to create AVD", e);
                view.log("ERROR: " + e.getMessage());
            } finally {
                view.showProgress(false);
            }
        }).start();
    }

    private void handleStartEmulator(String avdName) {
        if (emulatorService == null) {
            JOptionPane.showMessageDialog(view,
                "EmulatorService not initialized",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                view.log("Starting emulator: " + avdName);
                emulatorService.startEmulator(avdName);
                view.log("Emulator started: " + avdName);
                refreshAvdList(); // Refresh to update status
            } catch (Exception e) {
                logger.error("Failed to start emulator", e);
                view.log("ERROR: " + e.getMessage());
            }
        }).start();
    }

    private void handleStopEmulator(String avdName) {
        if (emulatorService == null) {
            JOptionPane.showMessageDialog(view,
                "EmulatorService not initialized",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        emulatorService.stopEmulator(avdName);
        view.log("Emulator stopped: " + avdName);
        refreshAvdList(); // Refresh to update status
    }

    private void handleDeleteAvd(String avdName) {
        if (!DialogFactory.showDeleteConfirmation(view, avdName)) {
            return;
        }

        new Thread(() -> {
            try {
                view.log("Deleting AVD: " + avdName);
                boolean success = emulatorService.deleteAvd(avdName);
                if (success) {
                    view.log("AVD deleted successfully");
                    refreshAvdList();
                } else {
                    view.log("ERROR: Failed to delete AVD");
                }
            } catch (Exception e) {
                logger.error("Failed to delete AVD", e);
                view.log("ERROR: " + e.getMessage());
            }
        }).start();
    }

    private void handleRenameAvd(String oldName) {
        String newName = DialogFactory.showRenameAvdDialog(view, oldName);
        if (newName == null) {
            return; // User cancelled or invalid name
        }

        new Thread(() -> {
            try {
                view.log("Renaming AVD: " + oldName + " -> " + newName);

                // Get AVD path
                EmulatorService.AvdInfo avdInfo = view.getAvdGridPanel().getAllAvds().stream()
                    .filter(avd -> avd.name().equals(oldName))
                    .findFirst()
                    .orElse(null);

                if (avdInfo == null || avdInfo.path() == null) {
                    view.log("ERROR: Could not find AVD path");
                    return;
                }

                Path avdPath = Path.of(avdInfo.path());
                Path iniFile = avdPath.getParent().resolve(oldName + ".ini");
                Path newAvdPath = avdPath.getParent().resolve(newName + ".avd");
                Path newIniFile = avdPath.getParent().resolve(newName + ".ini");

                // Rename .avd directory
                if (Files.exists(avdPath)) {
                    Files.move(avdPath, newAvdPath);
                }

                // Rename .ini file
                if (Files.exists(iniFile)) {
                    Files.move(iniFile, newIniFile);
                    // Update path in ini file
                    String iniContent = Files.readString(newIniFile);
                    iniContent = iniContent.replace(oldName + ".avd", newName + ".avd");
                    Files.writeString(newIniFile, iniContent);
                }

                view.log("AVD renamed successfully");
                refreshAvdList();
            } catch (Exception e) {
                logger.error("Failed to rename AVD", e);
                view.log("ERROR: " + e.getMessage());
                JOptionPane.showMessageDialog(view,
                    "Failed to rename AVD: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    private void refreshAvdList() {
        if (emulatorService == null) {
            return;
        }

        new Thread(() -> {
            try {
                var avds = emulatorService.listAvds();
                SwingUtilities.invokeLater(() -> {
                    view.getAvdGridPanel().updateAvdList(avds);
                });
                view.log("Refreshed AVD list (" + avds.size() + " devices)");
            } catch (Exception e) {
                logger.error("Failed to list AVDs", e);
                view.log("ERROR: " + e.getMessage());
            }
        }).start();
    }

    private void handleWindowClosing() {
        if (emulatorService != null && !emulatorService.getRunningEmulators().isEmpty()) {
            int result = JOptionPane.showConfirmDialog(view,
                "There are running emulators. Stop them and exit?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                emulatorService.stopAllEmulators();
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    /**
     * Shows the main view.
     */
    public void show() {
        view.setVisible(true);
    }

    /**
     * Gets the main view.
     *
     * @return Main view instance
     */
    public MainView getView() {
        return view;
    }
}
