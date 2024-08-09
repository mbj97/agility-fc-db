package main;

import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MemberManager {
    private Map<String, String> membersMap = new HashMap<>();
    private HTTPClient httpClient;

    public MemberManager() {
    }

    public void setHttpClient(HTTPClient httpClient) {
        this.httpClient = httpClient;
    }

    public void populateInitialMembersList() {
        WidgetChild widget = Widgets.getWidget(7).getChild(12);
        if (widget == null) {
            System.out.println("Widget 7 Child 12 is null");
            return;
        }
        WidgetChild[] members = widget.getChildren();
        if (members == null) {
            System.out.println("Members array is null");
            return;
        }

        for (int i = 0; i < members.length; i += 3) {
            if (members[i].getText() != null && !members[i].getText().isEmpty()) {
                String name = members[i].getText();
                String world = members[i + 1].getText();
                membersMap.put(name, world);
                System.out.println(name + " - " + world);
                httpClient.sendUserToServer(name, world);
            }
        }
        httpClient.sendSnapshotToServer();
    }

    public void updateMembersList() {
        WidgetChild widget = Widgets.getWidget(7).getChild(12);
        if (widget == null) {
            System.out.println("Widget 7 Child 12 is null");
            return;
        }
        WidgetChild[] members = widget.getChildren();
        if (members == null) {
            System.out.println("Members array is null");
            return;
        }

        Set<String> currentMembers = new HashSet<>();
        for (int i = 0; i < members.length; i += 3) {
            if (members[i].getText() != null && !members[i].getText().isEmpty()) {
                String name = members[i].getText();
                String world = members[i + 1].getText();
                currentMembers.add(name);

                if (!membersMap.containsKey(name)) {
                    // New member joined
                    String joinMessage = "Member joined: " + name + " (" + world + ")";
                    System.out.println(joinMessage);
                    httpClient.sendMessageToServer(joinMessage);
                    httpClient.sendUserToServer(name, world);
                }

                membersMap.put(name, world);
            }
        }

        Set<String> membersToRemove = new HashSet<>();
        for (String name : membersMap.keySet()) {
            if (!currentMembers.contains(name)) {
                // Member left
                String leaveMessage = "Member left: " + name;
                System.out.println(leaveMessage);
                httpClient.sendMessageToServer(leaveMessage);
                membersToRemove.add(name);
            }
        }

        for (String name : membersToRemove) {
            membersMap.remove(name);
        }
    }

    public String handleChatMessage(Message message) {
        // Remove anything within angle brackets in the username
        String rawUsername = message.getUsername();
        String username = rawUsername.replaceAll("<.*?>", "");

        String world = membersMap.getOrDefault(username, "NA");
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return timeStamp + " - " + username + " (" + world + "): " + message.getMessage();
    }

    public Map<String, String> getMembersMap() {
        return membersMap;
    }
}
