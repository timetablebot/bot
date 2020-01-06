package de.lukweb.timetablebot.timetable.repres;

import de.lukweb.timetablebot.telegram.TelegramUser;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Amendment {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("eeee 'den' dd.MM.").withLocale(Locale.GERMAN);

    private AmendmentType type;
    private long date;
    private String lesson; // todo parse this
    private String teacher;
    private String subject;
    private String replacementTeacher;
    private GradeRange grade;
    private String room;
    private String writtenBy;
    private String additionalInformation;

    public Amendment(AmendmentType type, long date, String lesson, String teacher, String subject,
                     String replacementTeacher, GradeRange grade, String room, String writtenBy, String additionalInformation) {
        this.type = type;
        this.date = date;
        this.lesson = lesson;
        this.teacher = teacher;
        this.subject = subject;
        this.replacementTeacher = replacementTeacher;
        this.grade = grade;
        this.room = room;
        this.writtenBy = writtenBy;
        this.additionalInformation = additionalInformation.replaceAll("\\n", "").replaceAll("\\r", "");
    }

    public AmendmentType getType() {
        return type;
    }

    public String getLesson() {
        return lesson;
    }

    public LessonRange getParsedLesson() {
        return new LessonRange(lesson);
    }

    public String getTeacher() {
        return teacher;
    }

    public String getSubject() {
        return subject;
    }

    public String getReplacementTeacher() {
        return replacementTeacher;
    }

    public GradeRange getGrade() {
        return grade;
    }

    public String getRoom() {
        return room;
    }

    public String getWrittenBy() {
        return writtenBy;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public long getDate() {
        return date;
    }

    public boolean fits(TelegramUser user) {
        if (user.isDisabled() || user.isBlocked()) {
            return false;
        }
        return grade.contains(user.getGrade(), user.getClassChar()) &&
                user.getTeachers().contains(teacher.toLowerCase());
    }

    public void send(TelegramUser user, Notification type) {
        switch (type) {
            case NEW:
                user.messages().send(buildMessage());
                break;
            case DELETE:
                user.messages().send(buildRemove());
                break;
            default:
                LoggerFactory.getLogger(getClass()).warn("There's no message for Amendment.Notifcation." + type.name());
                break;
        }
    }

    private String buildMessage() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(getDate()), ZoneOffset.UTC);

        boolean replacement = getReplacementTeacher().trim().equalsIgnoreCase("---");

        String message = "Du hast am " + FORMATTER.format(dateTime) + " eine Stundenplanänderung:\n" +
                "Stunde(n): " + getLesson() + "\n" +
                "Typ: " + getType().getFullName() + "\n" +
                "Lehrer: " + getTeacher() + (replacement ? " wird durch " +
                getReplacementTeacher() + " vertreten" : "") + "\n" +
                "Fach: " + getSubject() + "\n" +
                "Raum: " + getRoom() + "\n";

        if (getAdditionalInformation().trim().length() > 1) {
            message += "Zusätzliche Informationen: " + getAdditionalInformation() + "\n";
        }

        GradeRange grade = getGrade();
        message += "Dies gilt für " + grade.translate();

        return message;
    }

    private String buildRemove() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(getDate()), ZoneOffset.UTC);

        String message = "Die Vertretung am " + FORMATTER.format(dateTime) + " \n" +
                "Stunde(n): " + lesson + " \n" +
                "Typ: " + type.getFullName() + "\n" +
                "Lehrer: " + teacher + "\n";

        if (getAdditionalInformation().trim().length() > 1) {
            message += "Informationen: " + additionalInformation + "\n";
        }

        message += "wurde vom Vertretungsplan *entfernt*!";

        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Amendment amendment = (Amendment) o;

        if (date != amendment.date) return false;
        if (type != amendment.type) return false;
        if (!lesson.equals(amendment.lesson)) return false;
        if (!teacher.equals(amendment.teacher)) return false;
        if (!subject.equals(amendment.subject)) return false;
        if (!replacementTeacher.equals(amendment.replacementTeacher)) return false;
        if (!grade.equals(amendment.grade)) return false;
        if (!room.equals(amendment.room)) return false;
        return additionalInformation.equals(amendment.additionalInformation);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (date ^ (date >>> 32));
        result = 31 * result + lesson.hashCode();
        result = 31 * result + teacher.hashCode();
        result = 31 * result + subject.hashCode();
        result = 31 * result + replacementTeacher.hashCode();
        result = 31 * result + grade.hashCode();
        result = 31 * result + room.hashCode();
        result = 31 * result + additionalInformation.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Amendment{" +
                "type=" + type +
                ", date=" + date +
                ", lesson='" + lesson + '\'' +
                ", teacher='" + teacher + '\'' +
                ", subject='" + subject + '\'' +
                ", replacementTeacher='" + replacementTeacher + '\'' +
                ", grade=" + grade +
                ", room='" + room + '\'' +
                ", writtenBy='" + writtenBy + '\'' +
                ", additionalInformation='" + additionalInformation + '\'' +
                '}';
    }

    public enum Notification {
        NEW,
        DELETE,
    }
}
