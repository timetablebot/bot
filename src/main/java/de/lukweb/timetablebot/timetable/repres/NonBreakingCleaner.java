package de.lukweb.timetablebot.timetable.repres;

import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonBreakingCleaner {

    private Elements elements;
    private Logger logger;

    public NonBreakingCleaner(Elements elements) {
        this.elements = elements;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public String text(int index) {
        return clean(elements.get(index).text());
    }

    private String clean(String string) {
        // This char would cause a MySQL error
        char invalidChar = '\uFFFD';
        if (string.indexOf(invalidChar) >= 0) {
            logger.warn("Invalid char in web string '{}' found", string);
        }

        // Replacing invalid characters
        return string.replace('\u00A0', ' ')
                // This would cause a MySQL error
                .replace("\uFFFD", "");
    }
}
