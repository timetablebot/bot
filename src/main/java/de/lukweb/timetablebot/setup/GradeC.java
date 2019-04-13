package de.lukweb.timetablebot.setup;

import de.lukweb.timetablebot.telegram.MessageCallback;
import de.lukweb.timetablebot.telegram.TelegramBot;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import org.telegram.telegrambots.meta.api.objects.Message;

public class GradeC extends TelegramCommand {

    public GradeC() {
        super("grade", "Klasse / Lehrer ändern");
    }

    @Override
    public void run(Message message, TelegramUser user) {
        SetupStore store = SetupStore.store();

        // Just pass a string, because for the first run is no text needed
        store.handleNew(user, "");

        TelegramBot.get().setCallback(message.getChatId(), newMsg -> {
            if (!newMsg.hasText()) return MessageCallback.RESUME;

            SetupState state = store.handle(user, newMsg.getText());
            return state.equals(SetupState.FINISH) || state.equals(SetupState.INVAILD);
        });
    }

}
