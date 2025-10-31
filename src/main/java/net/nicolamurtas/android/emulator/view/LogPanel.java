package net.nicolamurtas.android.emulator.view;

import net.nicolamurtas.android.emulator.util.ThemeUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for displaying application logs with accordion toggle.
 * Provides an expandable/collapsible log view.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public class LogPanel extends JPanel {
    private final JTextArea logArea;
    private final JScrollPane logScrollPane;
    private JLabel logLabel;
    private JButton clearButton;
    private boolean expanded = false;

    public LogPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker(), 1));

        // Header panel with toggle button
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Log content panel (initially hidden)
        logArea = createLogArea();
        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(0, 0));
        logScrollPane.setVisible(false);
        add(logScrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeUtils.getHeaderBackgroundColor());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        logLabel = new JLabel("▶ Log");
        logLabel.setFont(logLabel.getFont().deriveFont(Font.BOLD, 13f));
        logLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logLabel.setForeground(UIManager.getColor("Label.foreground"));

        clearButton = new JButton("Clear");
        clearButton.setFont(clearButton.getFont().deriveFont(10f));
        clearButton.setMargin(new Insets(2, 8, 2, 8));
        clearButton.addActionListener(e -> logArea.setText(""));
        clearButton.setVisible(false);

        headerPanel.add(logLabel, BorderLayout.WEST);
        headerPanel.add(clearButton, BorderLayout.EAST);

        // Toggle functionality
        headerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggleLog();
            }
        });

        logLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggleLog();
            }
        });

        return headerPanel;
    }

    private JTextArea createLogArea() {
        JTextArea area = new JTextArea(10, 0);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        area.setBackground(UIManager.getColor("TextArea.background"));
        area.setForeground(UIManager.getColor("TextArea.foreground"));
        area.setCaretColor(UIManager.getColor("TextArea.caretForeground"));
        return area;
    }

    private void toggleLog() {
        expanded = !expanded;

        if (expanded) {
            logLabel.setText("▼ Log");
            logScrollPane.setPreferredSize(new Dimension(0, 200));
            logScrollPane.setVisible(true);
            clearButton.setVisible(true);
        } else {
            logLabel.setText("▶ Log");
            logScrollPane.setPreferredSize(new Dimension(0, 0));
            logScrollPane.setVisible(false);
            clearButton.setVisible(false);
        }

        revalidate();
        repaint();
    }

    /**
     * Appends a message to the log.
     * Thread-safe: automatically invokes on EDT if needed.
     *
     * @param message Message to append
     */
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Clears all log content.
     */
    public void clearLog() {
        SwingUtilities.invokeLater(() -> logArea.setText(""));
    }

    /**
     * Returns whether the log panel is currently expanded.
     *
     * @return true if expanded, false otherwise
     */
    public boolean isExpanded() {
        return expanded;
    }
}
