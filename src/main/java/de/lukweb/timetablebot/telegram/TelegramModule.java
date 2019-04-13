package de.lukweb.timetablebot.telegram;

import de.lukweb.timetablebot.BotModule;
import de.lukweb.timetablebot.ModuleLoader;
import de.lukweb.timetablebot.telegram.callbacks.CallbackQueryHandler;
import de.lukweb.timetablebot.telegram.callbacks.CommandCallback;
import de.lukweb.timetablebot.telegram.commands.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TelegramModule extends BotModule {

    private ModuleLoader moduleLoader;

    public TelegramModule() {
        name = "telegram";
    }

    @Override
    protected void setModuleLoader(ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;
    }

    @Override
    public List<Runnable> tasks() {
        return Collections.singletonList(
                new WelcomeBackTimer.WelcomeBackRunnable()
        );
    }

    @Override
    public List<TelegramCommand> commands() {
        return Arrays.asList(
                new DeleteEverythingC(),
                new HelpC(moduleLoader),
                new ListCMDsC(moduleLoader),
                new PanicC(),
                new StartC(),
                new VerifyC(getConfig())
        );
    }

    @Override
    public List<CallbackQueryHandler> queryHandlers() {
        return Collections.singletonList(
                new CommandCallback(moduleLoader)
        );
    }
}
