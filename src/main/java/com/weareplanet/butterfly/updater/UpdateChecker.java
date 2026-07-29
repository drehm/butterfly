package com.weareplanet.butterfly.updater;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        this.updateDir = Paths.get("update");
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
                LOGGER.info("Update available: " + updateInfo.version);
                if (listener != null) {
                    listener.onUpdateAvailable(updateInfo);
                }
                downloadUpdate(updateInfo);
            } else {
                LOGGER.info("Already running latest version: " + currentVersion);
                if (listener != null) {
                    listener.onNoUpdateAvailable();
                }
            }
        } catch (Exception e) {
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

                if (listener != null) {
                    listener.onDownloadStarted(updateInfo);
                }

                downloadFile(updateInfo.downloadUrl, downloadPath);

                if (listener != null) {
                    listener.onDownloadComplete(updateInfo, downloadPath);
                }

                LOGGER.info("Update downloaded to: " + downloadPath);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to download update", e);
                if (listener != null) {
                    listener.onDownloadFailed(e);
                }
            }
        });
    }

    private void downloadFile(String urlString, Path targetPath) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(SOCKET_TIMEOUT);

        try {
            if (conn.getResponseCode() != 200) {
                throw new IOException("Download failed: " + conn.getResponseCode());
            }

            long totalSize = conn.getContentLengthLong();
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
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
    }

    public void installUpdate(UpdateInfo updateInfo, Path jarPath) throws Exception {
        Path appJar = getApplicationJar();
        Path backupJar = appJar.resolveSibling(appJar.getFileName() + ".backup");

        Files.move(appJar, backupJar);

        try {
            Files.move(jarPath, appJar);
            LOGGER.info("Update installed successfully");

            if (listener != null) {
                listener.onInstallComplete(updateInfo);
            }
        } catch (Exception e) {
            Files.move(backupJar, appJar);
            throw e;
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
        return Paths.get(classPath);
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
