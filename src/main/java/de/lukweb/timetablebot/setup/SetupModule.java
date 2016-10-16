package de.lukweb.timetablebot.setup;

import de.lukweb.timetablebot.BotModule;
import de.lukweb.timetablebot.telegram.callbacks.CallbackQueryHandler;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;

import java.util.Collections;
import java.util.List;

public class SetupModule extends BotModule {

    public SetupModule() {
        name = "setup";
    }

    @Override
    public List<TelegramCommand> commands() {
        return Collections.singletonList(
                new GradeC()
        );
    }

    @Override
    public List<CallbackQueryHandler> queryHandlers() {
        return Collections.singletonList(
                new TeachersCallback()
        );
    }
}
