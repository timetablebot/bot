package de.lukweb.timetablebot.telegram;

import de.lukweb.timetablebot.ModuleLoader;
import de.lukweb.timetablebot.config.TimetableBotConfig;
import de.lukweb.timetablebot.sql.DB;
import de.lukweb.timetablebot.sql.UsersSQL;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.util.HashMap;

public class TelegramBot extends TelegramLongPollingBot {

    private static TelegramBot instance;

    public static TelegramBot get() {
        return instance;
    }

    private static DefaultBotOptions buildOptions() {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setMaxThreads(3);
        return options;
    }

    //////>

    private BotSession botSession;
    private ModuleLoader moduleLoader;
    private TimetableBotConfig config;
    private Logger logger;
    private Logger sentLogger;

    public TelegramBot(ModuleLoader moduleLoader, TimetableBotConfig config) {
        super(buildOptions());
        instance = this;

        this.logger = LoggerFactory.getLogger(getClass());
        this.sentLogger = LoggerFactory.getLogger("de.lukweb.timetablebot.telegram.MessagesSent");
        this.moduleLoader = moduleLoader;
        this.config = config;

        Users.load();
        try {
            start();
        } catch (TelegramApiRequestException e) {
            logger.error("Telegram API Exception", e);
            System.exit(1);
        }
    }

    private void start() throws TelegramApiRequestException {
        botSession = new TelegramBotsApi().registerBot(this);
    }

    public String getBotUsername() {
        return config.getBotName();
    }

    public String getBotToken() {
        return config.getBotToken();
    }

    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {

            CallbackQuery query = update.getCallbackQuery();
            moduleLoader.handleCallbackQuery(query);

        } else if (update.hasMessage()) {

            Message message = update.getMessage();

            if (callbacks.containsKey(message.getChatId())) {
                if (callbacks.get(message.getChatId()).execute(message)) setCallback(message.getChatId(), null);
                return;
            }

            if (!message.hasText()) return;

            String msgText = message.getText();
            TelegramUser user = Users.get(message.getChatId());

            if (user == null) {
                user = new TelegramUser(message.getChat());
                Users.add(user);
                TelegramUser finalUser = user;
                DB.get().useHandle(handle -> new UsersSQL(handle).insert(finalUser));
            } else {
                user.updateTelegramName(message.getChat());
            }

            // Allowing commands with arguments
            if (msgText.startsWith("/") && msgText.split(" ").length >= 2) {
                msgText = msgText.trim().split(" ")[0];
            }

            TelegramCommand command = moduleLoader.getCommand(msgText);
            if (command == null) {
                moduleLoader.getCommand("help").execute(message, user);
                return;
            }

            command.execute(message, user);
        }
    }

    private HashMap<Long, MessageCallback> callbacks = new HashMap<>();

    public void setCallback(long chat, MessageCallback callback) {
        if (callback == null) {
            callbacks.remove(chat);
            return;
        }
        callbacks.put(chat, callback);
    }

    public void stop() {
        if (botSession != null && botSession.isRunning()) {
            botSession.stop();
        }
    }
}
