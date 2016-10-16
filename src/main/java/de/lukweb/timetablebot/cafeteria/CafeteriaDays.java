package de.lukweb.timetablebot.cafeteria;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CafeteriaDays {

    private DateTimeFormatter formatterLong;
    private DateTimeFormatter formatterShort;
    private LocalDate baseDate;
    private LocalDate nextDate;

    public CafeteriaDays() {
        this.formatterLong = DateTimeFormatter
                .ofPattern("eeee 'den' dd.MM.")
                .withLocale(Locale.GERMAN);
        this.formatterShort = DateTimeFormatter
                .ofPattern("dd.MM.")
                .withLocale(Locale.GERMAN);
        this.baseDate = notWeekend(LocalDate.now());
        this.nextDate = notWeekend(baseDate.plusDays(1));
    }

    private LocalDate notWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        } else {
            return date;
        }
    }

    public LocalDate today() {
        return baseDate;
    }

    public LocalDate tomorrow() {
        return nextDate;
    }

    public String formatLong(LocalDate date) {
        return formatterLong.format(date);
    }

    public String formatShort(LocalDate date) {
        return formatterShort.format(date);
    }

}
