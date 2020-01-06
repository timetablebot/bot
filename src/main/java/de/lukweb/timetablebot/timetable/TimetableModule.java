package de.lukweb.timetablebot.timetable;

import de.lukweb.timetablebot.BotModule;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import de.lukweb.timetablebot.timetable.cmd.AbbreviationsC;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimetableModule extends BotModule {

    public TimetableModule() {
        name = "timetable";
        setSchedulerPeriod(5, TimeUnit.MINUTES);

        if (!getConfig().isInstantRun()) {
            setSchedulerDelay(delayForFiveMinutes(), TimeUnit.MILLISECONDS);
        }
    }

    private long delayForFiveMinutes() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 5;
        calendar.add(Calendar.MINUTE, mod == 0 ? 5 : 5 - mod);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }

    @Override
    public List<Runnable> tasks() {
        return Collections.singletonList(
                new TimetableRunnable(getConfig())
        );
    }

    @Override
    public List<TelegramCommand> commands() {
        return Collections.singletonList(
                new AbbreviationsC()
        );
    }
}
