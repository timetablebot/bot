package de.lukweb.timetablebot.telegram.communication;

import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.callbacks.CallbackQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public class MessageUserCallback implements CallbackQueryHandler {

    private Logger logger;
    private MessageUserC command;

    public MessageUserCallback() {
        logger = LoggerFactory.getLogger(getClass());
        command = new MessageUserC();
    }

    @Override
    public String getMatchingPrefix() {
        return "msguser_";
    }

    @Override
    public AnswerCallbackQuery execute(CallbackQuery query, TelegramUser user) {
        String data = query.getData().toLowerCase().replace("msguser_", "");

        try {
            long chatid = Long.parseLong(data);
            command.wantMessage(user, chatid);
        } catch (NumberFormatException ex) {
            logger.warn("Can't parse argument of query: '{}'", query);
        }

        return null;
    }

}
