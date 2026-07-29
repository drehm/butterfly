import java.net.URL;
import java.net.HttpURLConnection;
import java.io.*;

public class GithubParseTest {
    public static void main(String[] args) throws Exception {
        String token = args.length > 0 ? args[0] : null;
        
        URL url = new URL("https://api.github.com/repos/drehm/butterfly/releases/latest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        
        String json = sb.toString();
        
        // Test tag_name extraction
        String tagName = extractJsonValue(json, "tag_name");
        System.out.println("Extracted tag_name: " + tagName);
        
        if (tagName != null) {
            String version = tagName.startsWith("v") ? tagName.substring(1) : tagName;
            System.out.println("Version after removing 'v': " + version);
        }
        
        // Test assets extraction - use better logic
        int assetsStart = json.indexOf("\"assets\"");
        System.out.println("\"assets\" found at index: " + assetsStart);
        
        if (assetsStart > 0) {
            int arrayStart = json.indexOf("[", assetsStart);
            System.out.println("Array start '[' at: " + arrayStart);
            
            if (arrayStart > 0) {
                int firstAssetStart = json.indexOf("{", arrayStart);
                System.out.println("First asset '{' at: " + firstAssetStart);
                
                if (firstAssetStart > 0) {
                    // Search for a } that's far enough to include the whole asset
                    int searchEnd = json.indexOf("}", firstAssetStart);
                    while (searchEnd > 0 && (searchEnd - firstAssetStart) < 500) {
                        searchEnd = json.indexOf("}", searchEnd + 1);
                    }
                    
                    System.out.println("Found closing '}' at: " + searchEnd + " (distance: " + (searchEnd - firstAssetStart) + ")");
                    
                    if (searchEnd > firstAssetStart) {
                        String assetSection = json.substring(firstAssetStart, searchEnd + 1);
                        System.out.println("Asset section (first 300 chars): " + assetSection.substring(0, Math.min(300, assetSection.length())));
                        
                        String downloadUrl = extractJsonValue(assetSection, "browser_download_url");
                        System.out.println("Extracted download URL: " + downloadUrl);
                    }
                }
            }
        }
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
                e2.printStackTrace();
            }
        }
        return null;
    }
}
