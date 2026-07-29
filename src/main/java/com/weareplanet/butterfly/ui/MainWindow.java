package com.weareplanet.butterfly.ui;

import com.weareplanet.butterfly.updater.UpdateInfo;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window with update notification support.
 */
public class MainWindow extends JFrame {
    private String appVersion;

    public MainWindow(String title, String version) {
        super(title);
        this.appVersion = version;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Set window icons
        setWindowIcons();

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Welcome to Butterfly v" + version);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Content area
        JTextArea textArea = new JTextArea();
        textArea.setText("Butterfly - Payment Terminal Management Application\n\n"
                + "Version: " + version + "\n"
                + "Status: Running\n"
                + "\nThe application is ready to use.\n"
                + "Updates will be checked automatically.");
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer
        JLabel statusLabel = new JLabel("Status: Checking for updates...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public void showUpdateAvailableNotification(UpdateInfo updateInfo) {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "New version available!\n\n"
                        + "Current: " + appVersion + "\n"
                        + "Available: " + updateInfo.version + "\n\n"
                        + (updateInfo.releaseNotes != null ? updateInfo.releaseNotes : "")
                        + "\n\nWould you like to install this update?",
                "Update Available",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this,
                        "Update will be installed on next restart.",
                        "Update Scheduled",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void showInstallUpdatePrompt(UpdateInfo updateInfo, java.nio.file.Path jarPath, com.weareplanet.butterfly.updater.UpdateChecker checker) {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Update downloaded successfully!\n\n"
                        + "Current: " + appVersion + "\n"
                        + "Available: " + updateInfo.version + "\n\n"
                        + "Would you like to install and restart now?",
                "Install Update",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    System.out.println("[UPDATE] User clicked Install. JAR path: " + jarPath.toAbsolutePath());
                    checker.installUpdate(updateInfo, jarPath);
                    System.out.println("[UPDATE] Installation staged. Now restarting app...");
                    checker.restartApplication();
                } catch (Exception e) {
                    System.err.println("[UPDATE] Installation failed: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Failed to install update: " + e.getMessage(),
                            "Installation Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("[UPDATE] User cancelled installation. Update will be applied on next restart.");
            }
        });
    }

    private void setWindowIcons() {
        try {
            List<Image> icons = new ArrayList<>();
            int[] sizes = {16, 32, 48, 128, 256};

            for (int size : sizes) {
                URL iconUrl = getClass().getResource("/icons/butterfly_" + size + ".png");
                if (iconUrl != null) {
                    ImageIcon imageIcon = new ImageIcon(iconUrl);
                    icons.add(imageIcon.getImage());
                }
            }

            if (!icons.isEmpty()) {
                setIconImages(icons);
            }
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }
    }
}
