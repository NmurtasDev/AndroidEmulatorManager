import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AndroidEmulatorManager extends JFrame {
    private JTextField sdkPathField;
    private JList<String> avdList;
    private DefaultListModel<String> avdListModel;
    private JTextArea logArea;
    private JButton setupSdkButton, createAvdButton, startEmulatorButton, stopEmulatorButton, deleteAvdButton, downloadSdkButton;
    private JProgressBar progressBar;
    
    private String sdkPath = "";
    private Process emulatorProcess;
    private static final String CONFIG_FILE = "android_emulator_config.properties";
    
    // URL per i Command Line Tools (aggiornare se necessario)
    private static final String CMDTOOLS_WIN_URL = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip";
    private static final String CMDTOOLS_LINUX_URL = "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip";
    private static final String CMDTOOLS_MAC_URL = "https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip";
    
    public AndroidEmulatorManager() {
        initializeComponents();
        loadConfig();
        refreshAvdList();
    }
    
    private void initializeComponents() {
        setTitle("Android Emulator Manager");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);
        
        // Layout principale
        setLayout(new BorderLayout());
        
        // Panel principale
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Sezione configurazione SDK
        JPanel configPanel = createConfigPanel();
        mainPanel.add(configPanel, BorderLayout.NORTH);
        
        // Sezione centrale con dispositivi e log
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Sezione dispositivi
        JPanel devicesPanel = createDevicesPanel();
        centerPanel.add(devicesPanel, BorderLayout.CENTER);
        
        // Sezione log
        JPanel logPanel = createLogPanel();
        centerPanel.add(logPanel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        mainPanel.add(progressBar, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Gestione chiusura finestra
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClosing();
            }
        });
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Configurazione SDK"));
        
        // Panel principale per i controlli
        JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        innerPanel.add(new JLabel("Percorso SDK:"));
        
        sdkPathField = new JTextField(40);
        innerPanel.add(sdkPathField);
        
        JButton browseButton = new JButton("Sfoglia");
        browseButton.addActionListener(e -> browseSdkPath());
        innerPanel.add(browseButton);
        
        // Panel per i bottoni di setup
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        downloadSdkButton = new JButton("Scarica SDK");
        downloadSdkButton.addActionListener(e -> downloadAndSetupSdk());
        downloadSdkButton.setBackground(new Color(76, 175, 80));
        downloadSdkButton.setForeground(Color.WHITE);
        buttonPanel.add(downloadSdkButton);
        
        setupSdkButton = new JButton("Setup SDK");
        setupSdkButton.addActionListener(e -> setupSdk());
        buttonPanel.add(setupSdkButton);
        
        panel.add(innerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createDevicesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Gestione Dispositivi Virtuali"));
        
        // Lista AVD
        avdListModel = new DefaultListModel<>();
        avdList = new JList<>(avdListModel);
        avdList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(avdList);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        panel.add(new JLabel("Dispositivi disponibili:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Pulsanti di controllo
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        createAvdButton = new JButton("Crea Nuovo");
        createAvdButton.addActionListener(e -> createAvdDialog());
        buttonPanel.add(createAvdButton);
        
        startEmulatorButton = new JButton("Avvia");
        startEmulatorButton.addActionListener(e -> startEmulator());
        buttonPanel.add(startEmulatorButton);
        
        stopEmulatorButton = new JButton("Ferma");
        stopEmulatorButton.addActionListener(e -> stopEmulator());
        buttonPanel.add(stopEmulatorButton);
        
        deleteAvdButton = new JButton("Elimina");
        deleteAvdButton.addActionListener(e -> deleteAvd());
        buttonPanel.add(deleteAvdButton);
        
        JButton refreshButton = new JButton("Aggiorna Lista");
        refreshButton.addActionListener(e -> refreshAvdList());
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log"));
        
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
    
    private void updateProgress(int value, String text) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(text);
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
        
        sdkPath = inputSdkPath;
        
        // Disabilita i bottoni durante il download
        SwingUtilities.invokeLater(() -> {
            downloadSdkButton.setEnabled(false);
            setupSdkButton.setEnabled(false);
        });
        
        CompletableFuture.runAsync(() -> {
            try {
                logMessage("=== DOWNLOAD E SETUP AUTOMATICO SDK ===");
                logMessage("Percorso SDK: " + sdkPath);
                
                // Determina l'OS e l'URL appropriato
                String os = System.getProperty("os.name").toLowerCase();
                String downloadUrl;
                String zipFileName;
                
                if (os.contains("win")) {
                    downloadUrl = CMDTOOLS_WIN_URL;
                    zipFileName = "commandlinetools-win.zip";
                    logMessage("Sistema operativo rilevato: Windows");
                } else if (os.contains("mac")) {
                    downloadUrl = CMDTOOLS_MAC_URL;
                    zipFileName = "commandlinetools-mac.zip";
                    logMessage("Sistema operativo rilevato: macOS");
                } else {
                    downloadUrl = CMDTOOLS_LINUX_URL;
                    zipFileName = "commandlinetools-linux.zip";
                    logMessage("Sistema operativo rilevato: Linux");
                }
                
                // Crea la cartella SDK
                Path sdkDir = Paths.get(sdkPath);
                Files.createDirectories(sdkDir);
                
                showProgress(true);
                updateProgress(5, "Inizializzazione download...");
                
                // Download dei Command Line Tools
                logMessage("Download Command Line Tools da: " + downloadUrl);
                Path downloadedFile = sdkDir.resolve(zipFileName);
                
                downloadFile(downloadUrl, downloadedFile);
                
                updateProgress(50, "Estrazione archivio...");
                logMessage("Estrazione archivio...");
                
                // Estrai l'archivio
                Path cmdlineToolsPath = sdkDir.resolve("cmdline-tools");
                Files.createDirectories(cmdlineToolsPath);
                
                extractZip(downloadedFile, cmdlineToolsPath);
                
                // Rinomina la cartella cmdline-tools/cmdline-tools in cmdline-tools/latest
                Path extractedCmdTools = cmdlineToolsPath.resolve("cmdline-tools");
                Path latestPath = cmdlineToolsPath.resolve("latest");
                
                if (Files.exists(extractedCmdTools)) {
                    if (Files.exists(latestPath)) {
                        deleteDirectory(latestPath);
                    }
                    Files.move(extractedCmdTools, latestPath);
                    logMessage("Cartella rinominata in 'latest'");
                }
                
                updateProgress(70, "Configurazione permessi...");
                
                // Rendi eseguibili i file su Linux/Mac
                if (!os.contains("win")) {
                    makeFilesExecutable(latestPath.resolve("bin"));
                }
                
                // Rimuovi il file zip scaricato
                Files.deleteIfExists(downloadedFile);
                
                updateProgress(80, "Installazione componenti SDK...");
                logMessage("Download completato! Installazione componenti...");
                
                // Installa i componenti SDK
                installSdkComponents();
                
                updateProgress(100, "Completato!");
                logMessage("=== SETUP COMPLETATO CON SUCCESSO! ===");
                
                saveConfig();
                
            } catch (Exception e) {
                logMessage("ERRORE durante il download/setup: " + e.getMessage());
                e.printStackTrace();
            } finally {
                showProgress(false);
                SwingUtilities.invokeLater(() -> {
                    downloadSdkButton.setEnabled(true);
                    setupSdkButton.setEnabled(true);
                });
            }
        });
    }
    
    private void downloadFile(String urlString, Path outputPath) throws IOException {
        URL url = new URL(urlString);
        try (InputStream in = url.openStream()) {
            long fileSize = url.openConnection().getContentLengthLong();
            
            try (FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                byte[] buffer = new byte[8192];
                long totalBytesRead = 0;
                int bytesRead;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    if (fileSize > 0) {
                        int progress = (int) ((totalBytesRead * 40) / fileSize) + 5; // 5-45%
                        String progressText = String.format("Download: %.1f MB / %.1f MB", 
                                                          totalBytesRead / 1024.0 / 1024.0, 
                                                          fileSize / 1024.0 / 1024.0);
                        updateProgress(progress, progressText);
                    }
                }
            }
        }
        logMessage("Download completato: " + outputPath.getFileName());
    }
    
    private void extractZip(Path zipPath, Path destPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = destPath.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }
    
    private void makeFilesExecutable(Path binPath) {
        try {
            if (Files.exists(binPath)) {
                Files.walk(binPath)
                     .filter(Files::isRegularFile)
                     .forEach(file -> {
                         try {
                             Runtime.getRuntime().exec("chmod +x " + file.toString()).waitFor();
                         } catch (Exception e) {
                             logMessage("Impossibile rendere eseguibile: " + file.getFileName());
                         }
                     });
                logMessage("Permessi di esecuzione impostati per i file bin/");
            }
        } catch (Exception e) {
            logMessage("Errore nell'impostazione permessi: " + e.getMessage());
        }
    }
    
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                 .sorted((a, b) -> b.compareTo(a)) // Reverse order for proper deletion
                 .forEach(p -> {
                     try {
                         Files.delete(p);
                     } catch (IOException e) {
                         logMessage("Impossibile eliminare: " + p);
                     }
                 });
        }
    }
    
    private void installSdkComponents() {
        try {
            String sdkManagerPath = getSdkManagerPath();
            if (sdkManagerPath == null) {
                logMessage("ERRORE: sdkmanager non trovato!");
                return;
            }
            
            // Accetta licenze
            logMessage("Accettazione licenze SDK...");
            executeCommand(new String[]{sdkManagerPath, "--licenses"}, "y\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\ny\n");
            
            // Installa componenti essenziali
            String[] components = {
                "platform-tools",
                "platforms;android-33",
                "platforms;android-34", 
                "system-images;android-33;google_apis;x86_64",
                "system-images;android-34;google_apis;x86_64",
                "emulator",
                "build-tools;34.0.0"
            };
            
            for (String component : components) {
                logMessage("Installazione " + component + "...");
                executeCommand(new String[]{sdkManagerPath, component}, null);
            }
            
            logMessage("Installazione componenti SDK completata!");
            
        } catch (Exception e) {
            logMessage("Errore nell'installazione componenti: " + e.getMessage());
        }
    }
    
    private String getSdkManagerPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String executable = os.contains("win") ? "sdkmanager.bat" : "sdkmanager";
        Path sdkManagerPath = Paths.get(sdkPath, "cmdline-tools", "latest", "bin", executable);
        
        if (Files.exists(sdkManagerPath)) {
            return sdkManagerPath.toString();
        }
        return null;
    }
    
    private void browseSdkPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Seleziona cartella SDK Android");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            sdkPathField.setText(path);
            sdkPath = path;
            saveConfig();
        }
    }
    
    private void setupSdk() {
        String inputSdkPath = sdkPathField.getText().trim();
        if (inputSdkPath.isEmpty()) {
            // Suggerisci percorso di default
            String defaultPath = System.getProperty("user.home") + File.separator + "Android" + File.separator + "sdk";
            sdkPathField.setText(defaultPath);
            inputSdkPath = defaultPath;
        }
        
        sdkPath = inputSdkPath;
        
        CompletableFuture.runAsync(() -> {
            try {
                logMessage("Inizio setup SDK...");
                
                String sdkManagerPath = getSdkManagerPath();
                if (sdkManagerPath == null) {
                    logMessage("Command line tools non trovati!");
                    logMessage("Usa il bottone 'Scarica SDK' per scaricare automaticamente l'SDK");
                    return;
                }
                
                installSdkComponents();
                saveConfig();
                
            } catch (Exception e) {
                logMessage("Errore durante il setup: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void createAvdDialog() {
        JDialog dialog = new JDialog(this, "Crea Nuovo Dispositivo Virtuale", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Nome dispositivo
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Nome dispositivo:"), gbc);
        
        JTextField nameField = new JTextField("MyDevice", 20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);
        
        // API Level
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("API Level:"), gbc);
        
        String[] apiLevels = {"30", "31", "32", "33", "34"};
        JComboBox<String> apiCombo = new JComboBox<>(apiLevels);
        apiCombo.setSelectedItem("33");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(apiCombo, gbc);
        
        // Tipo dispositivo
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Tipo dispositivo:"), gbc);
        
        String[] deviceTypes = {"pixel", "pixel_xl", "pixel_2", "pixel_3", "pixel_4", "pixel_5", "pixel_6"};
        JComboBox<String> deviceCombo = new JComboBox<>(deviceTypes);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(deviceCombo, gbc);
        
        // Pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton createButton = new JButton("Crea");
        createButton.addActionListener(e -> {
            CompletableFuture.runAsync(() -> {
                try {
                    if (sdkPath.isEmpty()) {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(dialog, "Configura prima il percorso SDK", 
                                                        "Errore", JOptionPane.ERROR_MESSAGE));
                        return;
                    }
                    
                    String avdManagerPath = getAvdManagerPath();
                    if (avdManagerPath == null) {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(dialog, "avdmanager non trovato. Esegui prima il setup SDK.", 
                                                        "Errore", JOptionPane.ERROR_MESSAGE));
                        return;
                    }
                    
                    String systemImage = "system-images;android-" + apiCombo.getSelectedItem() + ";google_apis;x86_64";
                    
                    logMessage("Creazione AVD " + nameField.getText() + "...");
                    
                    String[] command = {
                        avdManagerPath, "create", "avd",
                        "-n", nameField.getText(),
                        "-k", systemImage,
                        "-d", (String) deviceCombo.getSelectedItem()
                    };
                    
                    if (executeCommand(command, "no\n")) {
                        logMessage("AVD " + nameField.getText() + " creato con successo!");
                        SwingUtilities.invokeLater(() -> {
                            refreshAvdList();
                            dialog.dispose();
                        });
                    } else {
                        logMessage("Errore nella creazione dell'AVD");
                    }
                    
                } catch (Exception ex) {
                    logMessage("Errore: " + ex.getMessage());
                }
            });
        });
        
        JButton cancelButton = new JButton("Annulla");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private String getAvdManagerPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String executable = os.contains("win") ? "avdmanager.bat" : "avdmanager";
        Path avdManagerPath = Paths.get(sdkPath, "cmdline-tools", "latest", "bin", executable);
        
        if (Files.exists(avdManagerPath)) {
            return avdManagerPath.toString();
        }
        return null;
    }
    
    private void refreshAvdList() {
        CompletableFuture.runAsync(() -> {
            try {
                if (sdkPath.isEmpty()) return;
                
                String avdManagerPath = getAvdManagerPath();
                if (avdManagerPath == null) return;
                
                ProcessBuilder pb = new ProcessBuilder(avdManagerPath, "list", "avd");
                pb.environment().put("ANDROID_HOME", sdkPath);
                pb.environment().put("ANDROID_SDK_ROOT", sdkPath);
                
                Process process = pb.start();
                
                List<String> avds = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().startsWith("Name:")) {
                            String avdName = line.split(":", 2)[1].trim();
                            avds.add(avdName);
                        }
                    }
                }
                
                SwingUtilities.invokeLater(() -> {
                    avdListModel.clear();
                    for (String avd : avds) {
                        avdListModel.addElement(avd);
                    }
                });
                
            } catch (Exception e) {
                logMessage("Errore nell'aggiornamento lista: " + e.getMessage());
            }
        });
    }
    
    private void startEmulator() {
        String selectedAvd = avdList.getSelectedValue();
        if (selectedAvd == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un dispositivo da avviare", 
                                        "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                String executable = os.contains("win") ? "emulator.exe" : "emulator";
                String emulatorPath = Paths.get(sdkPath, "emulator", executable).toString();
                
                ProcessBuilder pb = new ProcessBuilder(emulatorPath, "-avd", selectedAvd);
                pb.environment().put("ANDROID_HOME", sdkPath);
                pb.environment().put("ANDROID_SDK_ROOT", sdkPath);
                
                logMessage("Avvio emulatore " + selectedAvd + "...");
                emulatorProcess = pb.start();
                logMessage("Emulatore " + selectedAvd + " avviato (PID: " + emulatorProcess.pid() + ")");
                
            } catch (Exception e) {
                logMessage("Errore nell'avvio emulatore: " + e.getMessage());
            }
        });
    }
    
    private void stopEmulator() {
        if (emulatorProcess != null && emulatorProcess.isAlive()) {
            emulatorProcess.destroy();
            logMessage("Emulatore fermato");
            emulatorProcess = null;
        } else {
            JOptionPane.showMessageDialog(this, "Nessun emulatore in esecuzione", 
                                        "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void deleteAvd() {
        String selectedAvd = avdList.getSelectedValue();
        if (selectedAvd == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un dispositivo da eliminare", 
                                        "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Eliminare il dispositivo '" + selectedAvd + "'?", 
            "Conferma", JOptionPane.YES_NO_OPTION);
        
        if (result != JOptionPane.YES_OPTION) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                String avdManagerPath = getAvdManagerPath();
                if (avdManagerPath == null) {
                    logMessage("avdmanager non trovato");
                    return;
                }
                
                logMessage("Eliminazione AVD " + selectedAvd + "...");
                
                String[] command = {avdManagerPath, "delete", "avd", "-n", selectedAvd};
                
                if (executeCommand(command, null)) {
                    logMessage("AVD " + selectedAvd + " eliminato con successo!");
                    SwingUtilities.invokeLater(() -> refreshAvdList());
                } else {
                    logMessage("Errore nell'eliminazione dell'AVD");
                }
                
            } catch (Exception e) {
                logMessage("Errore: " + e.getMessage());
            }
        });
    }
    
    private boolean executeCommand(String[] command, String input) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.environment().put("ANDROID_HOME", sdkPath);
            pb.environment().put("ANDROID_SDK_ROOT", sdkPath);
            
            Process process = pb.start();
            
            // Fornisce input se necessario
            if (input != null && !input.isEmpty()) {
                try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                    writer.print(input);
                    writer.flush();
                }
            }
            
            // Legge output ed errori
            CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logMessage(line);
                    }
                } catch (Exception e) {
                    logMessage("Errore lettura output: " + e.getMessage());
                }
            });
            
            CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logMessage("ERROR: " + line);
                    }
                } catch (Exception e) {
                    logMessage("Errore lettura error stream: " + e.getMessage());
                }
            });
            
            return process.waitFor() == 0;
            
        } catch (Exception e) {
            logMessage("Errore esecuzione comando: " + e.getMessage());
            return false;
        }
    }
    
    private void saveConfig() {
        Properties props = new Properties();
        props.setProperty("sdk_path", sdkPath);
        
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Android Emulator Manager Configuration");
        } catch (Exception e) {
            logMessage("Errore nel salvare configurazione: " + e.getMessage());
        }
    }
    
    private void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
            sdkPath = props.getProperty("sdk_path", "");
            sdkPathField.setText(sdkPath);
        } catch (Exception e) {
            // Ignora errori di caricamento
        }
    }
    
    private void onClosing() {
        if (emulatorProcess != null && emulatorProcess.isAlive()) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Emulatore in esecuzione. Fermarlo e uscire?", 
                "Conferma", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                stopEmulator();
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {            
            new AndroidEmulatorManager().setVisible(true);
        });
    }
}