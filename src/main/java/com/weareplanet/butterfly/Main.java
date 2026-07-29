package com.weareplanet.butterfly;

import com.weareplanet.butterfly.updater.UpdateChecker;
import com.weareplanet.butterfly.updater.UpdateInfo;
import com.weareplanet.butterfly.ui.MainWindow;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main entry point for Butterfly application.
 * Initializes the application and auto-updater system.
 */
public class Main {
    private static final String APP_NAME = "Butterfly";
    private static String APP_VERSION;

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
}
