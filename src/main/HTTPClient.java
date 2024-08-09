package main;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class HTTPClient {
    private String serverUrl;
    private MemberManager memberManager;

    public HTTPClient(String serverUrl, MemberManager memberManager) {
        this.serverUrl = serverUrl;
        this.memberManager = memberManager;
    }

    public void sendMessageToServer(String message) {
        try {
            String encodedMessage = java.net.URLEncoder.encode(message, "UTF-8");
            URL url = new URL(serverUrl + "/send-message?message=" + encodedMessage);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.getResponseCode();
        } catch (IOException e) {
            System.out.println("An error occurred while sending the message: " + e.getMessage());
        }
    }

    public void sendUserToServer(String username, String world) {
        try {
            URL url = new URL(serverUrl + "/save-user");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = String.format("{\"UserID\": \"%s\", \"World\": \"%s\"}", username, world);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            connection.getResponseCode();
        } catch (IOException e) {
            System.out.println("An error occurred while sending user data: " + e.getMessage());
        }
    }

    public void sendSnapshotToServer() {
        try {
            URL url = new URL(serverUrl + "/save-snapshot");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            StringBuilder snapshotBuilder = new StringBuilder();
            snapshotBuilder.append("[");
            for (Map.Entry<String, String> entry : memberManager.getMembersMap().entrySet()) {
                snapshotBuilder.append(String.format("{\"UserID\": \"%s\", \"World\": \"%s\"},", entry.getKey(), entry.getValue()));
            }
            if (snapshotBuilder.length() > 1) {
                snapshotBuilder.setLength(snapshotBuilder.length() - 1); // Remove trailing comma
            }
            snapshotBuilder.append("]");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            String jsonInputString = String.format("{\"ID\": \"%s\", \"Timestamp\": \"%s\", \"Snapshot\": %s, \"NumberOnline\": %d}", 
                "SNAPSHOT_PARTITION", timestamp, snapshotBuilder.toString(), memberManager.getMembersMap().size());

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            connection.getResponseCode();
        } catch (IOException e) {
            System.out.println("An error occurred while sending snapshot data: " + e.getMessage());
        }
    }
}
