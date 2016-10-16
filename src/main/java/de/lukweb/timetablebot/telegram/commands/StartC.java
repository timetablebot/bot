package de.lukweb.timetablebot.telegram.commands;

import de.lukweb.timetablebot.telegram.TelegramUser;
import org.telegram.telegrambots.meta.api.objects.Message;

public class StartC extends TelegramCommand {

    public StartC() {
        super("start", null);
        withoutVerification();
    }

    @Override
    public void run(Message message, TelegramUser user) {
        if (user.isDisabled()) {
            user.setDisabled(false);
            user.saveChanges();
        }
        user.messages().send("Willkommen beim ALS Stundenplanbot!\n" +
                "Der Bot schreibt dir, wenn du Vertretung hast.\n" +
                "(Davor musst du ihn noch schnell einrichten.)\n" +
                "Wenn du Hilfe benötigtest schreibe /help.");
        if (!user.isVerified()) {
            user.messages().inlineButton("Du bist noch nicht verifiziert. \n" +
                    "Drücke unten auf den Knopf, um zu beginnen! ⬇️", "Verifizieren", "Command_Verify");
        }
    }
}
