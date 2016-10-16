package de.lukweb.timetablebot.timetable.repres;

import org.junit.Test;

import static org.junit.Assert.*;

public class LessonTests {

    @Test
    public void testSingle() {
        String lesson = "2";

        LessonRange lessonRange = new LessonRange(lesson);
        assertFalse(lessonRange.hasError());
        assertTrue(lessonRange.contains(new LessonRange(lesson)));
        assertEquals(lessonRange.toString(), lesson);
    }

    @Test
    public void testInvalid() {
        String lesson = "6a-fs6";

        LessonRange lessonRange = new LessonRange(lesson);
        assertTrue(lessonRange.hasError());
    }

    @Test
    public void testPair() {
        String lesson = " 4 - 8 ";

        LessonRange lessonRange = new LessonRange(lesson);
        assertFalse(lessonRange.hasError());
        assertTrue(lessonRange.contains(new LessonRange(lesson)));
        assertFalse(lessonRange.contains(new LessonRange("3-6")));
        assertTrue(lessonRange.contains(new LessonRange("5-6")));
        assertEquals(lessonRange.toString(), lesson.trim());
    }

}
