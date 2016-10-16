package de.lukweb.timetablebot.timetable.repres;

public enum TimetableDay {

    TODAY("heute"),
    TOMORROW("morgen");

    private String webPath;

    TimetableDay(String webPath) {
        this.webPath = webPath;
    }

    public String getWebPath() {
        return webPath;
    }
}
