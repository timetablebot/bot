package de.lukweb.timetablebot.telegram.callbacks;

import de.lukweb.timetablebot.telegram.TelegramUser;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackQueryHandler {

    String getMatchingPrefix();

    AnswerCallbackQuery execute(CallbackQuery query, TelegramUser user);

}
