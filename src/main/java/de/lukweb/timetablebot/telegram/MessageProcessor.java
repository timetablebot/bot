package de.lukweb.timetablebot.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.Serializable;
import java.util.Collections;

public class MessageProcessor {

    private TelegramUser user;
    private TelegramBot bot;
    private Logger logger;

    public MessageProcessor(TelegramUser user) {
        this.user = user;
        this.bot = TelegramBot.get();
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public Message send(String message) {
        SendMessage sendMessage = new SendMessage()
                .setText(message)
                .setChatId(user.getChatid());
        return processMessage(sendMessage);
    }

    public Message sendNoPreview(String message) {
        SendMessage sendMessage = new SendMessage()
                .disableWebPagePreview()
                .setText(message)
                .setChatId(user.getChatid());
        return processMessage(sendMessage);
    }

    public Message keyboard(String message, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage()
                .setText(message)
                .disableWebPagePreview()
                .setChatId(user.getChatid())
                .setReplyMarkup(keyboard);
        return processMessage(sendMessage);
    }

    public void keyboardUpdate(int messageId, InlineKeyboardMarkup markup) {
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup()
                .setChatId(user.getChatid())
                .setMessageId(messageId)
                .setReplyMarkup(markup);
        processRequest(edit);
    }

    public Message inlineButton(String message, String buttonText, String callback) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setCallbackData(callback);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(Collections.singletonList(Collections.singletonList(button)));

        return keyboard(message, markup);
    }

    public boolean test(String message) {
        SendMessage sendMessage = new SendMessage()
                .setText(message)
                .setChatId(user.getChatid());
        return processMessage(sendMessage) != null;
    }

    private Message processMessage(SendMessage message) {
        message.enableMarkdown(true);
        try {
            return bot.execute(message);
        } catch (TelegramApiException ex) {
            handleProcessError(ex, message);
            return null;
        }
    }

    private boolean processRequest(BotApiMethod<? extends Serializable> method) {
        try {
            bot.execute(method);
        } catch (TelegramApiException ex) {
            handleProcessError(ex, method);
            return false;
        }
        return true;
    }

    private void handleProcessError(TelegramApiException ex, BotApiMethod<? extends Serializable> method) {
        /*
        References:
        - https://telegram.wiki/bots/bot-error-codes
        - https://stackoverflow.com/questions/35263618/how-can-i-detect-whether-a-user-deletes-the-telegram-bot-chat
         */
        if (ex instanceof TelegramApiRequestException) {
            TelegramApiRequestException requsetEx = (TelegramApiRequestException) ex;
            if (requsetEx.getErrorCode() == 403 && requsetEx.getApiResponse().contains("blocked")) {
                disableMessageToUser();
                return;
            }
        }

        if (method instanceof SendMessage) {
            SendMessage sendMessage = (SendMessage) method;

            if (sendMessage.getText().contains("Willkommen zurück!")) {
                disableMessageToUser();
                return;
            }

            logger.warn("Coudln't execute Telegram method: {}\nTo:{} ; Text:'{}'",
                    method.getMethod(), sendMessage.getChatId(), sendMessage.getText(), ex);
        } else {
            logger.warn("Coudln't execute Telegram method: {}", method.getMethod(), ex);
        }
    }

    private void disableMessageToUser() {
        if (user.isDisabled()) {
            return;
        }
        logger.warn("The bot was blocked by {}", user.getChatid());
        user.setDisabled(true);
        user.saveChanges();
    }
}
