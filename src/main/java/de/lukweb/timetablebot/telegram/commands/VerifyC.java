package de.lukweb.timetablebot.telegram.commands;

import de.lukweb.timetablebot.config.TimetableBotConfig;
import de.lukweb.timetablebot.telegram.MessageCallback;
import de.lukweb.timetablebot.telegram.TelegramBot;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.utils.NetUtils;
import org.telegram.telegrambots.meta.api.objects.Message;

public class VerifyC extends TelegramCommand {

    private TimetableBotConfig config;

    public VerifyC(TimetableBotConfig config) {
        super("verify", "Verifizere dich");
        withoutVerification();

        this.config = config;
    }

    @Override
    public void run(Message message, TelegramUser user) {
        if (user.isVerified()) {
            user.messages().send("Du bist bereits verifiziert!");
            return;
        }
        // Maybe shorten more
        user.messages().send("Du musst dich verifizieren. Gebe dafür die Login-Daten " +
                "des Stundenplans, wie im Internet an.  " +
                "*Nutzername*:");

        final String[] datas = {null, null}; // username, password

        TelegramBot.get().setCallback(message.getChatId(), newMessage -> {
            if (!newMessage.hasText()) return MessageCallback.RESUME;
            if (datas[0] == null || datas[0].trim().isEmpty()) {
                datas[0] = newMessage.getText();
                user.messages().send("Gebe nun das *Passwort* ein:");
                return MessageCallback.RESUME;
            } else {
                datas[1] = newMessage.getText();
                scheduleTask(() -> checkData(user, datas));
                return MessageCallback.FINISHED;
            }
        });
    }

    private void checkData(TelegramUser user, String[] datas) {
        boolean auth = NetUtils.checkAuthentication(config.getTableBasePath() + "heute/subst_001.htm", datas[0], datas[1]);
        if (auth) {
            user.setVerified(true);
            user.saveChanges();
            user.messages().inlineButton(
                    "✔ Du bist nun verifiziert! Wähle jetzt deine Klasse und Lehrer aus.",
                    "Klasse / Lehrer auswählen", "Command_grade"
            );
        } else {
            user.messages().send("✖ Die Login-Daten stimmen leider nicht!");
        }
    }
}
