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
                //mainWindow.showUpdateAvailableNotification(updateInfo);
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
            
             System.out.println("[UPDATE] ===== UPDATE INSTALLATION STARTUP CHECK =====");
             System.out.println("[UPDATE] Current app JAR: " + appJar.toAbsolutePath());
             System.out.println("[UPDATE] Checking for pending update: " + pendingJar.toAbsolutePath());
            
             if (!Files.exists(pendingJar)) {
                 System.out.println("[UPDATE] No pending update found. App will start normally.");
                 return false;
             }

             try {
                 System.out.println("[UPDATE] ");
                 System.out.println("[UPDATE] ===== PENDING UPDATE DETECTED =====");
                 System.out.println("[UPDATE] Found pending JAR: " + pendingJar.toAbsolutePath());
                 System.out.println("[UPDATE] File size: " + Files.size(pendingJar) / 1024 + " KB");
                 LOGGER.info("Found pending update: " + pendingJar);
                
                 Path backupJar = appJar.resolveSibling(appJar.getFileName() + ".backup");
                
                 System.out.println("[UPDATE] ");
                 System.out.println("[UPDATE] ===== STAGE 1: BACKUP CURRENT VERSION =====");
                 System.out.println("[UPDATE] Backing up current JAR:");
                 System.out.println("[UPDATE]   FROM: " + appJar.toAbsolutePath());
                 System.out.println("[UPDATE]   TO:   " + backupJar.toAbsolutePath());
                
                 // At this point, the old JAR is not locked, so we can replace it
                 Files.move(appJar, backupJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                 System.out.println("[UPDATE] ✓ Backup completed. Original JAR saved.");
                
                 try {
                     System.out.println("[UPDATE] ");
                     System.out.println("[UPDATE] ===== STAGE 2: INSTALL NEW VERSION =====");
                     System.out.println("[UPDATE] Replacing with new JAR:");
                     System.out.println("[UPDATE]   FROM: " + pendingJar.toAbsolutePath());
                     System.out.println("[UPDATE]   TO:   " + appJar.toAbsolutePath());
                    
                     Files.move(pendingJar, appJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                     System.out.println("[UPDATE] ✓ New JAR installed successfully!");
                     System.out.println("[UPDATE] ");
                     System.out.println("[UPDATE] ===== LAUNCH INFO =====");
                     System.out.println("[UPDATE] Will launch: " + appJar.toAbsolutePath());
                     System.out.println("[UPDATE] File size: " + Files.size(appJar) / 1024 + " KB");
                     System.out.println("[UPDATE] ");
                    
                     LOGGER.info("Update installed successfully");
                     return true;
                 } catch (Exception e) {
                     System.err.println("[UPDATE] ✗ INSTALLATION FAILED: " + e.getMessage());
                     e.printStackTrace();
                     LOGGER.log(Level.SEVERE, "Failed to install new JAR", e);
                    
                     // Restore backup if replacement failed
                     System.out.println("[UPDATE] ");
                     System.out.println("[UPDATE] ===== RECOVERY: RESTORING BACKUP =====");
                     System.out.println("[UPDATE] Rolling back to previous version:");
                     System.out.println("[UPDATE]   FROM: " + backupJar.toAbsolutePath());
                     System.out.println("[UPDATE]   TO:   " + appJar.toAbsolutePath());
                    
                     Files.move(backupJar, appJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                     System.out.println("[UPDATE] ✓ Rollback completed. Launching original version.");
                     System.out.println("[UPDATE] ");
                     throw e;
                 }
             } catch (Exception e) {
                 System.err.println("[UPDATE] ERROR during update installation: " + e.getMessage());
                 e.printStackTrace();
                 LOGGER.log(Level.SEVERE, "Failed to install pending update", e);
                 return false;
             }
         } catch (Exception e) {
             System.err.println("[UPDATE] ERROR checking for pending updates: " + e.getMessage());
             e.printStackTrace();
             LOGGER.log(Level.SEVERE, "Error checking for pending updates", e);
             return false;
         }
     }
}
