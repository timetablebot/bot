package de.lukweb.timetablebot.telegram.communication;

import de.lukweb.timetablebot.telegram.TelegramBot;
import de.lukweb.timetablebot.telegram.TelegramRank;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.Users;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageUserC extends TelegramCommand {

    public MessageUserC() {
        super("msguser", "Benutzer schreiben", "Nachricht an einzlenen Benutzer schreiben");

        requireRank(TelegramRank.ADMIN);
    }

    @Override
    protected void run(Message message, TelegramUser user) {
        String msgText = message.getText();
        long chatIdArg = findChatIdArgument(msgText);

        if (chatIdArg >= 0) {
            wantMessage(user, chatIdArg);
        } else {
            wantChatId(user);
        }
    }

    private long findChatIdArgument(String message) {
        String[] msgSplit = message.split(" ");

        if (!message.startsWith("/" + getCommand())) {
            return -1;
        }

        if (msgSplit.length < 2) {
            return -1;
        }

        String argChatId = msgSplit[1];
        if (argChatId.trim().isEmpty()) {
            return -1;
        }

        try {
            return Long.parseLong(argChatId);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void wantChatId(TelegramUser user) {
        user.messages().send("An wen soll die Nachricht gehen (ChatId) ?");
        TelegramBot.get().setCallback(user.getChatid(), msgCallback -> {
            try {
                int chatId = Integer.parseInt(msgCallback.getText());
                if (chatId >= 0) {
                    wantMessage(user, chatId);
                    // Allowing to change the callback
                    return false;
                } else {
                    user.messages().send("Die Zahl muss größer gleich 0 sein!");
                }
            } catch (NumberFormatException ex) {
                user.messages().send("Du musst eine ganze Zahl eingeben!");
            }
            return true;
        });
    }

    public void wantMessage(TelegramUser user, long chatid) {
        user.messages().send("Die Nachricht:");
        TelegramBot.get().setCallback(user.getChatid(), msgCallback -> {
            if (!msgCallback.hasText()) {
                user.messages().send("Die Nachricht muss Text enthalten!");
                return true;
            }

            String toName = sendMessageTo(chatid, msgCallback.getText());
            if (toName == null) {
                user.messages().send("Der Benutzer wurde nicht gefunden!");
                return true;
            }

            user.messages().send("Die Nachricht wurde an " + toName + " versendet!");
            return true;
        });
    }

    private String sendMessageTo(long chatid, String message) {
        TelegramUser user = Users.get(chatid);
        if (user == null) {
            return null;
        }

        user.messages().send(message);
        return user.getTelegramName();
    }


}
