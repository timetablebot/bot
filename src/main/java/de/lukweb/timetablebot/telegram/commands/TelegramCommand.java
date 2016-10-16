package de.lukweb.timetablebot.telegram.commands;

import de.lukweb.timetablebot.telegram.TelegramRank;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

public abstract class TelegramCommand {

    private String command;
    private String shortHelp;
    private String help;
    private TelegramRank rank;
    private boolean verification;

    protected Logger logger;

    public TelegramCommand(String command, String help) {
        this(command, help, help);
    }

    public TelegramCommand(String command, String shortHelp, String help) {
        this.command = command;
        this.help = help;
        this.shortHelp = shortHelp;
        this.rank = TelegramRank.USER;
        this.verification = true;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    protected void withoutVerification() {
        this.verification = false;
    }

    protected void requireRank(TelegramRank rank) {
        this.rank = rank;
    }

    protected void scheduleTask(Runnable runnable) {
        ThreadUtils.schedule(runnable);
    }

    public String getCommand() {
        return command;
    }

    public String getHelp() {
        if (help != null) {
            return help;
        } else {
            logger.warn("No help for command '{}', but was requested", command);
            return "";
        }
    }

    public String getShortHelp() {
        if (shortHelp != null) {
            return shortHelp;
        } else {
            logger.warn("No short help for command '{}', but was requested", command);
            return "";
        }
    }

    public boolean showInHelp() {
        return help != null;
    }

    public boolean showInShortHelp() {
        return shortHelp != null;
    }

    public boolean canUse(TelegramUser user) {
        if (verification && !user.isVerified()) {
            return false;
        }

        if (user.getRank().getImportance() < rank.getImportance()) {
            return false;
        }

        return true;
    }

    public void execute(Message message, TelegramUser user) {
        if (!user.isVerified() && verification) {
            user.messages().send("Du musst dich erst mit /verify verfizieren!");
            return;
        }

        if (user.getRank().getImportance() < rank.getImportance()) {
            user.messages().send("Du hast darauf keinen Zugriff!");
            return;
        }

        run(message, user);
    }

    protected abstract void run(Message message, TelegramUser user);

}
