package net.nicolamurtas.android.emulator.view;

import net.nicolamurtas.android.emulator.util.DeviceNameFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.util.*;
import java.util.List;

/**
 * Factory class for creating standardized dialogs.
 * Centralizes dialog creation logic for consistency.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public final class DialogFactory {
    private static final Logger logger = LoggerFactory.getLogger(DialogFactory.class);

    private DialogFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Shows the Android SDK License Agreement dialog.
     *
     * @param parent Parent component
     * @return true if user accepted, false otherwise
     */
    public static boolean showLicenseAgreementDialog(Component parent) {
        String licenseText = """
                ANDROID SOFTWARE DEVELOPMENT KIT LICENSE AGREEMENT

                1. Introduction

                1.1 The Android Software Development Kit (referred to in the License Agreement as the "SDK"
                and specifically including the Android system files, packaged APIs, and Google APIs add-ons)
                is licensed to you subject to the terms of the License Agreement. The License Agreement forms
                a legally binding contract between you and Google in relation to your use of the SDK.

                1.2 "Android" means the Android software stack for devices, as made available under the
                Android Open Source Project, which is located at the following URL:
                https://source.android.com/, as updated from time to time.

                1.3 A "compatible implementation" means any Android device that (i) complies with the Android
                Compatibility Definition document, which can be found at the Android compatibility website
                (https://source.android.com/compatibility) and which may be updated from time to time; and
                (ii) successfully passes the Android Compatibility Test Suite (CTS).

                1.4 "Google" means Google LLC, a Delaware corporation with principal place of business at
                1600 Amphitheatre Parkway, Mountain View, CA 94043, United States.


                2. Accepting this License Agreement

                2.1 In order to use the SDK, you must first agree to the License Agreement. You may not use
                the SDK if you do not accept the License Agreement.

                2.2 By clicking to accept, you hereby agree to the terms of the License Agreement.

                2.3 You may not use the SDK and may not accept the License Agreement if you are a person
                barred from receiving the SDK under the laws of the United States or other countries,
                including the country in which you are resident or from which you use the SDK.

                2.4 If you are agreeing to be bound by the License Agreement on behalf of your employer or
                other entity, you represent and warrant that you have full legal authority to bind your
                employer or such entity to the License Agreement. If you do not have the requisite authority,
                you may not accept the License Agreement or use the SDK on behalf of your employer or other
                entity.


                3. SDK License from Google

                3.1 Subject to the terms of the License Agreement, Google grants you a limited, worldwide,
                royalty-free, non-assignable, non-exclusive, and non-sublicensable license to use the SDK
                solely to develop applications for compatible implementations of Android.

                3.2 You may not use this SDK to develop applications for other platforms (including
                non-compatible implementations of Android) or to develop another SDK. You are of course free
                to develop applications for other platforms, including non-compatible implementations of
                Android, provided that this SDK is not used for that purpose.

                3.3 You agree that Google or third parties own all legal right, title and interest in and to
                the SDK, including any Intellectual Property Rights that subsist in the SDK. "Intellectual
                Property Rights" means any and all rights under patent law, copyright law, trade secret law,
                trademark law, and any and all other proprietary rights. Google reserves all rights not
                expressly granted to you.


                For the complete license agreement, please visit:
                https://developer.android.com/studio/terms


                BY CLICKING "I ACCEPT" BELOW, YOU ACKNOWLEDGE THAT YOU HAVE READ AND UNDERSTOOD THE ABOVE
                TERMS AND CONDITIONS AND AGREE TO BE BOUND BY THEM.
                """;

        JTextArea textArea = new JTextArea(licenseText);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 500));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Android SDK License Agreement");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(titleLabel, BorderLayout.NORTH);

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JLabel noteLabel = new JLabel("<html><b>Note:</b> You must accept this license to download and use the Android SDK.</html>");
        noteLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        bottomPanel.add(noteLabel, BorderLayout.NORTH);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton linkButton = new JButton("View Full License at developer.android.com");
        linkButton.setBorderPainted(false);
        linkButton.setContentAreaFilled(false);
        linkButton.setForeground(new Color(33, 150, 243));
        linkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://developer.android.com/studio/terms"));
            } catch (Exception ex) {
                logger.warn("Could not open browser", ex);
            }
        });
        linkPanel.add(linkButton);
        bottomPanel.add(linkPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        Object[] options = {"I Accept", "I Decline"};
        int result = JOptionPane.showOptionDialog(
            parent,
            panel,
            "Android SDK License Agreement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[1] // Default to "I Decline"
        );

        boolean accepted = (result == JOptionPane.YES_OPTION);
        logger.info("Android SDK License Agreement {}", accepted ? "accepted" : "declined");
        return accepted;
    }

    /**
     * Shows SDK component selection dialog.
     *
     * @param parent Parent component
     * @return List of selected components, or null if cancelled
     */
    public static List<String> showSdkComponentSelectionDialog(Component parent) {
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

        String[] essentialComponents = {"platform-tools", "emulator", "build-tools;35.0.0"};
        for (String component : essentialComponents) {
            gbc.gridy++;
            JCheckBox cb = new JCheckBox(component, true);
            cb.setEnabled(false);
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
        int result = JOptionPane.showConfirmDialog(parent, new JScrollPane(panel),
            "SDK Component Selection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        // Build selected components list
        List<String> selectedComponents = new ArrayList<>();
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

    /**
     * Data class for AVD creation parameters.
     */
    public static class AvdCreationParams {
        public final String name;
        public final String apiLevel;
        public final String device;

        public AvdCreationParams(String name, String apiLevel, String device) {
            this.name = name;
            this.apiLevel = apiLevel;
            this.device = device;
        }
    }

    /**
     * Shows create AVD dialog.
     *
     * @param parent Parent component
     * @return AVD creation parameters, or null if cancelled
     */
    public static AvdCreationParams showCreateAvdDialog(Component parent) {
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

        int result = JOptionPane.showConfirmDialog(parent, panel,
            "Create New AVD", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String avdName = nameField.getText().trim();

        // Validate AVD name
        if (!DeviceNameFormatter.isValidAvdName(avdName)) {
            JOptionPane.showMessageDialog(parent,
                "Invalid AVD name!\n\n" +
                "The name cannot contain spaces or special characters.\n" +
                "Use letters, numbers, underscores, and hyphens only.",
                "Invalid Name", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Determine which API level to use (standard or legacy)
        String selectedApi = legacyCheckBox.isSelected() ?
            (String) legacyApiCombo.getSelectedItem() :
            (String) apiCombo.getSelectedItem();

        return new AvdCreationParams(avdName, selectedApi, (String) deviceCombo.getSelectedItem());
    }

    /**
     * Shows rename AVD dialog.
     *
     * @param parent Parent component
     * @param oldName Current AVD name
     * @return New name, or null if cancelled or invalid
     */
    public static String showRenameAvdDialog(Component parent, String oldName) {
        String newName = JOptionPane.showInputDialog(parent,
            "Enter new name for AVD '" + oldName + "':\n\n" +
            "(letters, numbers, underscores, and hyphens only)",
            "Rename AVD",
            JOptionPane.PLAIN_MESSAGE);

        if (newName != null && !newName.trim().isEmpty() && !newName.equals(oldName)) {
            newName = newName.trim();

            // Validate AVD name
            if (!DeviceNameFormatter.isValidAvdName(newName)) {
                JOptionPane.showMessageDialog(parent,
                    "Invalid AVD name!\n\n" +
                    "The name cannot contain spaces or special characters.\n" +
                    "Use letters, numbers, underscores, and hyphens only.",
                    "Invalid Name", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            return newName;
        }

        return null;
    }

    /**
     * Shows delete confirmation dialog.
     *
     * @param parent Parent component
     * @param avdName AVD name to delete
     * @return true if confirmed, false otherwise
     */
    public static boolean showDeleteConfirmation(Component parent, String avdName) {
        int result = JOptionPane.showConfirmDialog(parent,
            "Delete AVD '" + avdName + "'?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        return result == JOptionPane.YES_OPTION;
    }
}
