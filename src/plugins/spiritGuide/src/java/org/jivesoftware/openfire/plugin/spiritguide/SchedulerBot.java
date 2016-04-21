package org.jivesoftware.openfire.plugin.spiritguide;

public class SchedulerBot implements Bot {


    public BotMessage getResponseMessage(String message) {
        if(message.contains("schedule")) {
            return new BotMessage("#schedule", "scheduler_bot");
        } else {
            return null;
        }
    }


}