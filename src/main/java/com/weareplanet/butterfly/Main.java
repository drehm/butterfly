package com.weareplanet.butterfly;

import com.weareplanet.butterfly.updater.UpdateChecker;
import com.weareplanet.butterfly.updater.UpdateInfo;
import com.weareplanet.butterfly.updater.UpdateLogger;
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
            UpdateLogger.error("Failed to load version from properties: " + e.getMessage());
        }
        return "unknown";
    }

    public static void main(String[] args) {
        UpdateLogger.info("Starting " + APP_NAME + " v" + APP_VERSION);

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
                UpdateLogger.info("Update available: " + updateInfo.version);
                //mainWindow.showUpdateAvailableNotification(updateInfo);
            }

            @Override
            public void onNoUpdateAvailable() {
                UpdateLogger.info("App is up to date");
            }

            @Override
            public void onCheckFailed(Exception e) {
                UpdateLogger.error("Update check failed: " + e.getMessage());
            }

            @Override
            public void onDownloadStarted(UpdateInfo updateInfo) {
                UpdateLogger.info("Downloading update: " + updateInfo.version);
            }

            @Override
            public void onDownloadProgress(int percentComplete) {
                UpdateLogger.info("Download progress: " + percentComplete + "%");
            }

            @Override
            public void onDownloadComplete(UpdateInfo updateInfo, java.nio.file.Path jarPath) {
                UpdateLogger.info("Download complete: " + jarPath);
                mainWindow.showInstallUpdatePrompt(updateInfo, jarPath, checker);
            }

            @Override
            public void onDownloadFailed(Exception e) {
                UpdateLogger.error("Download failed: " + e.getMessage());
            }

            @Override
            public void onInstallComplete(UpdateInfo updateInfo) {
                UpdateLogger.info("Update installed, restarting...");
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
      * This happens BEFORE the UI starts, so the JAR is not yet locked.
      * @return true if an update was found and is being installed, false otherwise
      */
     private static boolean checkAndInstallPendingUpdate() {
         try {
             Path appJar = Paths.get(UpdateChecker.class.getProtectionDomain()
                 .getCodeSource().getLocation().toURI().getPath());
            
             // Handle Windows URI path with leading slash
             if (appJar.toString().startsWith("/") && appJar.toString().length() > 2 && appJar.toString().charAt(2) == ':') {
                 appJar = Paths.get(appJar.toString().substring(1));
             }
            
             Path pendingJar = appJar.resolveSibling(appJar.getFileName() + ".pending");
            
             UpdateLogger.section("UPDATE INSTALLATION STARTUP CHECK");
             UpdateLogger.info("[UPDATE] Current app JAR: " + appJar.toAbsolutePath());
             UpdateLogger.info("[UPDATE] Checking for pending update: " + pendingJar.toAbsolutePath());
            
             if (!Files.exists(pendingJar)) {
                 UpdateLogger.info("[UPDATE] No pending update found. App will start normally.");
                 return false;
             }

             try {
                 UpdateLogger.info("[UPDATE] ");
                 UpdateLogger.section("PENDING UPDATE DETECTED");
                 UpdateLogger.info("[UPDATE] Found pending JAR: " + pendingJar.toAbsolutePath());
                 UpdateLogger.logFile(pendingJar.toAbsolutePath().toString(), "[UPDATE] Pending JAR");
                 LOGGER.info("Found pending update: " + pendingJar);
                 
                 Path backupJar = appJar.resolveSibling(appJar.getFileName() + ".backup");
                 
                 UpdateLogger.info("[UPDATE] ");
                 UpdateLogger.section("STAGE 1: BACKUP CURRENT VERSION");
                 UpdateLogger.info("[UPDATE] Backing up current JAR:");
                 UpdateLogger.info("[UPDATE]   FROM: " + appJar.toAbsolutePath());
                 UpdateLogger.info("[UPDATE]   TO:   " + backupJar.toAbsolutePath());
                 
                 // At this point, the old JAR is not locked, so we can replace it
                 Files.move(appJar, backupJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                 
                 UpdateLogger.info("[UPDATE] ✓ Backup completed. Original JAR saved.");
                 
                 try {
                     UpdateLogger.info("[UPDATE] ");
                     UpdateLogger.section("STAGE 2: INSTALL NEW VERSION");
                     UpdateLogger.info("[UPDATE] Replacing with new JAR:");
                     UpdateLogger.info("[UPDATE]   FROM: " + pendingJar.toAbsolutePath());
                     UpdateLogger.info("[UPDATE]   TO:   " + appJar.toAbsolutePath());
                     
                     Files.move(pendingJar, appJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                     
                     UpdateLogger.info("[UPDATE] ✓ New JAR installed successfully!");
                     UpdateLogger.info("[UPDATE] ");
                     UpdateLogger.section("LAUNCH INFO");
                     UpdateLogger.info("[UPDATE] Will launch: " + appJar.toAbsolutePath());
                     UpdateLogger.logFile(appJar.toAbsolutePath().toString(), "[UPDATE] New JAR");
                     UpdateLogger.info("[UPDATE] ");
                     
                     LOGGER.info("Update installed successfully");
                     return true;
                 } catch (Exception e) {
                     UpdateLogger.error("[UPDATE] ✗ INSTALLATION FAILED: " + e.getMessage(), e);
                     LOGGER.log(Level.SEVERE, "Failed to install new JAR", e);
                     
                     // Restore backup if replacement failed
                     UpdateLogger.info("[UPDATE] ");
                     UpdateLogger.section("RECOVERY: RESTORING BACKUP");
                     UpdateLogger.info("[UPDATE] Rolling back to previous version:");
                     UpdateLogger.info("[UPDATE]   FROM: " + backupJar.toAbsolutePath());
                     UpdateLogger.info("[UPDATE]   TO:   " + appJar.toAbsolutePath());
                     
                     Files.move(backupJar, appJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                     
                     UpdateLogger.info("[UPDATE] ✓ Rollback completed. Launching original version.");
                     UpdateLogger.info("[UPDATE] ");
                     throw e;
                 }
             } catch (Exception e) {
                 UpdateLogger.error("[UPDATE] ERROR during update installation: " + e.getMessage(), e);
                 LOGGER.log(Level.SEVERE, "Failed to install pending update", e);
                 return false;
             }
         } catch (Exception e) {
             UpdateLogger.error("[UPDATE] ERROR checking for pending updates: " + e.getMessage(), e);
             LOGGER.log(Level.SEVERE, "Error checking for pending updates", e);
             return false;
         }
     }
}
