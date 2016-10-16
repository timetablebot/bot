package de.lukweb.timetablebot.timetable.cmd;

import de.lukweb.timetablebot.sql.DB;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import de.lukweb.timetablebot.timetable.sql.AmendmentSQL;
import de.lukweb.timetablebot.telegram.TelegramUser;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public class AbbreviationsC extends TelegramCommand {

    public AbbreviationsC() {
        // Zeigt dir alle gespeicherten Lehrerkürzel an!
        super("tokens", null, "Lehrerkürzel anzeigen");
    }

    @Override
    public void run(Message message, TelegramUser user) {
        List<String> abbreviations = DB.get().withHandle(handle -> new AmendmentSQL(handle).getTeachers());
        user.messages().send("Gespeicherte Lehrerkürzel:\n" + String.join(", ", abbreviations));
    }
}
