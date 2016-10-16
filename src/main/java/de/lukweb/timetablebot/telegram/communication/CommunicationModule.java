package de.lukweb.timetablebot.telegram.communication;

import de.lukweb.timetablebot.BotModule;
import de.lukweb.timetablebot.telegram.callbacks.CallbackQueryHandler;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommunicationModule extends BotModule {

    public CommunicationModule() {
        name = "communication";
    }

    @Override
    public List<TelegramCommand> commands() {
        return Arrays.asList(
                new BroadcastC(),
                new MessageUserC(),
                new NewSchoolYearC()
        );
    }

    @Override
    public List<CallbackQueryHandler> queryHandlers() {
        return Collections.singletonList(
                new MessageUserCallback()
        );
    }
}
