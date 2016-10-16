package de.lukweb.timetablebot.cafeteria;

import de.lukweb.timetablebot.BotModule;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CafeteriaModule extends BotModule {

    public CafeteriaModule() {
        name = "cafeteria";
        setSchedulerPeriod(2, TimeUnit.HOURS);
    }

    @Override
    public List<Runnable> tasks() {
        return Collections.singletonList(
                new CafeteriaRunnable()
        );
    }

    @Override
    public List<TelegramCommand> commands() {
        return Arrays.asList(
                new MensaCommand(),
                new RefreshMensaCommand()
        );
    }


}
