package de.lukweb.timetablebot.feedback;

import de.lukweb.timetablebot.BotModule;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;

import java.util.Collections;
import java.util.List;

public class FeedbackModule extends BotModule {

    public FeedbackModule() {
        name = "feedback";
    }

    @Override
    public List<TelegramCommand> commands() {
        return Collections.singletonList(
                new FeedbackC()
        );
    }
}
