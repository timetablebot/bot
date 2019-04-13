package de.lukweb.timetablebot.telegram.commands;

import de.lukweb.timetablebot.setup.SetupStore;
import de.lukweb.timetablebot.sql.DB;
import de.lukweb.timetablebot.sql.UsersSQL;
import de.lukweb.timetablebot.telegram.MessageCallback;
import de.lukweb.timetablebot.telegram.TelegramBot;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.Users;
import org.telegram.telegrambots.meta.api.objects.Message;

public class DeleteEverythingC extends TelegramCommand {

    public DeleteEverythingC() {
        super("deletedata", "Löscht deine gesamten Daten");

        withoutVerification();
        hideFromShortHelp();
    }

    @Override
    protected void run(Message message, TelegramUser user) {
        user.messages().send("Wenn du deine *gesamten Daten löschen* möchtest, " +
                "antworte auf diese Nachricht mit 'Löschen'!");

        TelegramBot.get().setCallback(user.getChatid(), callMsg -> {
            if (!callMsg.hasText()) {
                return MessageCallback.RESUME;
            }
            if (!callMsg.getText().trim().equalsIgnoreCase("löschen")) {
                user.messages().send("Der Löschvorgang wurde abgebrochen!");
                return MessageCallback.FINISHED;
            }

            scheduleTask(() -> DB.get().useHandle(handle -> new UsersSQL(handle).delete(user)));
            SetupStore.store().abort(user);
            Users.remove(user);

            user.messages().send("Alle deine Daten wurden gelöscht.\n" +
                    "Um wieder zu starten, schreibe einfach eine Nachricht!");
            return MessageCallback.FINISHED;
        });
    }
}
