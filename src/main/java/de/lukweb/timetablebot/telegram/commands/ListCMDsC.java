package de.lukweb.timetablebot.telegram.commands;

import de.lukweb.timetablebot.ModuleLoader;
import de.lukweb.timetablebot.telegram.TelegramUser;
import org.telegram.telegrambots.meta.api.objects.Message;

public class ListCMDsC extends TelegramCommand {

    private ModuleLoader moduleLoader;

    public ListCMDsC(ModuleLoader moduleLoader) {
        super("helpcmd", null, "Commands auflisten");
        withoutVerification();

        this.moduleLoader = moduleLoader;
    }

    @Override
    protected void run(Message message, TelegramUser user) {
        StringBuilder commandStr = new StringBuilder();

        for (TelegramCommand command : moduleLoader.getCommands()) {
            if (!command.showInHelp()) continue;
            if (!command.canUse(user)) continue;
            if (command.getCommand().equalsIgnoreCase("verify") && user.isVerified()) continue;

            commandStr
                    .append("/")
                    .append(command.getCommand())
                    .append(" - ")
                    .append(command.getHelp())
                    .append("\n");
        }

        user.messages().send("Das ist der Stundenplanbot für die Alte Landesschule!\n" +
                "Verfügbare Commands: \n" + commandStr);
    }

}
