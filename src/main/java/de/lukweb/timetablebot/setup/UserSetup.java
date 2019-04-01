package de.lukweb.timetablebot.setup;

import com.google.common.collect.Lists;
import de.lukweb.timetablebot.TimetableBot;
import de.lukweb.timetablebot.sql.DB;
import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.timetable.TimetableRunnable;
import de.lukweb.timetablebot.timetable.sql.TeachersSQL;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.*;

public class UserSetup {

    private TelegramUser user;
    private SetupKeyboards keyboards;
    private SetupState state;
    private HashMap<Integer, List<String>> teacherMessages;
    private List<String> teachers;

    public UserSetup(TelegramUser user) {
        this.user = user;
        this.keyboards = new SetupKeyboards();
        this.state = SetupState.START;
        this.teachers = new ArrayList<>();
        this.teacherMessages = new HashMap<>();
    }

    //
    // Invalid State
    //

    public void invalidState() {
        user.messages().send("Die Einrichtung ist bereits abgeschlossen! Um etwas zu ändern, benutze erneut /grade.");
    }

    //
    // State
    //

    public SetupState getState() {
        return state;
    }

    public void nextState() {
        SetupState[] states = SetupState.values();
        int index = Arrays.binarySearch(states, state);

        if (index < 0) {
            index = 0;
        } else {
            index++;
        }

        state = states[index];
    }

    //
    // Grade
    //

    public void prepareGrade() {
        user.messages().keyboard("Bitte wähle deine Jahrgangsstufe aus:", keyboards.grade());
    }

    public boolean grade(String gradeText) {
        int grade;

        try {
            grade = Integer.parseInt(gradeText);
        } catch (NumberFormatException ex) {
            user.messages().send("Du musst eine Zahl angeben!");
            return false;
        }

        if (grade < 5 || 13 < grade) {
            user.messages().send("Die Zahl muss zwischen 5 und 13 liegen!");
            return false;
        }

        user.setGrade(grade);
        return true;
    }

    //
    // Class
    //

    public void prepareClass() {
        user.messages().keyboard("Gebe nun deine Klasse an:", keyboards.classChar());
    }

    public boolean classChar(String classText) {
        char classChar = Character.toLowerCase(classText.charAt(0));
        if (!Character.isLetter(classChar) || !(classChar >= 97 && classChar <= 122)) {
            return false;
        }
        user.setClassChar(classChar);
        return true;
    }

    public void classError() {
        user.messages().send("Du musst einen Buchstaben von 'a' bis 'z' eingeben.  " +
                "Nutze dafür einfach die eingeblendete Tastatur!");
    }

    //
    // Teachers
    //

    /**
     * Clears the list of teachers for people who selected a grade lower than 10
     */
    public void clearTeachers() {
        user.getTeachers().clear();
    }

    public boolean directlyEnterTeachers() {
        return user.getGrade() >= 10;
    }

    public void promtForTeachers() {
        user.messages().keyboard("Hast du Stunden mit Mitschülern, die *nicht* aus deiner Klasse sind?",
                keyboards.yesNo());
    }

    public boolean processTeacherPrompt(String answer) {
        return answer.trim().equalsIgnoreCase("yes");
    }

    public void preapareTeachers() {
        teachers.clear();
        user.getTeachers().stream()
                .filter(teacher -> !teacher.trim().isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .forEach(teachers::add);

        teacherMessages = new HashMap<>();

        // Sending muliplte messages if there are more than 99 teachers
        List<List<String>> teacherLists = generateTeacherLists();
        for (int i = 0; i < teacherLists.size(); i++) {

            String text;
            if (i <= 0) {
                text = "Bei welchen Lehrern hast du Unterricht? Bitte wähle ihre *Kürzel* aus!";
            } else {
                text = "Hier noch ein paar mehr Lehrer zum Auswählen:";
            }

            List<String> teacherList = teacherLists.get(i);

            Message message = user.messages().keyboard(text, keyboards.teachers(teacherList, this.teachers));
            teacherMessages.put(message.getMessageId(), teacherList);
        }

    }

    /**
     * The keyboard is updated by the TeachersCallback when a new teacher is added.
     *
     * @see TeachersCallback
     */
    public void teachers(String teacher) {
        teacher = teacher.trim().toUpperCase();

        if (!teachers.contains(teacher)) {
            teachers.add(teacher);
        } else {
            teachers.remove(teacher);
        }

        // We're saving the List<String> in the HashMap to prevent a database request for
        // every newly selected teacher and to ensure persistency
        for (Map.Entry<Integer, List<String>> message : teacherMessages.entrySet()) {
            // Only updating the correct message with, otherwise there are a lot of errors
            if (!message.getValue().contains(teacher)) {
                continue;
            }

            user.messages().keyboardUpdate(message.getKey(), keyboards.teachers(message.getValue(), teachers));
        }
    }

    private List<List<String>> generateTeacherLists() {
        // TODO: Problem when there are no teachers -> Try later again
        int elementsPerMessage = 99;

        // Fetching all teachers from the teachers table
        List<String> abbreviations = DB.get().withHandle(handle -> new TeachersSQL(handle).getAll());

        // Removing all teacher names which are too long or contain a plus
        abbreviations.removeIf(teacher -> teacher.length() > 48 || teacher.contains("+") || teacher.contains(","));
        abbreviations.remove("");

        return Lists.partition(abbreviations, elementsPerMessage);
    }

    public boolean isEndingTeachers(String teacher) {
        return teacher.trim().equalsIgnoreCase("fertig");
    }

    //
    // End
    //

    public void finish() {
        teachers.sort(String::compareToIgnoreCase);

        // Removing duplicates from the teachers list
        user.getTeachers().clear();
        teachers.stream()
                .map(String::toLowerCase)
                .distinct()
                .forEach(user.getTeachers()::add);

        user.saveChanges();

        String text = "Herzlichen Glückwunsch! Du hast nun die Einrichtung abgeschlossen. " +
                "Dir werden nun die neusten Stundenplanänderungen für die Klasse " +
                "*" + user.getGrade() + user.getClassChar() + "* ";

        if (teachers.size() > 0) {
            text += "und die Lehrer " + String.join(", ", teachers) + " ";
        }

        text += "geschickt.";

        user.messages().keyboard(text, new ReplyKeyboardRemove());

        // Sending current amendments for the new setup
        // TODO: Test
        TimetableRunnable ttRunable = TimetableBot.getModuleLoader().getRunnable(TimetableRunnable.class);
        if (ttRunable != null) {
            ttRunable.getCachedAmendments().stream()
                    .filter(amendment -> amendment.fits(user))
                    .forEach(amendment -> amendment.send(user));
        }
    }

}
