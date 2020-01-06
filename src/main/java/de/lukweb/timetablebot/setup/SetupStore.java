package de.lukweb.timetablebot.setup;

import de.lukweb.timetablebot.telegram.TelegramBot;
import de.lukweb.timetablebot.telegram.TelegramUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class SetupStore {

    private static SetupStore instance;

    public static SetupStore store() {
        if (instance == null) instance = new SetupStore();
        return instance;
    }


    private HashMap<TelegramUser, UserSetup> setups = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(getClass());

    public SetupState handleNew(TelegramUser user, String text) {
        UserSetup setup = setups.get(user);

        if (setup == null) {
            setup = new UserSetup(user);
            setups.put(user, setup);
        }

        return handle(user, text);
    }

    public void abort(TelegramUser user) {
        setups.remove(user);
    }

    public SetupState handle(TelegramUser user, String text) {
        UserSetup setup = setups.get(user);

        if (setup == null) {
            new UserSetup(user).invalidState();
            return SetupState.INVALID;
        }

        switch (setup.getState()) {
            case START:
                setup.prepareGrade();
                break;
            case GRADE:
                // Retry if the number check fails
                if (!setup.grade(text)) return setup.getState();
                setup.prepareClass();
                break;
            case CLASS:
                if (!setup.classChar(text)) {
                    setup.classError();
                    return setup.getState();
                }
                if (setup.directlyEnterTeachers()) {
                    // Skipping the NEED_TEACHERS state
                    setup.nextState();
                    setup.preapareTeachers();
                } else {
                    setup.promtForTeachers();
                }
                break;
            case NEED_TEACHERS:
                // Finishing the setup if the user don't want to enter teachers
                if (!setup.processTeacherPrompt(text)) {
                    setup.clearTeachers();
                    return finish(user, setup);
                }
                setup.preapareTeachers();
                break;
            case TEACHERS:
                if (!setup.isEndingTeachers(text)) {
                    setup.teachers(text);
                    return setup.getState();
                }
                // Just go to the next case
            case FINISH:
                return finish(user, setup);
        }

        setup.nextState();
        return setup.getState();
    }

    private SetupState finish(TelegramUser user, UserSetup setup) {
        setup.finish();
        setups.remove(user);
        TelegramBot.get().setCallback(user.getChatid(), null);
        return SetupState.FINISH;
    }

}
