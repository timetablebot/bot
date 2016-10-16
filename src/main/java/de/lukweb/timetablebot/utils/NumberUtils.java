package de.lukweb.timetablebot.utils;

public class NumberUtils {

    public static boolean isNumber(String text) {
        for (char c : text.toCharArray()) {
            if (c == ',' || c == '.' || c == '-') continue;
            if (!isNumber(c)) return false;
        }
        return true;
    }

    public static boolean isNumber(char number) {
        return Character.isDigit(number);
    }

}
