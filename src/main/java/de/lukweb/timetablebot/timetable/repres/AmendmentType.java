package de.lukweb.timetablebot.timetable.repres;

public enum AmendmentType {

    DROPPED_LESSON("Entfall"),
    STUDYING_TIME("Studierzeit"),
    EVENT("Veranst.", "Veranstaltung"),
    REPLACEMENT_TEACHER("Vertretung"),
    TEACHER_EXCHANGE("Lehrertausch"),
    REPLACEMENT_WITHOUT_TEACHER("Vtr. ohne Lehrer", "Vertretung ohne Lehrer"),
    INSTEAD_REPLACEMENT("Statt-Vertretung"),
    CARE_TEACHER("Betreuung"),
    EXCHANGE("Tausch"),
    OTHER_ROOM("Raum-Vtr.", "Raum-Vertretung"),
    LESSON_CHANGED("Unterricht geändert"),
    DESPITE_ABSENCE("Trotz Absenz"),
    UNDEFINDED("Unbekannt"),
    SPECIAL_OPERATION("Sondereins.", "Sondereinsatz"),
    SHIFTING("Verlegung"),
    WORKING_WITHOUT_TEACHER("freies Arbeiten"),
    PRE_MARK("Vormerkung");

    private String tableName;
    private String fullName;

    AmendmentType(String tableName) {
        this.tableName = tableName;
        this.fullName = tableName;
    }

    AmendmentType(String tableName, String fullName) {
        this.tableName = tableName;
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public static AmendmentType getByName(String name) {
        for (AmendmentType type : values()) if (type.tableName.equalsIgnoreCase(name)) return type;
        return null;
    }

}
