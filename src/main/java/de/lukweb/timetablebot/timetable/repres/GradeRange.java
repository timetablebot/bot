package de.lukweb.timetablebot.timetable.repres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradeRange {

    private static Pattern gradePattern = Pattern.compile("(\\d?\\d)([a-z]+)?");
    private static List<String> ignore = new ArrayList<>(Arrays.asList("", "als", "daz", "sl", "s1", "pr", "sport"));

    private Logger logger;
    private List<Integer> grades;
    private List<Character> classes;

    public GradeRange(String gradeStr) {
        logger = LoggerFactory.getLogger(getClass());
        grades = new ArrayList<>();
        classes = new ArrayList<>(); // This should be empty if there are multiple grades
        parse(gradeStr.trim());
    }

    private void parse(String gradeStr) {
        if (gradeStr.equals("1112")) {
            // If it's an lessons with students from the 11. & 12. grade, this will catch it
            grades.add(11);
            grades.add(12);
            return;
        } else if (ignore.contains(gradeStr.toLowerCase())) {
            // We ignore grades which are meant to address the whole school
            return;
        }

        String[] parts = gradeStr.split(";");
        for (String part : parts) {
            // Try to match the part
            Matcher matcher = gradePattern.matcher(part.trim());
            // If it doesn't matches we ignore this part
            if (!matcher.matches()) {
                logger.warn("Couldn't match part '{}' of grade string '{}'", part, gradeStr);
                continue;
            }
            // If it's a single class or the whole grade the matcher will find it
            grades.add(Integer.parseInt(matcher.group(1)));

            String classesStr = matcher.group(2);
            if (classesStr != null) {
                classesStr.trim().chars().mapToObj(i -> ((char) i)).forEach(classes::add);
            }
        }

        // If there are no results we log it
        if (grades.isEmpty()) {
            logger.warn("Couldn't parse grade string: '{}'", gradeStr);
            // But then we add it to the ignore list
            ignore.add(gradeStr);
        }
    }

    private boolean isAllClasses() {
        return classes.isEmpty();
    }

    private boolean containsGrade(int grade) {
        return grades.contains(grade);
    }

    private boolean containsSubgrade(char className) {
        return isAllClasses() || classes.contains(className);
    }

    /**
     * Gets whether an error during the parsing occurred
     *
     * @return Whether there was a error
     */
    public boolean hasError() {
        return grades.isEmpty();
    }

    /**
     * Checks whether the $grades and the $className are included in
     * this set of grade(s) and subgrades
     *
     * @param grade     The grade to check
     * @param className The name of the subgrade
     * @return Whether it's included in this set of grades
     */
    public boolean contains(int grade, char className) {
        return containsGrade(grade) && containsSubgrade(className);
    }

    /**
     * Translates this grade range to a German
     *
     * @return The translation to German
     */
    public String translate() {
        if (grades.size() == 1 && classes.size() == 1) {
            return "die Klasse " + toString();
        } else if (grades.size() == 1 && classes.size() > 1) {
            return "die Klassen " + toString();
        } else if (grades.size() == 1) {
            return "die Jahrgangsstufe " + toString();
        } else if (grades.size() > 1) {
            String gradesAnd = grades.stream().map(Object::toString).collect(Collectors.joining(" und "));
            return "die Jahrgangsstufen " + gradesAnd;
        } else {
            logger.warn("Couldn't translate the Grade: '{}'", toString());
            return null;
        }
    }

    @Override
    public String toString() {
        if (grades.size() == 0) {
            return "";
        } else if (grades.size() == 1) {
            return grades.get(0) + classes.stream().map(Object::toString).collect(Collectors.joining(""));
        } else {
            return grades.stream().map(Object::toString).collect(Collectors.joining(";"));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GradeRange grade = (GradeRange) o;

        if (!grades.equals(grade.grades)) return false;
        return classes.equals(grade.classes);
    }

    @Override
    public int hashCode() {
        int result = grades.hashCode();
        result = 31 * result + classes.hashCode();
        return result;
    }
}
