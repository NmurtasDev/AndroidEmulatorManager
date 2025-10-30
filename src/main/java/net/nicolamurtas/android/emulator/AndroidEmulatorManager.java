package net.nicolamurtas.android.emulator;

import net.nicolamurtas.android.emulator.service.ConfigService;
import net.nicolamurtas.android.emulator.service.EmulatorService;
import net.nicolamurtas.android.emulator.service.SdkDownloadService;
import net.nicolamurtas.android.emulator.util.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main application class for Android Emulator Manager.
 *
 * A modern Java 21 application for managing Android SDK and emulators.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public class AndroidEmulatorManager extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(AndroidEmulatorManager.class);

    private final ConfigService configService;
    private final SdkDownloadService sdkDownloadService;
    private EmulatorService emulatorService;

    // UI Components
    private JTextField sdkPathField;
    private DefaultListModel<String> avdListModel;
    private JList<String> avdList;
    private JTextArea logArea;
    private JProgressBar progressBar;

    public AndroidEmulatorManager() {
        this.configService = new ConfigService();
        this.sdkDownloadService = new SdkDownloadService();

        Path sdkPath = configService.getSdkPath();
        if (Files.exists(sdkPath)) {
            this.emulatorService = new EmulatorService(sdkPath);
        }

        initializeUI();
        loadConfiguration();
        refreshAvdList();

        logger.info("Android Emulator Manager started");
    }

    private void initializeUI() {
        setTitle("Android Emulator Manager v3.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Failed to set system look and feel", e);
        }

        // Main layout
        setLayout(new BorderLayout(10, 10));

        // SDK Configuration Panel
        add(createSdkPanel(), BorderLayout.NORTH);

        // Center panel with AVD list and log
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(createAvdPanel());
        splitPane.setBottomComponent(createLogPanel());
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.SOUTH);

        // Window closing handler
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onClosing();
            }
        });
    }

    private JPanel createSdkPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("SDK Configuration"));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("SDK Path:"));

        sdkPathField = new JTextField(40);
        topPanel.add(sdkPathField);

        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseSdkPath());
        topPanel.add(browseButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton downloadButton = new JButton("Download SDK");
        downloadButton.setBackground(new Color(76, 175, 80));
        downloadButton.setForeground(Color.WHITE);
        downloadButton.addActionListener(e -> downloadSdk());
        buttonPanel.add(downloadButton);

        JButton verifyButton = new JButton("Verify SDK");
        verifyButton.addActionListener(e -> verifySdk());
        buttonPanel.add(verifyButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAvdPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Android Virtual Devices"));

        avdListModel = new DefaultListModel<>();
        avdList = new JList<>(avdListModel);
        avdList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(avdList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton createButton = new JButton("Create AVD");
        createButton.addActionListener(e -> createAvdDialog());
        buttonPanel.add(createButton);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> startEmulator());
        buttonPanel.add(startButton);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopEmulator());
        buttonPanel.add(stopButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteAvd());
        buttonPanel.add(deleteButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAvdList());
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log"));

        logArea = new JTextArea(15, 0);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadConfiguration() {
        Path sdkPath = configService.getSdkPath();
        sdkPathField.setText(sdkPath.toString());
    }

    private void browseSdkPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Android SDK Directory");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            sdkPathField.setText(path.toString());
            configService.setSdkPath(path);
            configService.saveConfig();
            emulatorService = new EmulatorService(path);
            log("SDK path set to: " + path);
        }
    }

    private void downloadSdk() {
        String pathText = sdkPathField.getText().trim();
        if (pathText.isEmpty()) {
            pathText = PlatformUtils.getDefaultSdkPath().toString();
            sdkPathField.setText(pathText);
        }

        Path sdkPath = Path.of(pathText);

        new Thread(() -> {
            try {
                showProgress(true);
                log("=== Starting SDK Download ===");
                log("Target path: " + sdkPath);

                sdkDownloadService.downloadAndInstallSdk(sdkPath, this::updateProgress);

                configService.setSdkPath(sdkPath);
                configService.saveConfig();
                emulatorService = new EmulatorService(sdkPath);

                log("=== SDK Installation Completed Successfully ===");
                JOptionPane.showMessageDialog(this,
                    "SDK downloaded and installed successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                logger.error("SDK download failed", e);
                log("ERROR: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                    "SDK download failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                showProgress(false);
            }
        }).start();
    }

    private void verifySdk() {
        if (configService.isSdkConfigured()) {
            JOptionPane.showMessageDialog(this,
                "SDK is properly configured!",
                "SDK Verification", JOptionPane.INFORMATION_MESSAGE);
            log("SDK verification: OK");
        } else {
            JOptionPane.showMessageDialog(this,
                "SDK is not properly configured. Please download SDK first.",
                "SDK Verification", JOptionPane.WARNING_MESSAGE);
            log("SDK verification: FAILED");
        }
    }

    private void createAvdDialog() {
        if (emulatorService == null) {
            JOptionPane.showMessageDialog(this,
                "Please configure SDK first",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Enhanced AVD creation dialog
        JTextField nameField = new JTextField("MyDevice");

        // Main API levels (30-36)
        String[] apiLevels = {"36", "35", "34", "33", "32", "31", "30"};
        JComboBox<String> apiCombo = new JComboBox<>(apiLevels);
        apiCombo.setSelectedItem("35");

        // Legacy API levels (< 30)
        JCheckBox legacyCheckBox = new JCheckBox("Show Legacy APIs (< 30)");
        String[] legacyApiLevels = {"29", "28", "27", "26", "25", "24", "23", "22", "21"};
        JComboBox<String> legacyApiCombo = new JComboBox<>(legacyApiLevels);
        legacyApiCombo.setEnabled(false);
        legacyApiCombo.setVisible(false);

        legacyCheckBox.addActionListener(e -> {
            boolean showLegacy = legacyCheckBox.isSelected();
            apiCombo.setEnabled(!showLegacy);
            legacyApiCombo.setEnabled(showLegacy);
            legacyApiCombo.setVisible(showLegacy);
        });

        String[] devices = {"pixel", "pixel_2", "pixel_3", "pixel_4", "pixel_5",
                           "pixel_6", "pixel_7", "pixel_8"};
        JComboBox<String> deviceCombo = new JComboBox<>(devices);
        deviceCombo.setSelectedItem("pixel_7");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(nameField, gbc);

        // Row 1: API Level
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("API Level:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(apiCombo, gbc);

        // Row 2: Legacy checkbox
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(legacyCheckBox, gbc);

        // Row 3: Legacy API combo (initially hidden)
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(legacyApiCombo, gbc);

        // Row 4: Device
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Device:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(deviceCombo, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Create New AVD", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            new Thread(() -> {
                try {
                    // Determine which API level to use (standard or legacy)
                    String selectedApi = legacyCheckBox.isSelected() ?
                        (String) legacyApiCombo.getSelectedItem() :
                        (String) apiCombo.getSelectedItem();

                    log("Creating AVD: " + nameField.getText() + " (API " + selectedApi + ")");
                    boolean success = emulatorService.createAvd(
                        nameField.getText(),
                        selectedApi,
                        (String) deviceCombo.getSelectedItem()
                    );

                    if (success) {
                        log("AVD created successfully");
                        refreshAvdList();
                    } else {
                        log("ERROR: Failed to create AVD");
                    }
                } catch (Exception e) {
                    logger.error("Failed to create AVD", e);
                    log("ERROR: " + e.getMessage());
                }
            }).start();
        }
    }

    private void startEmulator() {
        String selected = avdList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Please select an AVD",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                log("Starting emulator: " + selected);
                emulatorService.startEmulator(selected);
                log("Emulator started: " + selected);
            } catch (Exception e) {
                logger.error("Failed to start emulator", e);
                log("ERROR: " + e.getMessage());
            }
        }).start();
    }

    private void stopEmulator() {
        String selected = avdList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Please select an AVD",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        emulatorService.stopEmulator(selected);
        log("Emulator stopped: " + selected);
    }

    private void deleteAvd() {
        String selected = avdList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Please select an AVD",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
            "Delete AVD '" + selected + "'?",
            "Confirm", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                try {
                    log("Deleting AVD: " + selected);
                    boolean success = emulatorService.deleteAvd(selected);
                    if (success) {
                        log("AVD deleted successfully");
                        refreshAvdList();
                    } else {
                        log("ERROR: Failed to delete AVD");
                    }
                } catch (Exception e) {
                    logger.error("Failed to delete AVD", e);
                    log("ERROR: " + e.getMessage());
                }
            }).start();
        }
    }

    private void refreshAvdList() {
        if (emulatorService == null) {
            return;
        }

        new Thread(() -> {
            try {
                var avds = emulatorService.listAvds();
                SwingUtilities.invokeLater(() -> {
                    avdListModel.clear();
                    avds.forEach(avd -> avdListModel.addElement(avd.name()));
                });
                log("Refreshed AVD list (" + avds.size() + " devices)");
            } catch (Exception e) {
                logger.error("Failed to list AVDs", e);
                log("ERROR: " + e.getMessage());
            }
        }).start();
    }

    private void updateProgress(int value, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(message);
            log(message);
        });
    }

    private void showProgress(boolean show) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(show);
            if (show) {
                progressBar.setValue(0);
            }
        });
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void onClosing() {
        if (emulatorService != null && !emulatorService.getRunningEmulators().isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this,
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

    public static void main(String[] args) {
        logger.info("Starting Android Emulator Manager v3.0");
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("Operating System: {}", PlatformUtils.getOperatingSystem());

        SwingUtilities.invokeLater(() -> {
            AndroidEmulatorManager app = new AndroidEmulatorManager();
            app.setVisible(true);
        });
    }
}
