package de.lukweb.timetablebot.telegram.communication;

import de.lukweb.timetablebot.telegram.*;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import org.telegram.telegrambots.meta.api.objects.Message;

public class BroadcastC extends TelegramCommand {

    public BroadcastC() {
        super("broadcast", "Rundruf senden", "Sendet eine Informationsnachricht an alle Teilnehmer");
        requireRank(TelegramRank.ADMIN);
    }

    @Override
    public void run(Message message, TelegramUser user) {
        TelegramBot telegramBot = TelegramBot.get();
        user.messages().send("Sende nun den Broadcast!");
        telegramBot.setCallback(message.getChatId(), callback -> {
            if (!callback.hasText()) return MessageCallback.RESUME;
            String text = "Broadcast: " + callback.getText();

            boolean canSendMessage = user.messages().test(text);

            if (!canSendMessage) {
                user.messages().send("Die Nachricht ist fehlerhaft, bitte überprüfe die Formatierung.");
                return MessageCallback.RESUME;
            }

            Users.getAll()
                    .stream()
                    .filter(testUser -> testUser.getChatid() != user.getChatid())
                    .filter(testUser -> !testUser.isDisabled() && !testUser.isBlocked())
                    .forEach(target -> target.messages().send(text));
            return MessageCallback.FINISHED;
        });
    }

}
