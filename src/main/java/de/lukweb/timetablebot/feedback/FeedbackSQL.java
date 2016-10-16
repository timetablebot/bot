package de.lukweb.timetablebot.feedback;

import de.lukweb.timetablebot.telegram.TelegramUser;
import org.jdbi.v3.core.Handle;

import java.time.Instant;

public class FeedbackSQL {

    private Handle handle;

    public FeedbackSQL(Handle handle) {
        this.handle = handle;
    }

    public void add(TelegramUser from, String feedback) {
        long timestamp = Instant.now().getEpochSecond();
        handle.execute("INSERT INTO feedback (`from`, feedback, timestamp) VALUES (?, ?, ?)",
                from.getChatid(), feedback, timestamp);
    }

}
