package de.lukweb.timetablebot.timetable.sql;

import de.lukweb.timetablebot.timetable.repres.Amendment;
import de.lukweb.timetablebot.timetable.repres.AmendmentBuilder;
import de.lukweb.timetablebot.timetable.repres.AmendmentType;
import de.lukweb.timetablebot.timetable.repres.GradeRange;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.mapper.RowMapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AmendmentSQL {

    private Handle handle;
    private RowMapper<Amendment> amendmentMapper;

    public AmendmentSQL(Handle handle) {
        this.handle = handle;
        this.amendmentMapper = getAmendmentMapper();
    }

    private RowMapper<Amendment> getAmendmentMapper() {
        return (rs, ctx) -> new AmendmentBuilder()
                .setType(AmendmentType.valueOf(rs.getString("type")))
                .setDate(rs.getLong("date"))
                .setLesson(rs.getString("lesson"))
                .setTeacher(rs.getString("teacher"))
                .setSubject(rs.getString("subject"))
                .setReplacementTeacher(rs.getString("replacement"))
                .setGrade(new GradeRange(rs.getString("grade")))
                .setRoom(rs.getString("room")).setWrittenBy("")
                .setAddtionalInformation(rs.getString("addtional_information"))
                .build();
    }

    public boolean exists(Amendment amendment) {
        Optional<Integer> count = handle.select(
                "SELECT COUNT(*) AS count FROM amendments WHERE  " +
                        "  type = ? AND " +
                        "  date = ? AND " +
                        "  lesson = ? AND " +
                        "  teacher = ? AND  " +
                        "  subject = ? AND  " +
                        "  replacement = ? AND " +
                        "  grade = ? AND " +
                        "  room = ? AND " +
                        "  addtional_information = ?",
                amendment.getType().name(), amendment.getDate(), amendment.getLesson(), amendment.getTeacher(),
                amendment.getSubject(), amendment.getReplacementTeacher(), amendment.getGrade().toString(),
                amendment.getRoom(), amendment.getAddtionalInformation()
        ).mapTo(Integer.class).findFirst();

        return count.isPresent() && count.get() > 0;
    }

    public void insert(Amendment amendment) {
        if (amendment == null) return;
        handle.execute(
                "INSERT INTO amendments " +
                        "(type, date, lesson, teacher, subject, replacement, grade, room, addtional_information) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ",
                amendment.getType().name(), amendment.getDate(), amendment.getLesson(), amendment.getTeacher(),
                amendment.getSubject(), amendment.getReplacementTeacher(), amendment.getGrade().toString(),
                amendment.getRoom(), amendment.getAddtionalInformation()
        );
    }

    public void delete(Amendment amendment) {
        if (amendment == null) return;
        handle.execute("UPDATE amendments SET deleted = 1 WHERE " +
                        "  type = ? AND " +
                        "  date = ? AND " +
                        "  lesson = ? AND " +
                        "  teacher = ? AND  " +
                        "  subject = ? AND  " +
                        "  replacement = ? AND " +
                        "  grade = ? AND " +
                        "  room = ? AND " +
                        "  addtional_information = ?",
                amendment.getType().name(), amendment.getDate(), amendment.getLesson(), amendment.getTeacher(),
                amendment.getSubject(), amendment.getReplacementTeacher(), amendment.getGrade().toString(),
                amendment.getRoom(), amendment.getAddtionalInformation()
        );
    }

    public List<Amendment> getLastTwoDays() {
        List<Amendment> amendments = handle.select(
                "SELECT * FROM amendments WHERE date IN (SELECT * FROM " +
                        "(SELECT date FROM amendments GROUP BY date ORDER BY date DESC LIMIT 2) AS t) AND deleted = 0"
        ).map(amendmentMapper).list();

        amendments = amendments.stream()
                .distinct()
                .collect(Collectors.toList());

        return amendments;
    }

    public List<Amendment> getAllFromDate(long date) {
        List<Amendment> amendments = handle.select(
                "SELECT * FROM amendments WHERE date >= ? AND deleted = 0", date
        ).map(amendmentMapper).list();

        amendments = amendments.stream()
                .distinct()
                .collect(Collectors.toList());

        return amendments;
    }

    public List<String> getTeachers() {
        List<String> teachers = handle
                .select("SELECT teacher FROM `amendments` WHERE teacher NOT LIKE '%,%' GROUP BY teacher")
                .mapTo(String.class)
                .list();
        teachers.removeIf(teacher -> teacher.trim().length() <= 1);
        return teachers;
    }

    /**
     * @param date The first date since the data will kept
     */
    public void deleteOld(long date) {
        long today = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        if (today > date) return;
        handle.execute("DELETE FROM amendments WHERE date < ?", date);
    }

}
