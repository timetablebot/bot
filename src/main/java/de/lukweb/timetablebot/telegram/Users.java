package de.lukweb.timetablebot.telegram;

import de.lukweb.timetablebot.sql.DB;
import de.lukweb.timetablebot.sql.UsersSQL;
import de.lukweb.timetablebot.timetable.repres.Amendment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Users {

    private static HashMap<Long, TelegramUser> users = new HashMap<>();

    public static void load() {
        DB.get().withHandle(handle -> new UsersSQL(handle).loadAll()).forEach(Users::add);
    }

    public static void add(TelegramUser user) {
        users.put(user.getChatid(), user);
    }

    public static void remove(TelegramUser user) {
        users.remove(user.getChatid());
    }

    public static TelegramUser get(long chatid) {
        return users.get(chatid);
    }

    public static List<TelegramUser> getAll() {
        return new ArrayList<>(users.values());
    }

    public static void notify(Amendment amendment, boolean remove) {
        users.values().stream()
                .filter(amendment::fits)
                .forEach(user -> {
                    if (remove) {
                        amendment.sendRemove(user);
                    } else {
                        amendment.send(user);
                    }
                });
    }

}
