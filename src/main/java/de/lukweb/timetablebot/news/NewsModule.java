package de.lukweb.timetablebot.news;

import de.lukweb.timetablebot.BotModule;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NewsModule extends BotModule {

    public NewsModule() {
        name = "news";
        setSchedulerPeriod(15, TimeUnit.MINUTES);
    }

    @Override
    public List<Runnable> tasks() {
        return Collections.singletonList(
                new NewsRunnable(getConfig())
        );
    }

    @Override
    public List<TelegramCommand> commands() {
        return Collections.singletonList(
                new NewsC()
        );
    }
}
