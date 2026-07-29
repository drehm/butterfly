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
    private static Path LOG_FILE = null;
    private static boolean INITIALIZED = false;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Initialize the logger. Should be called early in app startup.
     */
    public static void init() {
        if (INITIALIZED) return;
        INITIALIZED = true;
        
        try {
            // HARDCODED PATH FOR DEBUGGING
            Path logDir = Paths.get("C:\\Users\\drehm\\Downloads\\But");
            LOG_FILE = logDir.resolve("butterfly-updates.log");
            
            // Ensure parent directory exists
            Files.createDirectories(logDir);
            
            // Create a blank line separator for new session
            writeToFile("");
            
            // Log startup message
            String startMessage = formatMessage("[UPDATE] ===== Butterfly Update Log Session Started =====");
            System.out.println(startMessage);
            writeToFile(startMessage);
            
            String appInfo = formatMessage("[UPDATE] Application: " + logDir.toAbsolutePath());
            System.out.println(appInfo);
            writeToFile(appInfo);
            
            String logInfo = formatMessage("[UPDATE] Log file: " + LOG_FILE.toAbsolutePath());
            System.out.println(logInfo);
            writeToFile(logInfo);
            
        } catch (Exception e) {
            System.err.println("[UPDATE] WARNING: Could not initialize log file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ensureInitialized() {
        if (!INITIALIZED) {
            init();
        }
    }

    public static void info(String message) {
        ensureInitialized();
        String formatted = formatMessage(message);
        System.out.println(formatted);
        writeToFile(formatted);
    }

    public static void error(String message) {
        ensureInitialized();
        String formatted = formatMessage(message);
        System.err.println(formatted);
        writeToFile(formatted);
    }

    public static void error(String message, Throwable e) {
        ensureInitialized();
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
            if (!message.isEmpty()) {
                writer.write(message);
            }
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
