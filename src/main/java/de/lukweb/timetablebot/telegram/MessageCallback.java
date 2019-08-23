package de.lukweb.timetablebot.telegram;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageCallback {

    /**
     * @param message the message object from Telegram
     * @return Wheather the interface wants more calls. True when finished, false if not!
     */
    boolean execute(Message message);

    boolean FINISHED = true;
    boolean RESUME = false;

}
