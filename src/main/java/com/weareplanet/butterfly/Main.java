package com.weareplanet.butterfly;

import com.weareplanet.butterfly.updater.UpdateChecker;
import com.weareplanet.butterfly.updater.UpdateInfo;
import com.weareplanet.butterfly.ui.MainWindow;

/**
 * Main entry point for Butterfly application.
 * Initializes the application and auto-updater system.
 */
public class Main {
    private static final String APP_VERSION = "0.0.0.1";
    private static final String APP_NAME = "Butterfly";

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
