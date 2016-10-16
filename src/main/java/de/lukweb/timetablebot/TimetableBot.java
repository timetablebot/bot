package de.lukweb.timetablebot;

import de.lukweb.timetablebot.cafeteria.CafeteriaModule;
import de.lukweb.timetablebot.config.TimetableBotConfig;
import de.lukweb.timetablebot.feedback.FeedbackModule;
import de.lukweb.timetablebot.news.NewsModule;
import de.lukweb.timetablebot.setup.SetupModule;
import de.lukweb.timetablebot.sql.DB;
import de.lukweb.timetablebot.telegram.TelegramBot;
import de.lukweb.timetablebot.telegram.TelegramModule;
import de.lukweb.timetablebot.telegram.communication.CommunicationModule;
import de.lukweb.timetablebot.timetable.TimetableModule;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;

public class TimetableBot {

    private static TelegramBot telegramBot;
    private static TimetableBotConfig config;
    private static ModuleLoader moduleLoader;

    public static void main(String[] args) {
        ApiContextInitializer.init();

        config = new TimetableBotConfig();
        DB.init(config);

        moduleLoader = new ModuleLoader(new BotModule[]{
                new CafeteriaModule(),
                new CommunicationModule(),
                new FeedbackModule(),
                new NewsModule(),
                new SetupModule(),
                new TelegramModule(),
                new TimetableModule(),
        });

        moduleLoader.load();
        telegramBot = new TelegramBot(moduleLoader, config);

        moduleLoader.startTasks();
        Runtime.getRuntime().addShutdownHook(new Thread(TimetableBot::stop));

        System.gc();
        LoggerFactory.getLogger(TimetableBot.class).info("Up and running!");
    }

    public static void stop() {
        LoggerFactory.getLogger(TimetableBot.class).info("Stopping...");
        moduleLoader.stopTasks();
    }

    public static ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    public static TelegramBot getTBot() {
        return telegramBot;
    }

    public static TimetableBotConfig getConfig() {
        return config;
    }
}
