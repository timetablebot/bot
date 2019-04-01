package de.lukweb.timetablebot.timetable.sql;

import de.lukweb.timetablebot.telegram.TelegramUser;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.List;
import java.util.stream.Collectors;

public class TeachersSQL {

    private Handle handle;

    public TeachersSQL(Handle handle) {
        this.handle = handle;
    }

    public void addMissingTeachers(List<String> names) {
        PreparedBatch batch = handle.prepareBatch("INSERT IGNORE INTO teachers (name) VALUES (?)");
        names.stream()
                .filter(name -> !name.equals("") && !name.contains(","))
                .map(String::toLowerCase)
                .forEach(batch::add);
        if (batch.size() > 0) {
            batch.execute();
        }
        batch.close();
    }

    public List<String> getAll() {
        return handle.select("SELECT `name` FROM teachers WHERE name NOT LIKE '%,%' OR name LIKE ''")
                .mapTo(String.class)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    public List<String> loadTeachers(long chatid) {
        return handle.select("SELECT tea.name FROM users_teachers usts " +
                "LEFT JOIN teachers tea ON usts.teacher = tea.id " +
                "WHERE user = ?", chatid)
                .mapTo(String.class)
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public void saveTeachers(TelegramUser user) {
        // Adding the missing teachers
        addMissingTeachers(user.getTeachers());

        long chatid = user.getChatid();
        // Removing the old entries
        handle.execute("DELETE FROM users_teachers WHERE user = ?", chatid);

        // Adding the new ones
        PreparedBatch batch = handle.prepareBatch("INSERT INTO users_teachers (user, teacher) " +
                "VALUES (?, (SELECT id FROM teachers WHERE name = ?))");

        user.getTeachers().forEach(teacher -> batch.add(chatid, teacher));
        if (batch.size() > 0) {
            batch.execute();
        }
        batch.close();
    }
}
