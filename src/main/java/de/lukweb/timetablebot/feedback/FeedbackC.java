package de.lukweb.timetablebot.feedback;

import de.lukweb.timetablebot.sql.DB;
import de.lukweb.timetablebot.telegram.*;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import org.telegram.telegrambots.meta.api.objects.Message;

public class FeedbackC extends TelegramCommand {

    public FeedbackC() {
        super("feedback", "Feedback senden");
        withoutVerification();
    }

    @Override
    public void run(Message message, TelegramUser user) {
        user.messages().send("Sende nun dein Feedback!");
        TelegramBot.get().setCallback(message.getChatId(), callback -> {
            if (!callback.hasText()) return MessageCallback.RESUME;

            String feedbackText = callback.getText();

            DB.get().useHandle(handle -> new FeedbackSQL(handle).add(user, feedbackText));
            user.messages().send("Vielen Dank für dein Feedback!");

            scheduleTask(() -> informAdmins(user, feedbackText));

            return MessageCallback.FINISHED;
        });
    }

    private void informAdmins(TelegramUser sender, String feedback) {
        String feedbackMessage = String.format("Es wurde neues Feedback von %s erstellt:\n%s",
                sender.getTelegramName(), feedback);

        Users.getAll()
                .stream()
                .filter(user -> user.getRank().equals(TelegramRank.ADMIN))
                .forEach(user -> sendMessageTo(user, sender, feedbackMessage));
    }

    private void sendMessageTo(TelegramUser user, TelegramUser from, String message) {
        user.messages().inlineButton(message, "Antworten", "msguser_" + from.getChatid());
    }

}
