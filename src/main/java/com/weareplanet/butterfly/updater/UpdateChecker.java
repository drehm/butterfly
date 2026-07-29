package com.weareplanet.butterfly.updater;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auto-updater for Butterfly application (like Firefly's electron-updater).
 * Checks for new versions, downloads updates, and manages installation.
 */
public class UpdateChecker {
    private static final Logger LOGGER = Logger.getLogger(UpdateChecker.class.getName());
    private static final String DEFAULT_UPDATE_SERVER = "https://api.github.com/repos/drehm/butterfly/releases/latest";
    private static final int SOCKET_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 10000;

    private final String currentVersion;
    private final Path updateDir;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private UpdateListener listener;

    public UpdateChecker(String currentVersion) {
        this.currentVersion = currentVersion;
        // Use absolute path relative to application JAR location
        Path dir = null;
        try {
            Path appLocation = Paths.get(UpdateChecker.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath());
            
            // Handle Windows URI path with leading slash
            if (appLocation.toString().startsWith("/") && appLocation.toString().length() > 2 && appLocation.toString().charAt(2) == ':') {
                appLocation = Paths.get(appLocation.toString().substring(1));
            }
            
            dir = appLocation.getParent().resolve("update");
        } catch (Exception e) {
            // Fallback to relative path if we can't determine app location
            System.err.println("[UPDATE] Warning: Could not determine app location, using relative path: " + e.getMessage());
            dir = Paths.get("update").toAbsolutePath();
        }
        this.updateDir = dir;
        ensureUpdateDirExists();
    }

    public void setUpdateListener(UpdateListener listener) {
        this.listener = listener;
    }

    public void checkForUpdatesAsync() {
        executor.execute(this::checkForUpdates);
    }

    public void checkForUpdates() {
        try {
            UpdateInfo updateInfo = fetchUpdateInfo();
            if (updateInfo != null && isNewerVersion(updateInfo.version, currentVersion)) {
                System.out.println("[UPDATE] Update available: " + updateInfo.version);
                LOGGER.info("Update available: " + updateInfo.version);
                if (listener != null) {
                    listener.onUpdateAvailable(updateInfo);
                }
                downloadUpdate(updateInfo);
            } else {
                System.out.println("[UPDATE] Already running latest version: " + currentVersion);
                LOGGER.info("Already running latest version: " + currentVersion);
                if (listener != null) {
                    listener.onNoUpdateAvailable();
                }
            }
        } catch (Exception e) {
            System.err.println("[UPDATE] Failed to check for updates: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Failed to check for updates", e);
            if (listener != null) {
                listener.onCheckFailed(e);
            }
        }
    }

    private UpdateInfo fetchUpdateInfo() throws Exception {
        URL url = new URL(DEFAULT_UPDATE_SERVER);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(SOCKET_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        
        // Try to use GitHub token if available
        String token = getGitHubToken();
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        try {
            if (conn.getResponseCode() != 200) {
                throw new IOException("Server returned: " + conn.getResponseCode());
            }

            String response = readStream(conn.getInputStream());
            return parseGitHubRelease(response);
        } finally {
            conn.disconnect();
        }
    }

    private String getGitHubToken() {
        // Try environment variable first
        String token = System.getenv("GITHUB_TOKEN");
        if (token != null && !token.isEmpty()) {
            return token;
        }
        
        // Try to get from gh CLI (if available)
        try {
            Process process = new ProcessBuilder("gh", "auth", "token").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();
            if (line != null && !line.isEmpty()) {
                return line;
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not get GitHub token from gh CLI", e);
        }
        
        return null;
    }

    private UpdateInfo parseGitHubRelease(String jsonResponse) {
        UpdateInfo info = new UpdateInfo();
        
        try {
            // Parse GitHub release format
            String tagName = extractJsonValue(jsonResponse, "tag_name");
            if (tagName == null) {
                return null;
            }
            
            // Remove 'v' prefix if present
            info.version = tagName.startsWith("v") ? tagName.substring(1) : tagName;
            
            // Get the release name/notes
            info.releaseNotes = extractJsonValue(jsonResponse, "body");
            
            // Find the assets array and extract the first asset's download URL
            int assetsStart = jsonResponse.indexOf("\"assets\"");
            if (assetsStart > 0) {
                int arrayStart = jsonResponse.indexOf("[", assetsStart);
                if (arrayStart > 0) {
                    // Find the first asset object {
                    int firstAssetStart = jsonResponse.indexOf("{", arrayStart);
                    if (firstAssetStart > 0) {
                        // Use brace balancing to find the actual end of the asset object
                        int braceCount = 0;
                        int assetEnd = -1;
                        for (int i = firstAssetStart; i < jsonResponse.length(); i++) {
                            char c = jsonResponse.charAt(i);
                            if (c == '{') braceCount++;
                            if (c == '}') {
                                braceCount--;
                                if (braceCount == 0) {
                                    assetEnd = i;
                                    break;
                                }
                            }
                        }
                        
                        if (assetEnd > firstAssetStart) {
                            String assetSection = jsonResponse.substring(firstAssetStart, assetEnd + 1);
                            info.downloadUrl = extractJsonValue(assetSection, "browser_download_url");
                        }
                    }
                }
            }
            
            return (info.version != null && info.downloadUrl != null) ? info : null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse GitHub release: " + e.getMessage(), e);
            return null;
        }
    }

    private void downloadUpdate(UpdateInfo updateInfo) {
        executor.execute(() -> {
            try {
                Path downloadPath = updateDir.resolve("butterfly-" + updateInfo.version + ".jar");

                System.out.println("[UPDATE] Downloading to: " + downloadPath);
                if (listener != null) {
                    listener.onDownloadStarted(updateInfo);
                }

                downloadFile(updateInfo.downloadUrl, downloadPath);

                if (listener != null) {
                    listener.onDownloadComplete(updateInfo, downloadPath);
                }

                System.out.println("[UPDATE] Download complete: " + downloadPath);
                LOGGER.info("Update downloaded to: " + downloadPath);
            } catch (Exception e) {
                System.err.println("[UPDATE] Failed to download update: " + e.getMessage());
                e.printStackTrace();
                LOGGER.log(Level.WARNING, "Failed to download update", e);
                if (listener != null) {
                    listener.onDownloadFailed(e);
                }
            }
        });
    }

    private void downloadFile(String urlString, Path targetPath) throws Exception {
        URL url = new URL(urlString);
        System.out.println("[UPDATE] Connecting to: " + urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(SOCKET_TIMEOUT);

        try {
            if (conn.getResponseCode() != 200) {
                throw new IOException("Download failed: " + conn.getResponseCode());
            }

            long totalSize = conn.getContentLengthLong();
            System.out.println("[UPDATE] File size: " + (totalSize > 0 ? totalSize / 1024 + " KB" : "unknown"));
            
            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(targetPath)) {

                byte[] buffer = new byte[8192];
                long downloaded = 0;
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;

                    if (listener != null && totalSize > 0) {
                        int progress = (int) ((downloaded * 100) / totalSize);
                        listener.onDownloadProgress(progress);
                        System.out.println("[UPDATE] Progress: " + progress + "%");
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
    }

    public void installUpdate(UpdateInfo updateInfo, Path jarPath) throws Exception {
        Path appJar = getApplicationJar();
        Path pendingJar = appJar.resolveSibling(appJar.getFileName() + ".pending");
        
        System.out.println("[UPDATE] Install stage 1: Moving downloaded JAR to .pending");
        System.out.println("[UPDATE]   From: " + jarPath.toAbsolutePath());
        System.out.println("[UPDATE]   To:   " + pendingJar.toAbsolutePath());
        
        // Don't replace the current JAR (it's locked). Instead, stage it as .pending
        // The replacement will happen on next startup when the JVM has released the lock
        Files.move(jarPath, pendingJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("[UPDATE] Install stage 1 complete! .pending file ready for next startup");
        LOGGER.info("Update staged for installation on next startup: " + pendingJar);
        
        if (listener != null) {
            listener.onInstallComplete(updateInfo);
        }
    }

    public void restartApplication() throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = Paths.get(javaHome, "bin", "java").toString();
        String appJar = getApplicationJar().toString();
        String[] cmd = {javaBin, "-jar", appJar};

        new ProcessBuilder(cmd).start();
        System.exit(0);
    }

    private Path getApplicationJar() throws Exception {
        String classPath = UpdateChecker.class.getProtectionDomain()
            .getCodeSource().getLocation().toURI().getPath();
        
        // On Windows, URI.getPath() returns /C:/path/to/file, remove leading slash
        if (classPath.startsWith("/") && classPath.length() > 2 && classPath.charAt(2) == ':') {
            classPath = classPath.substring(1);
        }
        
        Path result = Paths.get(classPath);
        System.out.println("[UPDATE] Application JAR location: " + result.toAbsolutePath());
        return result;
    }

    private boolean isNewerVersion(String newVersion, String currentVersion) {
        try {
            String[] newParts = newVersion.split("\\.");
            String[] curParts = currentVersion.split("\\.");

            for (int i = 0; i < Math.max(newParts.length, curParts.length); i++) {
                int newNum = i < newParts.length ? Integer.parseInt(newParts[i]) : 0;
                int curNum = i < curParts.length ? Integer.parseInt(curParts[i]) : 0;

                if (newNum > curNum) return true;
                if (newNum < curNum) return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error comparing versions", e);
        }
        return false;
    }

    private void ensureUpdateDirExists() {
        try {
            Files.createDirectories(updateDir);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to create update directory", e);
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            // Fallback for numeric values or null
            pattern = "\"" + key + "\"\\s*:\\s*([^,}]*)";
            try {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(json);
                if (m.find()) {
                    String value = m.group(1).trim();
                    if (value.startsWith("\"")) {
                        return value.substring(1, value.length() - 1);
                    }
                    return value;
                }
            } catch (Exception e2) {
                // Ignore
            }
        }
        return null;
    }

    public void shutdown() {
        executor.shutdown();
    }

    public interface UpdateListener {
        void onUpdateAvailable(UpdateInfo updateInfo);
        void onNoUpdateAvailable();
        void onCheckFailed(Exception e);
        void onDownloadStarted(UpdateInfo updateInfo);
        void onDownloadProgress(int percentComplete);
        void onDownloadComplete(UpdateInfo updateInfo, Path jarPath);
        void onDownloadFailed(Exception e);
        void onInstallComplete(UpdateInfo updateInfo);
    }
}
