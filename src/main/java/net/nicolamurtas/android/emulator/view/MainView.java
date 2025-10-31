package net.nicolamurtas.android.emulator.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window.
 * Orchestrates all UI components (SDK panel, AVD grid, log panel, progress bar).
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public class MainView extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);

    private final SdkConfigPanel sdkConfigPanel;
    private final AvdGridPanel avdGridPanel;
    private final LogPanel logPanel;
    private final JProgressBar progressBar;

    public MainView(boolean sdkConfigured) {
        this.sdkConfigPanel = new SdkConfigPanel(sdkConfigured);
        this.avdGridPanel = new AvdGridPanel();
        this.logPanel = new LogPanel();
        this.progressBar = new JProgressBar();

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Android Emulator Manager v3.0");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Let controller handle closing
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
        add(sdkConfigPanel, BorderLayout.NORTH);

        // Center panel with AVD list and accordion log
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(avdGridPanel, BorderLayout.CENTER);
        centerPanel.add(logPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Progress bar
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.SOUTH);
    }

    /**
     * Gets the SDK configuration panel.
     *
     * @return SDK config panel
     */
    public SdkConfigPanel getSdkConfigPanel() {
        return sdkConfigPanel;
    }

    /**
     * Gets the AVD grid panel.
     *
     * @return AVD grid panel
     */
    public AvdGridPanel getAvdGridPanel() {
        return avdGridPanel;
    }

    /**
     * Gets the log panel.
     *
     * @return Log panel
     */
    public LogPanel getLogPanel() {
        return logPanel;
    }

    /**
     * Shows or hides the progress bar.
     *
     * @param show Whether to show the progress bar
     */
    public void showProgress(boolean show) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(show);
            if (show) {
                progressBar.setValue(0);
            }
        });
    }

    /**
     * Updates progress bar value and message.
     *
     * @param value Progress value (0-100)
     * @param message Progress message
     */
    public void updateProgress(int value, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(message);
        });
    }

    /**
     * Appends a message to the log.
     *
     * @param message Log message
     */
    public void log(String message) {
        logPanel.appendLog(message);
    }
}
