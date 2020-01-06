package de.lukweb.timetablebot;

import de.lukweb.timetablebot.config.TimetableBotConfig;
import de.lukweb.timetablebot.telegram.callbacks.CallbackQueryHandler;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import de.lukweb.timetablebot.utils.CatchingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BotModule {

    protected String name;
    protected Logger logger;
    private long schedulerPeriod;
    private long schedulerDelay;
    private int schedulerThreads;
    private ScheduledThreadPoolExecutor executor;

    public BotModule() {
        name = "";
        logger = LoggerFactory.getLogger(getClass());
        schedulerPeriod = TimeUnit.MINUTES.toMillis(30);
        schedulerDelay = 0;
        schedulerThreads = 1;
    }

    protected void setSchedulerPeriod(long period, TimeUnit unit) {
        schedulerPeriod = unit.toMillis(period);
    }

    protected void setSchedulerDelay(long delay, TimeUnit unit) {
        schedulerDelay = unit.toMillis(delay);
    }

    protected void setSchedulerThreads(int schedulerThreads) {
        this.schedulerThreads = schedulerThreads;
        if (this.executor != null) {
            this.executor.setMaximumPoolSize(schedulerThreads);
        }
    }

    public List<Runnable> startScheduler(ScheduledExecutorService taskTerminator) {
        List<Runnable> tasks = tasks();
        if (tasks.isEmpty()) {
            return tasks;
        }

        executor = new ScheduledThreadPoolExecutor(schedulerThreads, new CatchingThreadFactory(name));
        for (Runnable task : tasks) {

            Runnable wrappedTask = () -> {
                Future<?> future = executor.submit(task);
                taskTerminator.schedule(() -> {
                    if (future.isDone()) {
                        return;
                    }
                    logger.warn("Task of {} took longer than 5 minutes and was terminated!", task.getClass().getName());
                    future.cancel(true);
                }, 5, TimeUnit.MINUTES);
            };

            executor.scheduleAtFixedRate(wrappedTask, schedulerDelay, schedulerPeriod, TimeUnit.MILLISECONDS);
        }

        return tasks;
    }

    public void stopScheduler() {
        if (executor != null) {
            boolean termination = false;

            try {
                executor.shutdown();
                termination = executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {

            }

            if (termination) {
                executor.shutdownNow();
            }
        }
    }

    protected void setModuleLoader(ModuleLoader moduleLoader) {

    }

    protected TimetableBotConfig getConfig() {
        return TimetableBot.getConfig();
    }

    public List<Runnable> tasks() {
        return Collections.emptyList();
    }

    public List<TelegramCommand> commands() {
        return Collections.emptyList();
    }

    public List<CallbackQueryHandler> queryHandlers() {
        return Collections.emptyList();
    }

}
