package com.androidemulatormanager;

import javax.swing.JOptionPane;

public class JavaVersionChecker {
    
    public static boolean checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        String[] versionParts = javaVersion.split("\\.");
        
        try {
            int majorVersion;
            if (versionParts[0].equals("1")) {
                // Java 8 format: 1.8.x
                majorVersion = Integer.parseInt(versionParts[1]);
            } else {
                // Java 9+ format: 11.x.x, 17.x.x
                majorVersion = Integer.parseInt(versionParts[0]);
            }
            
            if (majorVersion < 11) {
                showJavaVersionWarning(javaVersion, "11");
                return false;
            } else if (majorVersion == 11) {
                showJava11Warning();
                return true;
            } else {
                // Java 17+, tutto ok
                return true;
            }
            
        } catch (NumberFormatException e) {
            showJavaVersionWarning(javaVersion, "11");
            return false;
        }
    }
    
    private static void showJavaVersionWarning(String currentVersion, String requiredVersion) {
        String message = String.format(
            "Java Version Warning\n\n" +
            "Current Java version: %s\n" +
            "Required Java version: %s or higher\n\n" +
            "Please update your Java installation to continue.\n" +
            "Download Java from: https://adoptium.net/",
            currentVersion, requiredVersion
        );
        
        JOptionPane.showMessageDialog(null, message, "Java Version Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private static void showJava11Warning() {
        String message = 
            "Java 11 Detected\n\n" +
            "You are using Java 11. This application will work, but:\n\n" +
            "• Uses older Android SDK tools (compatible with Java 11)\n" +
            "• Some newer Android API levels may not be available\n" +
            "• For best experience, consider upgrading to Java 17+\n\n" +
            "The application will automatically handle compatibility issues.";
        
        JOptionPane.showMessageDialog(null, message, "Java 11 Compatibility", JOptionPane.INFORMATION_MESSAGE);
    }
}