package main;

import okhttp3.*;
import com.google.gson.*;

import java.io.IOException;

public class OpenAIClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "api-key-here";

    public static String processMessage(String message) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String prompt = "Respond with only true false to this statement:\n"
                + "Is the following message asking what the discord is or where it can be found:\n"
                + "\"" + message + "\"";

        JsonObject json = new JsonObject();
        JsonArray messages = new JsonArray();
        JsonObject messageObject = new JsonObject();
        messageObject.addProperty("role", "user");
        messageObject.addProperty("content", prompt);
        messages.add(messageObject);
        
        json.add("messages", messages);
        json.addProperty("model", "gpt-3.5-turbo");
        json.addProperty("max_tokens", 5);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                json.toString()
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JsonObject responseJson = JsonParser.parseString(response.body().string()).getAsJsonObject();
            return responseJson.get("choices").getAsJsonArray().get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString().trim();
        }
    }
}