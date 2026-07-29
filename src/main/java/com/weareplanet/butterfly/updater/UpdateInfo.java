package com.weareplanet.butterfly.updater;

/**
 * Contains metadata about an available update.
 */
public class UpdateInfo {
    public String version;
    public String releaseNotes;
    public String downloadUrl;
    public long fileSize;
    public String sha256Hash;
    public boolean mandatory;

    public UpdateInfo() {
    }

    public UpdateInfo(String version, String downloadUrl) {
        this.version = version;
        this.downloadUrl = downloadUrl;
        this.mandatory = false;
    }

    /**
     * Parse JSON response from update server.
     * Expected format:
     * {
     *   "version": "1.2.0",
     *   "releaseNotes": "Bug fixes and improvements",
     *   "downloadUrl": "https://...",
     *   "fileSize": 12345678,
     *   "sha256Hash": "abc123...",
     *   "mandatory": false
     * }
     */
    public static UpdateInfo parse(String jsonResponse) {
        UpdateInfo info = new UpdateInfo();

        try {
            info.version = extractJsonValue(jsonResponse, "version");
            info.downloadUrl = extractJsonValue(jsonResponse, "downloadUrl");
            info.releaseNotes = extractJsonValue(jsonResponse, "releaseNotes");
            info.sha256Hash = extractJsonValue(jsonResponse, "sha256Hash");

            String fileSizeStr = extractJsonValue(jsonResponse, "fileSize");
            if (fileSizeStr != null && !fileSizeStr.isEmpty()) {
                info.fileSize = Long.parseLong(fileSizeStr);
            }

            String mandatoryStr = extractJsonValue(jsonResponse, "mandatory");
            info.mandatory = "true".equalsIgnoreCase(mandatoryStr);

        } catch (Exception e) {
            System.err.println("Failed to parse update info: " + e.getMessage());
            return null;
        }

        return info.version != null ? info : null;
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

    @Override
    public String toString() {
        return "UpdateInfo{" +
                "version='" + version + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", fileSize=" + fileSize +
                ", mandatory=" + mandatory +
                '}';
    }
}
