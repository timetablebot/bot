package de.lukweb.timetablebot.telegram.callbacks;

import de.lukweb.timetablebot.ModuleLoader;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public class CommandCallback implements CallbackQueryHandler {

    private Logger logger;
    private ModuleLoader moduleLoader;

    public CommandCallback(ModuleLoader moduleLoader) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.moduleLoader = moduleLoader;
    }

    @Override
    public String getMatchingPrefix() {
        return "command_";
    }

    @Override
    public AnswerCallbackQuery execute(CallbackQuery query, TelegramUser user) {
        String data = query.getData().toLowerCase().replace("command_", "");

        TelegramCommand command = moduleLoader.getCommand(data);
        if (command == null) {
            logger.warn("No command with the name '{}' found", data);
            return null;
        }

        command.execute(query.getMessage(), user);

        return null;
    }

}
