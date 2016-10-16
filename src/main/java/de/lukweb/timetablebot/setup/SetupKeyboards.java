package de.lukweb.timetablebot.setup;

import com.google.common.collect.Lists;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetupKeyboards {

    public ReplyKeyboardMarkup grade() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setOneTimeKeyboard(false);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        row1.add("5");
        row1.add("6");
        row1.add("7");

        row2.add("8");
        row2.add("9");
        row2.add("10");

        row3.add("11");
        row3.add("12");
        row3.add("13");

        markup.setKeyboard(Arrays.asList(
                row1, row2, row3
        ));
        return markup;
    }

    public ReplyKeyboardMarkup classChar() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setOneTimeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        row1.add("a");
        row1.add("b");
        row1.add("c");

        row2.add("d");
        row2.add("e");
        row2.add("f");

        row3.add("g");
        row3.add("h");
        row3.add("i");

        markup.setKeyboard(Arrays.asList(
                row1, row2, row3
        ));
        return markup;
    }

    /**
     * https://core.telegram.org/bots/api#inlinekeyboardmarkup
     */
    public InlineKeyboardMarkup teachers(List<String> teachers, List<String> selected) {
        // Maximum of 33 lines if every line contains 3 elements. (=> 100 Elements)
        int teachersPerLine = 3;

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        // Splitting the teachers list to lists of _teachersPerLine_
        for (List<String> teacherRow : Lists.partition(teachers, teachersPerLine)) {
            List<InlineKeyboardButton> subButtons = new ArrayList<>();

            for (String teacher : teacherRow) {
                // Creating the button for each teacher
                boolean active = selected.contains(teacher.toUpperCase());
                subButtons.add(teacherButton(teacher, active));
            }

            // Adding the list of buttons to an upper list as an row
            buttons.add(subButtons);
        }

        // Add a save button
        InlineKeyboardButton finishButton = new InlineKeyboardButton();
        finishButton.setCallbackData("Teacher_Do_Finish");
        finishButton.setText("Fertig");
        buttons.add(Collections.singletonList(finishButton));

        // Creating the markup and adding the list
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(buttons);
        return markup;
    }

    private InlineKeyboardButton teacherButton(String name, boolean active) {
        InlineKeyboardButton button = new InlineKeyboardButton();

        String text = name + (active ? " ✅" : " ✖");
        button.setText(text);

        // A fallback if the teachers name is too long
        String callbackText = "Teacher_";
        /* if (name.length() > 16) {
            callbackText += "L_" + name.substring(0, 16);
        } else {
            callbackText += name;
        } */
        callbackText += name;
        button.setCallbackData(callbackText);

        return button;
    }

    public ReplyKeyboardMarkup yesNo() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setOneTimeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();

        row1.add("Ja");
        row1.add("Nein");

        markup.setKeyboard(Collections.singletonList(row1));
        return markup;
    }

}
