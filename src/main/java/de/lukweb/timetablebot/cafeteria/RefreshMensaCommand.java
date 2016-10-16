package de.lukweb.timetablebot.cafeteria;

import de.lukweb.timetablebot.telegram.TelegramRank;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import org.telegram.telegrambots.meta.api.objects.Message;

public class RefreshMensaCommand extends TelegramCommand {

    public RefreshMensaCommand() {
        super("refreshmensa", "Mensaplan aktualisieren", "Ruft den Mensaplan erneut aus der Datenbank ab");

        requireRank(TelegramRank.ADMIN);
    }

    @Override
    protected void run(Message message, TelegramUser user) {
        CafeteriaRunnable.refresh();
        user.messages().send("Mensaplan aktualisiert");
    }

}
