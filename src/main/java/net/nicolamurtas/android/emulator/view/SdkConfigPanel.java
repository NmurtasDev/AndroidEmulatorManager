package net.nicolamurtas.android.emulator.view;

import net.nicolamurtas.android.emulator.util.ThemeUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel for SDK configuration with accordion-style toggle.
 * Allows users to browse, download, and verify Android SDK.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public class SdkConfigPanel extends JPanel {
    private final JTextField sdkPathField;
    private final JPanel contentPanel;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private boolean expanded = false;

    // Action handlers (set by controller)
    private Runnable onBrowse;
    private Runnable onDownload;
    private Runnable onVerify;

    public SdkConfigPanel(boolean sdkConfigured) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker(), 1));

        // Collapsed if SDK is configured, expanded if not
        this.expanded = !sdkConfigured;

        // Header panel with toggle button
        JPanel headerPanel = createHeaderPanel(sdkConfigured);
        add(headerPanel, BorderLayout.NORTH);

        // SDK content panel
        sdkPathField = new JTextField(40);
        contentPanel = createContentPanel();
        contentPanel.setVisible(expanded);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel(boolean sdkConfigured) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeUtils.getHeaderBackgroundColor());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        headerLabel = new JLabel(expanded ? "▼ SDK Configuration" : "▶ SDK Configuration");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 13f));
        headerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerLabel.setForeground(UIManager.getColor("Label.foreground"));

        statusLabel = new JLabel(sdkConfigured ? "✓ Configured" : "⚠ Not Configured");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        statusLabel.setForeground(sdkConfigured ? ThemeUtils.Colors.SUCCESS : ThemeUtils.Colors.WARNING);

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);

        // Toggle functionality
        headerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggle();
            }
        });

        headerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggle();
            }
        });

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with path field
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("SDK Path:"));
        topPanel.add(sdkPathField);

        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> {
            if (onBrowse != null) onBrowse.run();
        });
        topPanel.add(browseButton);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton downloadButton = new JButton("Download SDK");
        downloadButton.setBackground(ThemeUtils.Colors.SUCCESS);
        downloadButton.setForeground(Color.WHITE);
        downloadButton.addActionListener(e -> {
            if (onDownload != null) onDownload.run();
        });
        buttonPanel.add(downloadButton);

        JButton verifyButton = new JButton("Verify SDK");
        verifyButton.addActionListener(e -> {
            if (onVerify != null) onVerify.run();
        });
        buttonPanel.add(verifyButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void toggle() {
        expanded = !expanded;

        if (expanded) {
            headerLabel.setText("▼ SDK Configuration");
            contentPanel.setVisible(true);
        } else {
            headerLabel.setText("▶ SDK Configuration");
            contentPanel.setVisible(false);
        }

        revalidate();
        repaint();
    }

    /**
     * Sets the SDK path in the text field.
     *
     * @param path SDK path to display
     */
    public void setSdkPath(String path) {
        sdkPathField.setText(path);
    }

    /**
     * Gets the current SDK path from the text field.
     *
     * @return Current SDK path
     */
    public String getSdkPath() {
        return sdkPathField.getText();
    }

    /**
     * Updates the configuration status indicator.
     *
     * @param configured Whether SDK is configured
     */
    public void setConfigured(boolean configured) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(configured ? "✓ Configured" : "⚠ Not Configured");
            statusLabel.setForeground(configured ? ThemeUtils.Colors.SUCCESS : ThemeUtils.Colors.WARNING);
        });
    }

    // Action handler setters
    public void setOnBrowse(Runnable action) {
        this.onBrowse = action;
    }

    public void setOnDownload(Runnable action) {
        this.onDownload = action;
    }

    public void setOnVerify(Runnable action) {
        this.onVerify = action;
    }
}
