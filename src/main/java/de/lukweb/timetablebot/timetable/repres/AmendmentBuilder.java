package de.lukweb.timetablebot.timetable.repres;

public class AmendmentBuilder {

    private AmendmentType type;
    private long date;
    private String lesson;
    private String teacher;
    private String subject;
    private String replacementTeacher;
    private GradeRange grade;
    private String room;
    private String writtenBy;
    private String addtionalInformation;

    public AmendmentBuilder setType(AmendmentType type) {
        this.type = type;
        return this;
    }

    public AmendmentBuilder setDate(long date) {
        this.date = date;
        return this;
    }

    public AmendmentBuilder setLesson(String lesson) {
        this.lesson = lesson;
        return this;
    }

    public AmendmentBuilder setTeacher(String teacher) {
        this.teacher = teacher;
        return this;
    }

    public AmendmentBuilder setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public AmendmentBuilder setReplacementTeacher(String replacementTeacher) {
        this.replacementTeacher = replacementTeacher;
        return this;
    }

    public AmendmentBuilder setGrade(GradeRange grade) {
        this.grade = grade;
        return this;
    }

    public AmendmentBuilder setRoom(String room) {
        this.room = room;
        return this;
    }

    public AmendmentBuilder setWrittenBy(String writtenBy) {
        this.writtenBy = writtenBy;
        return this;
    }

    public AmendmentBuilder setAddtionalInformation(String addtionalInformation) {
        this.addtionalInformation = addtionalInformation;
        return this;
    }

    public Amendment build() {
        return new Amendment(type, date, lesson, teacher, subject, replacementTeacher, grade, room, writtenBy,
                addtionalInformation);
    }

    public AmendmentBuilder copy() {
        AmendmentBuilder other = new AmendmentBuilder();
        other.type = this.type;
        other.date = this.date;
        other.lesson = this.lesson;
        other.teacher = this.teacher;
        other.subject = this.subject;
        other.replacementTeacher = this.replacementTeacher;
        other.grade = this.grade;
        other.room = this.room;
        other.writtenBy = this.writtenBy;
        other.addtionalInformation = this.addtionalInformation;
        return other;
    }

}