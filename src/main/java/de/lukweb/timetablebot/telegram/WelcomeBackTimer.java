package de.lukweb.timetablebot.telegram;

import de.lukweb.timetablebot.utils.CatchingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WelcomeBackTimer {

    private static ScheduledExecutorService executorService;

    public static void start() {
        executorService = Executors.newScheduledThreadPool(0, new CatchingThreadFactory("welcomeBack"));
        executorService.scheduleWithFixedDelay(new WelcomeBackRunnable(), 1, 30, TimeUnit.MINUTES);
    }

    public static void stop() {
        executorService.shutdown();
        try {
            boolean terminated = executorService.awaitTermination(15, TimeUnit.SECONDS);
            if (!terminated) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }
    }

    public static class WelcomeBackRunnable implements Runnable {

        private Logger logger;

        public WelcomeBackRunnable() {
            logger = LoggerFactory.getLogger(getClass());
        }

        @Override
        public void run() {
            logger.info("Welcome Back Check");
            Users.getAll()
                    .stream()
                    .filter(TelegramUser::isDisabled)
                    .forEach(this::sendTestMessage);
        }

        private void sendTestMessage(TelegramUser user) {
            boolean result = user.messages().test("Willkommen zurück! Hilfe gibt's mit /help");

            if (!result) {
                return;
            }

            user.setDisabled(false);
            user.saveChanges();
        }

    }

}
