package com.weareplanet.butterfly.updater;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Centralized logging for update operations.
 * Writes to both console and persistent log file.
 */
public class UpdateLogger {
    private static final Path LOG_FILE = initializeLogFile();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static Path initializeLogFile() {
        try {
            // Log file goes in the same directory as the app JAR
            Path appLocation = Paths.get(UpdateLogger.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath());
            
            // Handle Windows URI path with leading slash
            if (appLocation.toString().startsWith("/") && appLocation.toString().length() > 2 && appLocation.toString().charAt(2) == ':') {
                appLocation = Paths.get(appLocation.toString().substring(1));
            }
            
            Path logDir = appLocation.getParent();
            Path logFile = logDir.resolve("butterfly-updates.log");
            
            // Ensure parent directory exists
            Files.createDirectories(logDir);
            
            return logFile;
        } catch (Exception e) {
            System.err.println("[UPDATE] WARNING: Could not initialize log file: " + e.getMessage());
            return null;
        }
    }

    public static void info(String message) {
        String formatted = formatMessage(message);
        System.out.println(formatted);
        writeToFile(formatted);
    }

    public static void error(String message) {
        String formatted = formatMessage(message);
        System.err.println(formatted);
        writeToFile(formatted);
    }

    public static void error(String message, Throwable e) {
        String formatted = formatMessage(message);
        System.err.println(formatted);
        writeToFile(formatted);
        
        // Write stack trace
        String stackTrace = formatStackTrace(e);
        System.err.println(stackTrace);
        writeToFile(stackTrace);
    }

    public static void section(String title) {
        String message = "[UPDATE] ===== " + title + " =====";
        info(message);
    }

    public static void logFile(String path, String label) {
        try {
            long size = Files.size(Paths.get(path));
            info("[UPDATE] " + label + ": " + path);
            info("[UPDATE] File size: " + (size / 1024) + " KB");
        } catch (Exception e) {
            info("[UPDATE] " + label + ": " + path);
        }
    }

    private static String formatMessage(String message) {
        return "[" + DATE_FORMAT.format(new Date()) + "] " + message;
    }

    private static String formatStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("    at ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    private static void writeToFile(String message) {
        if (LOG_FILE == null) {
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                LOG_FILE,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            // Silently fail if we can't write to log file
            System.err.println("[UPDATE] WARNING: Could not write to log file: " + e.getMessage());
        }
    }

    public static Path getLogFile() {
        return LOG_FILE;
    }
}
