package de.lukweb.timetablebot.timetable.repres;

public class News {

    private TimetableDay day;
    private String content;

    public News(TimetableDay day, String content) {
        this.day = day;
        this.content = content;
    }

    public TimetableDay getDay() {
        return day;
    }

    public String getContent() {
        return content;
    }
}
