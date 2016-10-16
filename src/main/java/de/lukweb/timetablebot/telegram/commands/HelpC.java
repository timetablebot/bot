package de.lukweb.timetablebot.telegram.commands;

import com.google.common.collect.Lists;
import de.lukweb.timetablebot.ModuleLoader;
import de.lukweb.timetablebot.telegram.TelegramUser;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class HelpC extends TelegramCommand {

    private ModuleLoader moduleLoader;

    public HelpC(ModuleLoader moduleLoader) {
        super("help", "Zeigt dir Informationen über alle Commands an!");
        withoutVerification();

        this.moduleLoader = moduleLoader;
    }

    @Override
    public void run(Message message, TelegramUser user) {
        String text = "Das ist der Stundenplanbot für die Alte Landesschule!\n\n" +
                "Antworten auf einige Fragen findest du im Web: https://stundenplanbot.ga\n\n" +
                "Das kannst du tun: \n";

        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (TelegramCommand command : moduleLoader.getCommands()) {
            if (!command.showInShortHelp()) continue;
            if (!command.canUse(user)) continue;
            if (command.getCommand().equalsIgnoreCase("verify") && user.isVerified()) continue;
            if (command.getCommand().equalsIgnoreCase("help")) continue;

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(command.getShortHelp());
            button.setCallbackData("Command_" + command.getCommand());

            buttons.add(button);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(Lists.partition(buttons, 2));

        user.messages().keyboard(text, markup);
    }

}
