package de.lukweb.timetablebot.timetable.repres;

import org.jsoup.select.Elements;

public class NonBreakingCleaner {

    private Elements elements;

    public NonBreakingCleaner(Elements elements) {
        this.elements = elements;
    }

    public String text(int index) {
        return clean(elements.get(index).text());
    }

    public static String clean(String string) {
        // Replacing invalid characters
        return string.replace('\u00A0', ' ')
                // This would cause a MySQL error
                .replace("\uFFFD", "");
    }
}
