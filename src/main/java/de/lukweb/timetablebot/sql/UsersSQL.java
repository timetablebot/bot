package de.lukweb.timetablebot.sql;

import de.lukweb.timetablebot.telegram.TelegramRank;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.timetable.sql.TeachersSQL;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.mapper.RowMapper;

import java.util.List;

public class UsersSQL {

    private Handle handle;
    private RowMapper<TelegramUser> userMapper;

    public UsersSQL(Handle handle) {
        this.handle = handle;
        this.userMapper = getUserMapper();
    }

    private RowMapper<TelegramUser> getUserMapper() {
        return (rs, ctx) -> new TelegramUser(
                rs.getLong("chatid"),
                rs.getBoolean("blocked"),
                rs.getBoolean("disabled"),
                rs.getBoolean("verified"),
                TelegramRank.valueOf(rs.getString("rank")),
                rs.getInt("grade"),
                rs.getString("classChar").charAt(0),
                new TeachersSQL(handle).loadTeachers(rs.getLong("chatid"))
        );
    }

    public List<TelegramUser> loadAll() {
        handle.select("SELECT * FROM teachers");
        return handle.select("SELECT * FROM users").map(userMapper).list();
    }

    public void insert(TelegramUser user) {
        long timestamp = System.currentTimeMillis() / 1000;
        handle.execute(
                "INSERT INTO users (chatid, blocked, disabled, verified, `rank`, grade, classChar, name, created, updated) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                user.getChatid(),
                user.isBlocked(),
                user.isDisabled(),
                user.isVerified(),
                user.getRank().name(),
                user.getGrade(),
                user.getClassChar() + "",
                trimToLength(user.getTelegramName(), 99),
                timestamp,
                timestamp
        );
        new TeachersSQL(handle).saveTeachers(user);
    }

    public void update(TelegramUser user) {
        handle.execute(
                "UPDATE users SET blocked = ?, disabled = ?, verified = ?, `rank` = ?, grade = ?, classChar = ?, name = ?, updated = ? " +
                        "WHERE chatid = ?",
                user.isBlocked(),
                user.isDisabled(),
                user.isVerified(),
                user.getRank().name(),
                user.getGrade(),
                user.getClassChar() + "",
                trimToLength(user.getTelegramName(), 99),
                System.currentTimeMillis() / 1000,
                ////
                user.getChatid()
        );
        new TeachersSQL(handle).saveTeachers(user);
    }

    public void delete(TelegramUser user) {
        handle.execute("DELETE FROM users WHERE chatid = ?", user.getChatid());
    }

    private String trimToLength(String string, int maxLength) {
        if (string.length() <= maxLength) {
            return string;
        }
        return string.substring(0, maxLength);
    }

}
