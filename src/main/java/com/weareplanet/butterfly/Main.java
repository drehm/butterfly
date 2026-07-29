package com.weareplanet.butterfly;

import com.weareplanet.butterfly.updater.UpdateChecker;
import com.weareplanet.butterfly.updater.UpdateInfo;
import com.weareplanet.butterfly.ui.MainWindow;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for Butterfly application.
 * Initializes the application and auto-updater system.
 */
public class Main {
    private static final String APP_NAME = "Butterfly";
    private static String APP_VERSION;
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    static {
        // Load version from properties file (injected by Maven)
        APP_VERSION = loadAppVersion();
    }

    private static String loadAppVersion() {
        try {
            Properties props = new Properties();
            try (InputStream is = Main.class.getResourceAsStream("/app.properties")) {
                if (is != null) {
                    props.load(is);
                    String version = props.getProperty("app.version");
                    if (version != null && !version.isEmpty() && !version.startsWith("${")) {
                        return version;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load version from properties: " + e.getMessage());
        }
        return "unknown";
    }

    public static void main(String[] args) {
        System.out.println("Starting " + APP_NAME + " v" + APP_VERSION);

        // Check if there's a pending update from previous session
        if (checkAndInstallPendingUpdate()) {
            return; // Application will restart after update install
        }

        // Initialize UI
        MainWindow mainWindow = new MainWindow(APP_NAME, APP_VERSION);
        mainWindow.setVisible(true);

        // Initialize auto-updater in background
        initializeAutoUpdater(mainWindow);
    }

    private static void initializeAutoUpdater(MainWindow mainWindow) {
        UpdateChecker checker = new UpdateChecker(APP_VERSION);

        checker.setUpdateListener(new UpdateChecker.UpdateListener() {
            @Override
            public void onUpdateAvailable(UpdateInfo updateInfo) {
                System.out.println("Update available: " + updateInfo.version);
                mainWindow.showUpdateAvailableNotification(updateInfo);
            }

            @Override
            public void onNoUpdateAvailable() {
                System.out.println("App is up to date");
            }

            @Override
            public void onCheckFailed(Exception e) {
                System.err.println("Update check failed: " + e.getMessage());
            }

            @Override
            public void onDownloadStarted(UpdateInfo updateInfo) {
                System.out.println("Downloading update: " + updateInfo.version);
            }

            @Override
            public void onDownloadProgress(int percentComplete) {
                System.out.println("Download progress: " + percentComplete + "%");
            }

            @Override
            public void onDownloadComplete(UpdateInfo updateInfo, java.nio.file.Path jarPath) {
                System.out.println("Download complete: " + jarPath);
                mainWindow.showInstallUpdatePrompt(updateInfo, jarPath, checker);
            }

            @Override
            public void onDownloadFailed(Exception e) {
                System.err.println("Download failed: " + e.getMessage());
            }

            @Override
            public void onInstallComplete(UpdateInfo updateInfo) {
                System.out.println("Update installed, restarting...");
            }
        });

        // Check for updates asynchronously
        checker.checkForUpdatesAsync();

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            checker.shutdown();
        }));
    }

     /**
      * Check if there's a pending update to install from a previous session.
      * @return true if an update was found and is being installed, false otherwise
      */
     private static boolean checkAndInstallPendingUpdate() {
         try {
             Path updateDir = Paths.get("update");
             if (!Files.exists(updateDir)) {
                 return false;
             }

             // Find any JAR files in the updates directory
             return Files.list(updateDir)
                 .filter(p -> p.toString().endsWith(".jar"))
                 .findFirst()
                 .map(jarPath -> {
                     try {
                         LOGGER.info("Found pending update: " + jarPath);
                        
                         // Extract version from filename (butterfly-X.Y.Z.jar)
                         String fileName = jarPath.getFileName().toString();
                         // Remove "butterfly-" prefix and ".jar" suffix
                         String version = fileName.replaceAll("^butterfly-", "").replaceAll("\\.jar$", "");
                        
                         UpdateInfo updateInfo = new UpdateInfo();
                         updateInfo.version = version;
                         
                         // Install the update
                         UpdateChecker checker = new UpdateChecker(APP_VERSION);
                         checker.installUpdate(updateInfo, jarPath);
                         
                         // Restart the application
                         checker.restartApplication();
                         
                         return true;
                     } catch (Exception e) {
                         LOGGER.log(Level.WARNING, "Failed to install pending update", e);
                         return false;
                     }
                 })
                 .orElse(false);
         } catch (Exception e) {
             LOGGER.log(Level.WARNING, "Error checking for pending updates", e);
             return false;
         }
     }
}
