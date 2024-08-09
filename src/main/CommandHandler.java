package main;

import org.dreambot.api.wrappers.widgets.message.Message;

public class CommandHandler {
    private ClanChatListener script;

    public CommandHandler(ClanChatListener script) {
        this.script = script;
    }

    public void handleCommands(Message message) {
        switch (message.getMessage().toLowerCase()) {
            case "!discord":
            case "!link":
                script.sendMessageToChat("Discord link is https://shorter.gg/agilityfc");
                break;
        }
    }
}
