package de.lukweb.timetablebot;

import de.lukweb.timetablebot.telegram.TelegramBot;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.Users;
import de.lukweb.timetablebot.telegram.callbacks.CallbackQueryHandler;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import de.lukweb.timetablebot.utils.CatchingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class ModuleLoader {

    protected Logger logger;
    private BotModule[] modules;
    private HashMap<String, TelegramCommand> commands;
    private List<TelegramCommand> orderedCommands;
    private HashMap<String, CallbackQueryHandler> callbackQueries;
    private HashMap<Class<? extends Runnable>, Runnable> runnables;
    private ScheduledExecutorService taskTerminator;

    public ModuleLoader(BotModule[] modules) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.modules = modules;
        this.runnables = new HashMap<>();
    }

    public void load() {
        commands = new HashMap<>();
        callbackQueries = new HashMap<>();
        orderedCommands = new ArrayList<>();
        for (BotModule module : modules) {

            module.setModuleLoader(this);

            for (TelegramCommand command : module.commands()) {
                commands.put(command.getCommand().toLowerCase(), command);
            }
            for (CallbackQueryHandler handler : module.queryHandlers()) {
                callbackQueries.put(handler.getMatchingPrefix().toLowerCase(), handler);
            }

        }

        orderedCommands = commands.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public TelegramCommand getCommand(String command) {
        if (command.startsWith("/")) command = command.substring(1);
        return commands.get(command.toLowerCase());
    }

    public List<TelegramCommand> getCommands() {
        return orderedCommands;
    }

    public CallbackQueryHandler getQueryHandler(String queryText) {
        queryText = queryText.toLowerCase();
        for (Map.Entry<String, CallbackQueryHandler> entry : callbackQueries.entrySet()) {
            if (queryText.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    // https://core.telegram.org/bots/api#callbackquery
    public void handleCallbackQuery(CallbackQuery query) {
        // We aren't a inline bot
        if (query.getMessage() == null) {
            return;
        }

        TelegramUser user = Users.get(query.getMessage().getChatId());
        // We ignore the user, because a user can't send a callback query without a single message
        if (user == null) {
            return;
        }

        CallbackQueryHandler handler = getQueryHandler(query.getData());
        if (handler == null) {
            return;
        }

        // https://core.telegram.org/bots/api#answercallbackquery
        AnswerCallbackQuery answer = handler.execute(query, user);
        if (answer == null) {
            answer = new AnswerCallbackQuery().setText(null);
        }
        answer.setCallbackQueryId(query.getId());

        try {
            TelegramBot.get().execute(answer);
        } catch (TelegramApiException e) {
            logger.warn("Couldn't answer a callback query: {}", query.getData());
        }
    }

    public <T extends Runnable> T getRunnable(Class<T> tClass) {
        Runnable runnable = runnables.get(tClass);
        if (runnable == null) {
            return null;
        }

        try {
            return tClass.cast(runnable);
        } catch (ClassCastException ex) {
            logger.warn("Couldn't cast {} as an instance of {}: {}", runnable, tClass, ex);
            return null;
        }
    }

    public void startTasks() {
        // TODO: Maybe stop terminator
        taskTerminator = Executors.newScheduledThreadPool(2, new CatchingThreadFactory("task-terminator"));
        for (BotModule module : modules) {
            List<Runnable> moduleRunnables = module.startScheduler(taskTerminator);

            for (Runnable runnable : moduleRunnables) {
                runnables.put(runnable.getClass(), runnable);
            }
        }

    }

    public void stopTasks() {
        runnables.values().stream()
                .filter(runnable -> runnable instanceof Stoppable)
                .map(Stoppable.class::cast)
                .forEach(Stoppable::stop);
        runnables.clear();

        for (BotModule module : modules) {
            module.stopScheduler();
        }

        taskTerminator.shutdown();
        taskTerminator.shutdownNow();

        taskTerminator = null;
    }

}
