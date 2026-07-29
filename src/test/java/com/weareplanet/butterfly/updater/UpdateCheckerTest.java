package com.weareplanet.butterfly.updater;

import java.nio.file.Path;

/**
 * Simple test to verify UpdateChecker can fetch and parse GitHub releases.
 */
public class UpdateCheckerTest {
    public static void main(String[] args) {
        System.out.println("Testing UpdateChecker with GitHub API...");
        
        UpdateChecker checker = new UpdateChecker("0.0.0.1");
        
        checker.setUpdateListener(new UpdateChecker.UpdateListener() {
            @Override
            public void onUpdateAvailable(UpdateInfo updateInfo) {
                System.out.println("✓ UPDATE AVAILABLE:");
                System.out.println("  Version: " + updateInfo.version);
                System.out.println("  Download URL: " + updateInfo.downloadUrl);
                System.out.println("  Release Notes: " + updateInfo.releaseNotes);
            }

            @Override
            public void onNoUpdateAvailable() {
                System.out.println("✓ No update available (running latest version)");
            }

            @Override
            public void onCheckFailed(Exception e) {
                System.out.println("✗ Check failed: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onDownloadStarted(UpdateInfo updateInfo) {
                System.out.println("→ Download started for version " + updateInfo.version);
            }

            @Override
            public void onDownloadProgress(int percentComplete) {
                if (percentComplete % 10 == 0) {
                    System.out.println("→ Download progress: " + percentComplete + "%");
                }
            }

            @Override
            public void onDownloadComplete(UpdateInfo updateInfo, Path jarPath) {
                System.out.println("✓ Download complete: " + jarPath);
            }

            @Override
            public void onDownloadFailed(Exception e) {
                System.out.println("✗ Download failed: " + e.getMessage());
            }

            @Override
            public void onInstallComplete(UpdateInfo updateInfo) {
                System.out.println("✓ Update installed successfully");
            }
        });
        
        System.out.println("Checking for updates...");
        checker.checkForUpdates();
        
        // Give async operations time to complete
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        checker.shutdown();
        System.out.println("\nTest complete!");
    }
}
