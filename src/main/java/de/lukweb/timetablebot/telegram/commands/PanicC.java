package de.lukweb.timetablebot.telegram.commands;

import de.lukweb.timetablebot.telegram.TelegramBot;
import de.lukweb.timetablebot.telegram.TelegramRank;
import de.lukweb.timetablebot.telegram.TelegramUser;
import org.telegram.telegrambots.meta.api.objects.Message;

public class PanicC extends TelegramCommand {

    public PanicC() {
        super("panicshutdown", null, "Gute Nacht!");
        requireRank(TelegramRank.ADMIN);
    }

    @Override
    public void run(Message message, TelegramUser user) {
        user.messages().send("Going down for real.");

        new Thread(() -> {
            try {
                Thread.sleep(1000 * 60 * 2);
            } catch (InterruptedException ignored) {
            }
            System.exit(5);
        }).start();

        TelegramBot.get().stop();
        System.exit(5);
    }
}
