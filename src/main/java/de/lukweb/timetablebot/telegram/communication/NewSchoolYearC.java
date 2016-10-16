package de.lukweb.timetablebot.telegram.communication;

import de.lukweb.timetablebot.telegram.TelegramRank;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.Users;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

public class NewSchoolYearC extends TelegramCommand {

    public NewSchoolYearC() {
        super("newschoolyear", null, "Nachricht zum neuen Schuljahr");
        requireRank(TelegramRank.ADMIN);
    }

    @Override
    public void run(Message message, TelegramUser user) {
        int year = LocalDateTime.now().getYear();

        String text = "Willkommen im neuem Schuljahr " + year + " / " + (year + 1) + "!";

        ArrayList<InlineKeyboardButton> firstLine = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton("Klasse / Lehrer ändern");
        button.setCallbackData("Command_grade");
        firstLine.add(button);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(Collections.singletonList(firstLine));

        Users.getAll().stream()
                .filter(otherUser -> !otherUser.isBlocked() && !otherUser.isDisabled())
                .forEach(otherUser -> otherUser.messages().keyboard(text, markup));
    }
}
