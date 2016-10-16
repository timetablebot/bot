package de.lukweb.timetablebot.setup;

import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.callbacks.CallbackQueryHandler;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public class TeachersCallback implements CallbackQueryHandler {

    @Override
    public String getMatchingPrefix() {
        return "teacher_";
    }

    @Override
    public AnswerCallbackQuery execute(CallbackQuery query, TelegramUser user) {
        String data = query.getData().replace("Teacher_", "");

        if (data.equals("Do_Finish")) {
            SetupStore.store().handle(user, "Fertig");
        } else {
            SetupStore.store().handle(user, data);
        }

        return null;
    }
}
