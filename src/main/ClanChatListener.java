package main;

import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.event.impl.keyboard.awt.Key;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.wrappers.widgets.message.MessageType;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@ScriptManifest(author = "Tide", name = "ClanChatListener", version = 1.3, description = "Listens to clan chat and sends messages to a server, also saves Friends Chat members, and responds to commands", category = Category.UTILITY)
public class ClanChatListener extends AbstractScript implements ChatListener {

    private static final String SERVER_URL = "http://localhost:8080";
    private long lastF7PressTime = System.currentTimeMillis();
    private Timer snapshotTimer;
    private MemberManager memberManager;
    private HTTPClient httpClient;
    private CommandHandler commandHandler;

    @Override
    public void onStart() {
        log("Starting ClanChatListener script!");
        memberManager = new MemberManager();
        httpClient = new HTTPClient(SERVER_URL, memberManager);
        memberManager.setHttpClient(httpClient); // Ensure the HTTPClient is set in MemberManager
        commandHandler = new CommandHandler(this);

        memberManager.populateInitialMembersList();
        httpClient.sendSnapshotToServer();

        // Schedule a snapshot every 5 minutes
        snapshotTimer = new Timer();
        snapshotTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                httpClient.sendSnapshotToServer();
            }
        }, 0, 5 * 60 * 1000); // 5 minutes period
    }

    @Override
    public int onLoop() {
        long currentTime = System.currentTimeMillis();

        // Press Key.F7 every 1-2 minutes
        if (currentTime - lastF7PressTime >= Calculations.random(60000, 120000)) {
            Keyboard.typeKey(Key.F7);
            lastF7PressTime = currentTime;
        }

        memberManager.updateMembersList();

        return Calculations.random(1000, 2000);
    }

    @Override
    public void onMessage(Message message) {
        if (message.getType() == MessageType.CHAT_CHANNEL) {
            String logMessage = memberManager.handleChatMessage(message);
            log(logMessage);
            httpClient.sendMessageToServer(logMessage);

            if (message.getMessage().toLowerCase().contains("discord") && !message.getUsername().toLowerCase().equals("final hit184")) {
                try {
                    String openAIResponse = OpenAIClient.processMessage(message.getMessage());
                    log(openAIResponse);
                    if ("true".equalsIgnoreCase(openAIResponse)) {
                        sendMessageToChat("Discord link is https://shorter.gg/agilityfc");
                    }
                } catch (IOException e) {
                    log("Error processing message with OpenAI: " + e.getMessage());
                }
            }

            commandHandler.handleCommands(message);
        }
    }

    public void sendMessageToChat(String message) {
        new Thread(() -> {
            Keyboard.type(message);
            Keyboard.typeKey(Key.ENTER);
        }).start();
    }

    @Override
    public void onExit() {
        if (snapshotTimer != null) {
            snapshotTimer.cancel();
        }
        log("Stopping ClanChatListener script!");
    }
}
