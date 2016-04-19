package org.jivesoftware.openfire.plugin.spiritguide;

public class BotMessage {
    private String message;
    private String botName;

    public BotMessage(String message, String botName) {
        this.message = message;
        this.botName = botName;
    }

    public String getMessage() {
        return message;
    }

    public String getBotName() {
        return botName;
    }

    @Override
    public String toString() {
        return "BotMessage{" +
                "message='" + message + '\'' +
                ", botName='" + botName + '\'' +
                '}';
    }
}
