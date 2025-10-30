package com.androidemulatormanager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class AndroidEmulatorManager extends JFrame {
    private JTextField sdkPathField;
    private JList<String> avdList;
    private DefaultListModel<String> avdListModel;
    private JTextArea logArea;
    private JButton setupSdkButton, createAvdButton, startEmulatorButton, stopEmulatorButton, deleteAvdButton, downloadSdkButton;
    private JProgressBar progressBar;
    
    private final LocalizationManager localization;
    private final ConfigurationManager configManager;
    private final SdkManager sdkManager;
    private final EmulatorManager emulatorManager;
    
    public AndroidEmulatorManager() {
        this.localization = new LocalizationManager();
        this.configManager = new ConfigurationManager(localization);
        this.sdkManager = new SdkManager(localization, this::logMessage, this::updateProgress);
        this.emulatorManager = new EmulatorManager(localization, this::logMessage);
        
        initializeComponents();
        loadConfiguration();
        refreshAvdList();
    }
    
    private void initializeComponents() {
        setTitle(localization.getMessage("window.title"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel configPanel = createConfigPanel();
        mainPanel.add(configPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        JPanel devicesPanel = createDevicesPanel();
        centerPanel.add(devicesPanel, BorderLayout.CENTER);
        
        JPanel logPanel = createLogPanel();
        centerPanel.add(logPanel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        mainPanel.add(progressBar, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClosing();
            }
        });
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(localization.getMessage("config.panel.title")));
        
        JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        innerPanel.add(new JLabel(localization.getMessage("label.sdk.path")));
        
        sdkPathField = new JTextField(40);
        innerPanel.add(sdkPathField);
        
        JButton browseButton = new JButton(localization.getMessage("button.browse"));
        browseButton.addActionListener(e -> browseSdkPath());
        innerPanel.add(browseButton);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        downloadSdkButton = new JButton(localization.getMessage("button.download.sdk"));
        downloadSdkButton.addActionListener(e -> downloadAndSetupSdk());
        downloadSdkButton.setBackground(new Color(76, 175, 80));
        downloadSdkButton.setForeground(Color.WHITE);
        buttonPanel.add(downloadSdkButton);
        
        setupSdkButton = new JButton(localization.getMessage("button.setup.sdk"));
        setupSdkButton.addActionListener(e -> setupSdk());
        buttonPanel.add(setupSdkButton);
        
        panel.add(innerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createDevicesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(localization.getMessage("devices.panel.title")));
        
        avdListModel = new DefaultListModel<>();
        avdList = new JList<>(avdListModel);
        avdList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(avdList);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        panel.add(new JLabel(localization.getMessage("label.devices.available")), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        createAvdButton = new JButton(localization.getMessage("button.create.new"));
        createAvdButton.addActionListener(e -> createAvdDialog());
        buttonPanel.add(createAvdButton);
        
        startEmulatorButton = new JButton(localization.getMessage("button.start"));
        startEmulatorButton.addActionListener(e -> startEmulator());
        buttonPanel.add(startEmulatorButton);
        
        stopEmulatorButton = new JButton(localization.getMessage("button.stop"));
        stopEmulatorButton.addActionListener(e -> stopEmulator());
        buttonPanel.add(stopEmulatorButton);
        
        deleteAvdButton = new JButton(localization.getMessage("button.delete"));
        deleteAvdButton.addActionListener(e -> deleteAvd());
        buttonPanel.add(deleteAvdButton);
        
        JButton refreshButton = new JButton(localization.getMessage("button.refresh.list"));
        refreshButton.addActionListener(e -> refreshAvdList());
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(localization.getMessage("log.panel.title")));
        
        logArea = new JTextArea(12, 0);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void updateProgress(SdkManager.ProgressInfo progressInfo) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progressInfo.value);
            progressBar.setString(progressInfo.text);
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
    
    private void downloadAndSetupSdk() {
        String inputSdkPath = sdkPathField.getText().trim();
        if (inputSdkPath.isEmpty()) {
            String defaultPath = System.getProperty("user.home") + File.separator + "Android" + File.separator + "sdk";
            sdkPathField.setText(defaultPath);
            inputSdkPath = defaultPath;
        }
        
        configManager.setSdkPath(inputSdkPath);
        
        SwingUtilities.invokeLater(() -> {
            downloadSdkButton.setEnabled(false);
            setupSdkButton.setEnabled(false);
        });
        
        showProgress(true);
        
        sdkManager.downloadAndSetupSdk(inputSdkPath).whenComplete((result, throwable) -> {
            showProgress(false);
            SwingUtilities.invokeLater(() -> {
                downloadSdkButton.setEnabled(true);
                setupSdkButton.setEnabled(true);
            });
        });
    }
    
    private void browseSdkPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(localization.getMessage("dialog.browse.title"));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            sdkPathField.setText(path);
            configManager.setSdkPath(path);
        }
    }
    
    private void setupSdk() {
        String inputSdkPath = sdkPathField.getText().trim();
        if (inputSdkPath.isEmpty()) {
            String defaultPath = System.getProperty("user.home") + File.separator + "Android" + File.separator + "sdk";
            sdkPathField.setText(defaultPath);
            inputSdkPath = defaultPath;
        }
        
        configManager.setSdkPath(inputSdkPath);
        sdkManager.setupSdk(inputSdkPath);
    }
    
    private void createAvdDialog() {
        JDialog dialog = new JDialog(this, localization.getMessage("dialog.create.avd.title"), true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(localization.getMessage("label.device.name")), gbc);
        
        JTextField nameField = new JTextField("MyDevice", 20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(localization.getMessage("label.api.level")), gbc);
        
        String[] apiLevels = {"30", "31", "32", "33", "34"};
        JComboBox<String> apiCombo = new JComboBox<>(apiLevels);
        apiCombo.setSelectedItem("33");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(apiCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(localization.getMessage("label.device.type")), gbc);
        
        String[] deviceTypes = {"pixel", "pixel_xl", "pixel_2", "pixel_3", "pixel_4", "pixel_5", "pixel_6"};
        JComboBox<String> deviceCombo = new JComboBox<>(deviceTypes);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(deviceCombo, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton createButton = new JButton(localization.getMessage("button.create"));
        createButton.addActionListener(e -> {
            String sdkPath = configManager.getSdkPath();
            if (sdkPath.isEmpty()) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(dialog, localization.getMessage("error.config.sdk.first"), 
                                                localization.getMessage("error.title"), JOptionPane.ERROR_MESSAGE));
                return;
            }
            
            String avdManagerPath = sdkManager.getAvdManagerPath(sdkPath);
            if (avdManagerPath == null) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(dialog, localization.getMessage("error.avdmanager.not.found"), 
                                                localization.getMessage("error.title"), JOptionPane.ERROR_MESSAGE));
                return;
            }
            
            emulatorManager.createAvd(sdkPath, avdManagerPath, nameField.getText(), 
                (String) apiCombo.getSelectedItem(), (String) deviceCombo.getSelectedItem())
                .thenAccept(success -> {
                    if (success) {
                        SwingUtilities.invokeLater(() -> {
                            refreshAvdList();
                            dialog.dispose();
                        });
                    }
                });
        });
        
        JButton cancelButton = new JButton(localization.getMessage("button.cancel"));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void refreshAvdList() {
        String sdkPath = configManager.getSdkPath();
        String avdManagerPath = sdkManager.getAvdManagerPath(sdkPath);
        
        emulatorManager.refreshAvdList(sdkPath, avdManagerPath)
            .thenAccept(avds -> {
                SwingUtilities.invokeLater(() -> {
                    avdListModel.clear();
                    for (String avd : avds) {
                        avdListModel.addElement(avd);
                    }
                });
            });
    }
    
    private void startEmulator() {
        String selectedAvd = avdList.getSelectedValue();
        if (selectedAvd == null) {
            JOptionPane.showMessageDialog(this, localization.getMessage("error.select.device.start"), 
                                        localization.getMessage("warning.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        emulatorManager.startEmulator(configManager.getSdkPath(), selectedAvd);
    }
    
    private void stopEmulator() {
        if (emulatorManager.isEmulatorRunning()) {
            emulatorManager.stopEmulator();
        } else {
            JOptionPane.showMessageDialog(this, localization.getMessage("error.no.emulator.running"), 
                                        localization.getMessage("info.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void deleteAvd() {
        String selectedAvd = avdList.getSelectedValue();
        if (selectedAvd == null) {
            JOptionPane.showMessageDialog(this, localization.getMessage("error.select.device.delete"), 
                                        localization.getMessage("warning.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            localization.getMessage("dialog.confirm.delete", selectedAvd), 
            localization.getMessage("dialog.confirm.title"), JOptionPane.YES_NO_OPTION);
        
        if (result != JOptionPane.YES_OPTION) return;
        
        String sdkPath = configManager.getSdkPath();
        String avdManagerPath = sdkManager.getAvdManagerPath(sdkPath);
        
        emulatorManager.deleteAvd(sdkPath, avdManagerPath, selectedAvd)
            .thenAccept(success -> {
                if (success) {
                    SwingUtilities.invokeLater(this::refreshAvdList);
                }
            });
    }
    
    private void loadConfiguration() {
        sdkPathField.setText(configManager.getSdkPath());
    }
    
    private void onClosing() {
        if (emulatorManager.isEmulatorRunning()) {
            int result = JOptionPane.showConfirmDialog(this, 
                localization.getMessage("dialog.confirm.close"), 
                localization.getMessage("dialog.confirm.title"), JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                emulatorManager.stopEmulator();
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        // Check Java version before starting the application
        if (!JavaVersionChecker.checkJavaVersion()) {
            System.exit(1);
        }
        
        SwingUtilities.invokeLater(() -> {            
            new AndroidEmulatorManager().setVisible(true);
        });
    }
}