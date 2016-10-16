package de.lukweb.timetablebot.timetable.repres;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LessonRange {

    private static final Pattern PATTERN = Pattern.compile("(\\d+)(?:[ ]*-[ ]*(\\d+))?");

    private int start;
    private int end;

    public LessonRange(String lesson) {
        this.start = -1;
        this.end = -1;
        parse(lesson);
    }

    private void parse(String lesson) {
        lesson = lesson.trim();

        Matcher matcher = PATTERN.matcher(lesson);
        if (!matcher.matches()) {
            return;
        }

        if (matcher.group(1) == null) {
            return;
        }

        try {
            int start = Integer.parseInt(matcher.group(1));
            int end = start;

            if (matcher.group(2) != null) {
                end = Integer.parseInt(matcher.group(2));
            }

            this.start = start;
            this.end = end;
        } catch (NumberFormatException ignored) {
            return;
        }

    }

    public boolean hasError() {
        return start == -1;
    }

    public boolean contains(LessonRange lessonRange) {
        return start <= lessonRange.start && lessonRange.end <= end;
    }

    @Override
    public String toString() {
        if (end != start) {
            return start + " - " + end;
        } else {
            return start + "";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LessonRange that = (LessonRange) o;
        return start == that.start &&
                end == that.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
