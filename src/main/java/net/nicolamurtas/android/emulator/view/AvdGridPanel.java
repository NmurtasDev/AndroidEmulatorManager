package net.nicolamurtas.android.emulator.view;

import net.nicolamurtas.android.emulator.service.EmulatorService;
import net.nicolamurtas.android.emulator.util.AndroidVersionMapper;
import net.nicolamurtas.android.emulator.util.DeviceNameFormatter;
import net.nicolamurtas.android.emulator.util.ThemeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel displaying Android Virtual Devices in a paginated grid layout.
 * Shows device cards with actions (start, stop, rename, delete).
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public class AvdGridPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(AvdGridPanel.class);
    private static final int CARDS_PER_PAGE = 10;

    private final JPanel devicesGridPanel;
    private JLabel pageLabel;
    private JButton prevPageButton;
    private JButton nextPageButton;

    private List<EmulatorService.AvdInfo> allAvds = new ArrayList<>();
    private int currentPage = 0;
    private EmulatorService emulatorService;

    // Action handlers (set by controller)
    private Runnable onCreateAvd;
    private Runnable onRefresh;
    private Consumer<String> onStartEmulator;
    private Consumer<String> onStopEmulator;
    private Consumer<String> onRenameAvd;
    private Consumer<String> onDeleteAvd;

    public AvdGridPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Android Virtual Devices"));

        // Devices grid panel (5 columns x 2 rows = 10 cards)
        devicesGridPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        devicesGridPanel.setBackground(UIManager.getColor("Panel.background"));
        devicesGridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(devicesGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with pagination and buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(UIManager.getColor("Panel.background"));

        // Pagination controls
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setBackground(UIManager.getColor("Panel.background"));

        prevPageButton = new JButton("◄ Previous");
        prevPageButton.addActionListener(e -> changePage(-1));
        prevPageButton.setEnabled(false);

        pageLabel = new JLabel("Page 1", SwingConstants.CENTER);
        pageLabel.setForeground(UIManager.getColor("Label.foreground"));
        pageLabel.setPreferredSize(new Dimension(100, 25));

        nextPageButton = new JButton("Next ►");
        nextPageButton.addActionListener(e -> changePage(1));
        nextPageButton.setEnabled(false);

        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextPageButton);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton createButton = new JButton("Create New AVD");
        createButton.setBackground(ThemeUtils.Colors.SUCCESS);
        createButton.setForeground(Color.WHITE);
        createButton.addActionListener(e -> {
            if (onCreateAvd != null) onCreateAvd.run();
        });
        buttonPanel.add(createButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            if (onRefresh != null) onRefresh.run();
        });
        buttonPanel.add(refreshButton);

        bottomPanel.add(paginationPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        return bottomPanel;
    }

    /**
     * Creates a device card panel for an AVD.
     */
    private JPanel createDeviceCard(EmulatorService.AvdInfo avd) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Panel.border"), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(180, 200));

        // Top panel with name and info
        JPanel topPanel = createCardTopPanel(avd);
        card.add(topPanel, BorderLayout.NORTH);

        // Action buttons panel
        JPanel actionsPanel = createCardActionsPanel(avd);
        card.add(actionsPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createCardTopPanel(EmulatorService.AvdInfo avd) {
        JPanel topPanel = new JPanel(new BorderLayout(3, 3));
        topPanel.setBackground(UIManager.getColor("Panel.background"));

        // Device name
        JLabel nameLabel = new JLabel(avd.name(), SwingConstants.CENTER);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
        nameLabel.setForeground(UIManager.getColor("Label.foreground"));
        topPanel.add(nameLabel, BorderLayout.NORTH);

        // Info panel with version and device type
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        infoPanel.setBackground(UIManager.getColor("Panel.background"));

        // Extract API level and show Android version
        String apiLevel = extractApiLevelFromPath(avd.path());
        String androidVersion = AndroidVersionMapper.getAndroidVersionName(apiLevel);
        JLabel versionLabel = new JLabel(androidVersion, SwingConstants.CENTER);
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.PLAIN, 11f));
        versionLabel.setForeground(UIManager.getColor("Label.foreground"));
        infoPanel.add(versionLabel);

        // Show device type
        String deviceType = extractDeviceType(avd.path());
        if (deviceType != null && !deviceType.isEmpty()) {
            JLabel deviceLabel = new JLabel(deviceType, SwingConstants.CENTER);
            deviceLabel.setFont(deviceLabel.getFont().deriveFont(Font.PLAIN, 10f));
            // Use a dimmed version of the foreground color for secondary text
            Color labelColor = UIManager.getColor("Label.foreground");
            if (labelColor != null) {
                deviceLabel.setForeground(new Color(
                    labelColor.getRed(),
                    labelColor.getGreen(),
                    labelColor.getBlue(),
                    180 // Alpha for dimming
                ));
            }
            infoPanel.add(deviceLabel);
        }

        // Check if running
        boolean isRunning = emulatorService != null && emulatorService.isEmulatorRunning(avd.name());
        JLabel statusLabel = new JLabel(isRunning ? "● Running" : "○ Stopped", SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 10f));
        if (isRunning) {
            statusLabel.setForeground(ThemeUtils.Colors.SUCCESS);
        } else {
            // Use dimmed foreground color for stopped status
            Color labelColor = UIManager.getColor("Label.foreground");
            if (labelColor != null) {
                statusLabel.setForeground(new Color(
                    labelColor.getRed(),
                    labelColor.getGreen(),
                    labelColor.getBlue(),
                    180 // Alpha for dimming
                ));
            }
        }
        infoPanel.add(statusLabel);

        topPanel.add(infoPanel, BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel createCardActionsPanel(EmulatorService.AvdInfo avd) {
        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        actionsPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton startBtn = new JButton("▶");
        startBtn.setToolTipText("Start");
        startBtn.setBackground(ThemeUtils.Colors.SUCCESS);
        startBtn.setForeground(Color.WHITE);
        startBtn.addActionListener(e -> {
            if (onStartEmulator != null) onStartEmulator.accept(avd.name());
        });

        JButton stopBtn = new JButton("■");
        stopBtn.setToolTipText("Stop");
        stopBtn.setBackground(ThemeUtils.Colors.ERROR);
        stopBtn.setForeground(Color.WHITE);
        stopBtn.addActionListener(e -> {
            if (onStopEmulator != null) onStopEmulator.accept(avd.name());
        });

        JButton renameBtn = new JButton("✎");
        renameBtn.setToolTipText("Rename");
        renameBtn.addActionListener(e -> {
            if (onRenameAvd != null) onRenameAvd.accept(avd.name());
        });

        JButton deleteBtn = new JButton("🗑");
        deleteBtn.setToolTipText("Delete");
        deleteBtn.setBackground(ThemeUtils.Colors.ERROR);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> {
            if (onDeleteAvd != null) onDeleteAvd.accept(avd.name());
        });

        actionsPanel.add(startBtn);
        actionsPanel.add(stopBtn);
        actionsPanel.add(renameBtn);
        actionsPanel.add(deleteBtn);

        return actionsPanel;
    }

    /**
     * Extracts API level from AVD config.ini file.
     */
    private String extractApiLevelFromPath(String avdPath) {
        if (avdPath == null) return "Unknown";

        try {
            Path configIni = Path.of(avdPath).resolve("config.ini");
            if (Files.exists(configIni)) {
                String content = Files.readString(configIni);
                return AndroidVersionMapper.extractApiLevelFromConfig(content);
            }
        } catch (Exception e) {
            logger.debug("Could not extract API level from path: {}", avdPath, e);
        }

        return "Unknown";
    }

    /**
     * Extracts device type from AVD path.
     */
    private String extractDeviceType(String avdPath) {
        if (avdPath == null) return null;

        try {
            Path configIni = Path.of(avdPath).resolve("config.ini");
            if (Files.exists(configIni)) {
                String content = Files.readString(configIni);
                for (String line : content.split("\n")) {
                    line = line.trim();
                    if (line.startsWith("hw.device.name")) {
                        int equalPos = line.indexOf('=');
                        if (equalPos > 0) {
                            String deviceName = line.substring(equalPos + 1).trim();
                            return DeviceNameFormatter.formatDeviceName(deviceName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract device type from path: {}", avdPath, e);
        }

        return null;
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
                Color borderColor = UIManager.getColor("Panel.border");
                if (borderColor == null) {
                    borderColor = UIManager.getColor("Panel.background").darker();
                }
                placeholder.setBorder(BorderFactory.createLineBorder(borderColor, 1, true));
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

    /**
     * Updates the AVD list and refreshes the display.
     *
     * @param avds List of AVDs to display
     */
    public void updateAvdList(List<EmulatorService.AvdInfo> avds) {
        this.allAvds = new ArrayList<>(avds);
        this.currentPage = 0;
        updateDeviceCards();
    }

    /**
     * Sets the emulator service for checking running status.
     *
     * @param emulatorService Emulator service instance
     */
    public void setEmulatorService(EmulatorService emulatorService) {
        this.emulatorService = emulatorService;
    }

    // Action handler setters
    public void setOnCreateAvd(Runnable action) {
        this.onCreateAvd = action;
    }

    public void setOnRefresh(Runnable action) {
        this.onRefresh = action;
    }

    public void setOnStartEmulator(Consumer<String> action) {
        this.onStartEmulator = action;
    }

    public void setOnStopEmulator(Consumer<String> action) {
        this.onStopEmulator = action;
    }

    public void setOnRenameAvd(Consumer<String> action) {
        this.onRenameAvd = action;
    }

    public void setOnDeleteAvd(Consumer<String> action) {
        this.onDeleteAvd = action;
    }

    /**
     * Gets the list of all AVDs currently displayed.
     *
     * @return List of AVDs
     */
    public List<EmulatorService.AvdInfo> getAllAvds() {
        return new ArrayList<>(allAvds);
    }
}
