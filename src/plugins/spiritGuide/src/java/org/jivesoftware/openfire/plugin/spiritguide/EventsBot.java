package org.jivesoftware.openfire.plugin.spiritguide;

public class EventsBot implements Bot {

	private static String welcomeText = "Welcome to our greatest hotel!";

    public BotMessage getResponseMessage(String message) {
        if(message.contains("sauna")) {
            return new BotMessage("#/sauna.html", "events_bot");
        } if(message.contains("dinner")) {
            return new BotMessage("#/dinner.html", "events_bot");
        } else {
            return null;
        }
    }


}