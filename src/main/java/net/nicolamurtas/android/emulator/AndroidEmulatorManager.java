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
import java.util.*;
import java.util.List;

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
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JPanel logPanel;
    private JScrollPane logScrollPane;
    private boolean logExpanded = false;

    // Device cards UI
    private JPanel devicesGridPanel;
    private List<EmulatorService.AvdInfo> allAvds = new ArrayList<>();
    private int currentPage = 0;
    private static final int CARDS_PER_PAGE = 10;
    private JLabel pageLabel;
    private JButton prevPageButton;
    private JButton nextPageButton;

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

        // Center panel with AVD list and accordion log
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(createAvdPanel(), BorderLayout.CENTER);
        centerPanel.add(createLogAccordion(), BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

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

        // Devices grid panel (5 columns x 2 rows = 10 cards)
        devicesGridPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        devicesGridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(devicesGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with pagination and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        // Pagination controls
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevPageButton = new JButton("◄ Previous");
        prevPageButton.addActionListener(e -> changePage(-1));
        prevPageButton.setEnabled(false);

        pageLabel = new JLabel("Page 1", SwingConstants.CENTER);
        pageLabel.setPreferredSize(new Dimension(100, 25));

        nextPageButton = new JButton("Next ►");
        nextPageButton.addActionListener(e -> changePage(1));
        nextPageButton.setEnabled(false);

        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextPageButton);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton createButton = new JButton("Create New AVD");
        createButton.setBackground(new Color(76, 175, 80));
        createButton.setForeground(Color.WHITE);
        createButton.addActionListener(e -> createAvdDialog());
        buttonPanel.add(createButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAvdList());
        buttonPanel.add(refreshButton);

        bottomPanel.add(paginationPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogAccordion() {
        logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker(), 1));

        // Header panel with toggle button - use system colors
        JPanel headerPanel = new JPanel(new BorderLayout());
        // Use slightly darker/lighter version of panel background for contrast
        Color panelBg = UIManager.getColor("Panel.background");
        Color headerBg = panelBg != null ?
            (isDarkTheme() ? panelBg.brighter() : panelBg.darker()) :
            new Color(240, 240, 240);
        headerPanel.setBackground(headerBg);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel logLabel = new JLabel("▶ Log");
        logLabel.setFont(logLabel.getFont().deriveFont(Font.BOLD, 13f));
        logLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Ensure label uses system text color
        logLabel.setForeground(UIManager.getColor("Label.foreground"));

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(clearButton.getFont().deriveFont(10f));
        clearButton.setMargin(new Insets(2, 8, 2, 8));
        clearButton.addActionListener(e -> logArea.setText(""));
        clearButton.setVisible(false); // Initially hidden

        headerPanel.add(logLabel, BorderLayout.WEST);
        headerPanel.add(clearButton, BorderLayout.EAST);

        // Log content panel (initially hidden) - use system colors
        logArea = new JTextArea(10, 0);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        // Set text area colors to match system theme
        logArea.setBackground(UIManager.getColor("TextArea.background"));
        logArea.setForeground(UIManager.getColor("TextArea.foreground"));
        logArea.setCaretColor(UIManager.getColor("TextArea.caretForeground"));

        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(0, 0));
        logScrollPane.setVisible(false);

        // Toggle functionality
        headerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggleLog(logLabel, clearButton);
            }
        });

        logLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggleLog(logLabel, clearButton);
            }
        });

        logPanel.add(headerPanel, BorderLayout.NORTH);
        logPanel.add(logScrollPane, BorderLayout.CENTER);

        return logPanel;
    }

    private void toggleLog(JLabel label, JButton clearButton) {
        logExpanded = !logExpanded;

        if (logExpanded) {
            label.setText("▼ Log");
            logScrollPane.setPreferredSize(new Dimension(0, 200));
            logScrollPane.setVisible(true);
            clearButton.setVisible(true);
        } else {
            label.setText("▶ Log");
            logScrollPane.setPreferredSize(new Dimension(0, 0));
            logScrollPane.setVisible(false);
            clearButton.setVisible(false);
        }

        logPanel.revalidate();
        logPanel.repaint();
    }

    /**
     * Creates a device card panel for an AVD.
     */
    private JPanel createDeviceCard(EmulatorService.AvdInfo avd) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Panel.border"), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(180, 200));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Device name at top
        JLabel nameLabel = new JLabel(avd.name(), SwingConstants.CENTER);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
        card.add(nameLabel, BorderLayout.NORTH);

        // Details panel (initially hidden, toggled on click)
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        detailsPanel.setVisible(false);

        // Extract API level and convert to Android version name
        String apiLevel = extractApiLevel(avd.target());
        String androidVersion = getAndroidVersionName(apiLevel);
        JLabel versionLabel = new JLabel("📱 " + androidVersion);
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.BOLD));
        detailsPanel.add(versionLabel);

        // Show device type from path (e.g., pixel_7)
        String deviceType = extractDeviceType(avd.path());
        if (deviceType != null && !deviceType.isEmpty()) {
            detailsPanel.add(new JLabel("📐 Device: " + deviceType));
        }

        // Check if running
        boolean isRunning = emulatorService != null && emulatorService.isEmulatorRunning(avd.name());
        JLabel statusLabel = new JLabel(isRunning ? "🟢 Running" : "⚪ Stopped");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        statusLabel.setForeground(isRunning ? new Color(76, 175, 80) : Color.GRAY);
        detailsPanel.add(statusLabel);

        card.add(detailsPanel, BorderLayout.CENTER);

        // Action buttons panel
        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 5, 5));

        JButton startBtn = new JButton("▶");
        startBtn.setToolTipText("Start");
        startBtn.setBackground(new Color(76, 175, 80));
        startBtn.setForeground(Color.WHITE);
        startBtn.addActionListener(e -> startEmulatorByName(avd.name()));

        JButton stopBtn = new JButton("■");
        stopBtn.setToolTipText("Stop");
        stopBtn.setBackground(new Color(244, 67, 54));
        stopBtn.setForeground(Color.WHITE);
        stopBtn.addActionListener(e -> stopEmulatorByName(avd.name()));

        JButton renameBtn = new JButton("✎");
        renameBtn.setToolTipText("Rename");
        renameBtn.addActionListener(e -> renameAvd(avd.name()));

        JButton deleteBtn = new JButton("🗑");
        deleteBtn.setToolTipText("Delete");
        deleteBtn.setBackground(new Color(244, 67, 54));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteAvdByName(avd.name()));

        actionsPanel.add(startBtn);
        actionsPanel.add(stopBtn);
        actionsPanel.add(renameBtn);
        actionsPanel.add(deleteBtn);

        card.add(actionsPanel, BorderLayout.SOUTH);

        // Toggle details on card click
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                detailsPanel.setVisible(!detailsPanel.isVisible());
                card.revalidate();
                card.repaint();
            }
        });

        return card;
    }

    /**
     * Extracts API level from target string.
     */
    private String extractApiLevel(String target) {
        if (target == null) return "Unknown";
        // Target format: "Android X.Y (API level Z)" or similar
        if (target.contains("API level")) {
            int start = target.indexOf("API level") + 10;
            int end = target.indexOf(")", start);
            if (end > start) {
                return target.substring(start, end).trim();
            }
        }
        // Try to extract just the number
        String[] parts = target.split("\\s+");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                return part;
            }
        }
        return "Unknown";
    }

    /**
     * Converts API level to Android version name.
     */
    private String getAndroidVersionName(String apiLevel) {
        if (apiLevel == null || apiLevel.equals("Unknown")) {
            return "Android (Unknown)";
        }

        return switch (apiLevel) {
            case "36" -> "Android 16";
            case "35" -> "Android 15";
            case "34" -> "Android 14";
            case "33" -> "Android 13";
            case "32" -> "Android 12L";
            case "31" -> "Android 12";
            case "30" -> "Android 11";
            case "29" -> "Android 10";
            case "28" -> "Android 9";
            case "27" -> "Android 8.1";
            case "26" -> "Android 8.0";
            case "25" -> "Android 7.1";
            case "24" -> "Android 7.0";
            case "23" -> "Android 6.0";
            case "22" -> "Android 5.1";
            case "21" -> "Android 5.0";
            default -> "Android API " + apiLevel;
        };
    }

    /**
     * Extracts device type from AVD path.
     * Example: /home/user/.android/avd/MyDevice.avd -> looks in config.ini for hw.device.name
     */
    private String extractDeviceType(String avdPath) {
        if (avdPath == null) return null;

        try {
            Path configIni = Path.of(avdPath).resolve("config.ini");
            if (Files.exists(configIni)) {
                String content = Files.readString(configIni);
                // Look for hw.device.name=pixel_7
                for (String line : content.split("\n")) {
                    if (line.startsWith("hw.device.name=")) {
                        String deviceName = line.substring(15).trim();
                        // Format device name: pixel_7 -> Pixel 7
                        return formatDeviceName(deviceName);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract device type from path: {}", avdPath);
        }

        return null;
    }

    /**
     * Formats device name for display.
     * Example: pixel_7 -> Pixel 7, pixel -> Pixel
     */
    private String formatDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isEmpty()) {
            return deviceName;
        }

        // Replace underscores with spaces and capitalize words
        String[] parts = deviceName.replace("_", " ").split(" ");
        StringBuilder formatted = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                formatted.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    formatted.append(part.substring(1));
                }
                formatted.append(" ");
            }
        }

        return formatted.toString().trim();
    }

    /**
     * Changes the current page of devices.
     */
    private void changePage(int delta) {
        int totalPages = (int) Math.ceil((double) allAvds.size() / CARDS_PER_PAGE);
        currentPage = Math.max(0, Math.min(currentPage + delta, totalPages - 1));
        updateDeviceCards();
    }

    /**
     * Updates the device cards display for the current page.
     */
    private void updateDeviceCards() {
        SwingUtilities.invokeLater(() -> {
            devicesGridPanel.removeAll();

            int start = currentPage * CARDS_PER_PAGE;
            int end = Math.min(start + CARDS_PER_PAGE, allAvds.size());

            for (int i = start; i < end; i++) {
                devicesGridPanel.add(createDeviceCard(allAvds.get(i)));
            }

            // Fill empty slots with placeholder panels
            int cardsShown = end - start;
            for (int i = cardsShown; i < CARDS_PER_PAGE; i++) {
                JPanel placeholder = new JPanel();
                placeholder.setPreferredSize(new Dimension(180, 200));
                placeholder.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
                placeholder.setBackground(UIManager.getColor("Panel.background"));
                devicesGridPanel.add(placeholder);
            }

            // Update pagination controls
            int totalPages = Math.max(1, (int) Math.ceil((double) allAvds.size() / CARDS_PER_PAGE));
            pageLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);
            prevPageButton.setEnabled(currentPage > 0);
            nextPageButton.setEnabled(currentPage < totalPages - 1);

            devicesGridPanel.revalidate();
            devicesGridPanel.repaint();
        });
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

        // Show component selection dialog
        List<String> selectedComponents = showSdkComponentSelectionDialog();
        if (selectedComponents == null || selectedComponents.isEmpty()) {
            log("SDK download cancelled by user");
            return;
        }

        new Thread(() -> {
            try {
                showProgress(true);
                log("=== Starting SDK Download ===");
                log("Target path: " + sdkPath);
                log("Selected components: " + selectedComponents.size());

                sdkDownloadService.downloadAndInstallSdk(sdkPath, selectedComponents, this::updateProgress);

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

    private List<String> showSdkComponentSelectionDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Select SDK Components to Install:");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(titleLabel, gbc);

        // Essential components (always selected, disabled)
        gbc.gridy++; gbc.gridwidth = 1;
        JLabel essentialLabel = new JLabel("Essential Components (required):");
        essentialLabel.setFont(essentialLabel.getFont().deriveFont(Font.BOLD));
        panel.add(essentialLabel, gbc);

        List<JCheckBox> essentialCheckboxes = new ArrayList<>();
        String[] essentialComponents = {"platform-tools", "emulator", "build-tools;35.0.0"};
        for (String component : essentialComponents) {
            gbc.gridy++;
            JCheckBox cb = new JCheckBox(component, true);
            cb.setEnabled(false);
            essentialCheckboxes.add(cb);
            panel.add(cb, gbc);
        }

        // API levels
        gbc.gridy++;
        JLabel apiLabel = new JLabel("Android API Levels:");
        apiLabel.setFont(apiLabel.getFont().deriveFont(Font.BOLD));
        panel.add(apiLabel, gbc);

        Map<String, JCheckBox> apiCheckboxes = new LinkedHashMap<>();
        for (int api = 36; api >= 30; api--) {
            gbc.gridy++;
            JCheckBox platformCb = new JCheckBox("Android " + api + " (Platform + System Image)", api >= 34);
            apiCheckboxes.put(String.valueOf(api), platformCb);
            panel.add(platformCb, gbc);
        }

        // Select/Deselect all buttons
        gbc.gridy++;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectAllBtn = new JButton("Select All");
        JButton deselectAllBtn = new JButton("Deselect All");

        selectAllBtn.addActionListener(e ->
            apiCheckboxes.values().forEach(cb -> cb.setSelected(true)));
        deselectAllBtn.addActionListener(e ->
            apiCheckboxes.values().forEach(cb -> cb.setSelected(false)));

        buttonPanel.add(selectAllBtn);
        buttonPanel.add(deselectAllBtn);
        panel.add(buttonPanel, gbc);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(panel),
            "SDK Component Selection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        // Build selected components list
        List<String> selectedComponents = new ArrayList<>();

        // Add essential components
        selectedComponents.add("platform-tools");
        selectedComponents.add("emulator");
        selectedComponents.add("build-tools;35.0.0");

        // Add selected APIs
        for (Map.Entry<String, JCheckBox> entry : apiCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                String api = entry.getKey();
                selectedComponents.add("platforms;android-" + api);
                selectedComponents.add("system-images;android-" + api + ";google_apis;x86_64");
            }
        }

        return selectedComponents;
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

                    // Show progress bar for potential API installation
                    showProgress(true);

                    boolean success = emulatorService.createAvd(
                        nameField.getText(),
                        selectedApi,
                        (String) deviceCombo.getSelectedItem(),
                        this::updateProgress
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
                } finally {
                    showProgress(false);
                }
            }).start();
        }
    }

    private void startEmulatorByName(String avdName) {
        if (emulatorService == null) {
            JOptionPane.showMessageDialog(this,
                "EmulatorService not initialized",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                log("Starting emulator: " + avdName);
                emulatorService.startEmulator(avdName);
                log("Emulator started: " + avdName);
                // Refresh cards to update status
                refreshAvdList();
            } catch (Exception e) {
                logger.error("Failed to start emulator", e);
                log("ERROR: " + e.getMessage());
            }
        }).start();
    }

    private void stopEmulatorByName(String avdName) {
        if (emulatorService == null) {
            JOptionPane.showMessageDialog(this,
                "EmulatorService not initialized",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        emulatorService.stopEmulator(avdName);
        log("Emulator stopped: " + avdName);
        // Refresh cards to update status
        refreshAvdList();
    }

    private void deleteAvdByName(String avdName) {
        int result = JOptionPane.showConfirmDialog(this,
            "Delete AVD '" + avdName + "'?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                try {
                    log("Deleting AVD: " + avdName);
                    boolean success = emulatorService.deleteAvd(avdName);
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

    private void renameAvd(String oldName) {
        String newName = JOptionPane.showInputDialog(this,
            "Enter new name for AVD '" + oldName + "':",
            "Rename AVD",
            JOptionPane.PLAIN_MESSAGE);

        if (newName != null && !newName.trim().isEmpty() && !newName.equals(oldName)) {
            new Thread(() -> {
                try {
                    log("Renaming AVD: " + oldName + " -> " + newName);

                    // Get AVD path
                    EmulatorService.AvdInfo avdInfo = allAvds.stream()
                        .filter(avd -> avd.name().equals(oldName))
                        .findFirst()
                        .orElse(null);

                    if (avdInfo == null || avdInfo.path() == null) {
                        log("ERROR: Could not find AVD path");
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

                    log("AVD renamed successfully");
                    refreshAvdList();
                } catch (Exception e) {
                    logger.error("Failed to rename AVD", e);
                    log("ERROR: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                        "Failed to rename AVD: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
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
                    allAvds = new ArrayList<>(avds);
                    currentPage = 0;
                    updateDeviceCards();
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

    /**
     * Detects if the current system theme is dark.
     * Uses panel background brightness to determine theme.
     */
    private boolean isDarkTheme() {
        Color bg = UIManager.getColor("Panel.background");
        if (bg == null) {
            return false;
        }
        // Calculate perceived brightness using standard formula
        int brightness = (int) Math.sqrt(
            bg.getRed() * bg.getRed() * 0.241 +
            bg.getGreen() * bg.getGreen() * 0.691 +
            bg.getBlue() * bg.getBlue() * 0.068
        );
        return brightness < 130; // Dark theme if brightness < 130
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
