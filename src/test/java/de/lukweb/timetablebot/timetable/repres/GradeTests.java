package de.lukweb.timetablebot.timetable.repres;

import org.junit.Test;

import static org.junit.Assert.*;

public class GradeTests {

    @Test
    public void testSingle() {
        assertTrue(new GradeRange("1z").contains(1, 'z'));
        assertTrue(new GradeRange("01z").contains(1, 'z'));
        assertTrue(new GradeRange("12e").contains(12, 'e'));
        assertTrue(new GradeRange("1111a").hasError());
    }

    @Test
    public void testStage() {
        GradeRange grade = new GradeRange("12");

        assertFalse(grade.hasError());
        assertTrue(grade.contains(12, 'a'));
        assertTrue(grade.contains(12, 'b'));
        assertTrue(grade.contains(12, 'c'));
        assertTrue(grade.contains(12, 'z'));
    }

    @Test
    public void testMultiple() {
        GradeRange grade = new GradeRange("06abcdefm");

        assertFalse(grade.hasError());
        assertTrue(grade.contains(6, 'a'));
        assertTrue(grade.contains(6, 'b'));
        assertTrue(grade.contains(6, 'c'));
        assertTrue(grade.contains(6, 'd'));
        assertTrue(grade.contains(6, 'e'));
        assertTrue(grade.contains(6, 'f'));
        assertTrue(grade.contains(6, 'm'));
        assertFalse(grade.contains(6, 'k'));
    }

    @Test
    public void testSerialisation() {
        checkToString("1112");
        checkToString("11def");
        checkToString("10");
        checkToString("7");
        checkToString("6abc");
    }

    private void checkToString(String gradeString) {
        GradeRange grade = new GradeRange(gradeString);
        assertFalse(grade.hasError());

        GradeRange gradeRead = new GradeRange(grade.toString());
        assertEquals(grade, gradeRead);
    }

}
