package org.jivesoftware.openfire.plugin.spiritguide;

public class SchedulerBot implements Bot {


    public BotMessage getResponseMessage(String message) {
        if(message.contains("schedule")) {
            return new BotMessage("Lunch at 7 o'clock", "scheduler_bot");
        } else {
            return null;
        }
    }


}