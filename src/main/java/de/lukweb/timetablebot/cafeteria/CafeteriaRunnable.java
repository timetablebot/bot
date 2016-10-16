package de.lukweb.timetablebot.cafeteria;

import de.lukweb.timetablebot.sql.DB;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CafeteriaRunnable implements Runnable {

    private static List<CafeteriaMeal> meals;

    @Override
    public void run() {
        refresh();
    }

    public static void refresh() {
        CafeteriaDays days = new CafeteriaDays();

        // Fetching from the database
        meals = DB.get().withHandle(handle -> {
            CafeteriaSQL sql = new CafeteriaSQL(handle);

            List<CafeteriaMeal> meals = new ArrayList<>();

            meals.addAll(sql.getMeals(days.today().toEpochDay()));
            meals.addAll(sql.getMeals(days.tomorrow().toEpochDay()));

            return meals;
        });

        if (!meals.isEmpty()) {
            return;
        }

        // If nothing is stored, we fetch it from the web
        meals = new CafeteriaParser().parse();
        DB.get().useHandle(handle -> {
            CafeteriaSQL sql = new CafeteriaSQL(handle);

            sql.saveMeals(meals);
        });
    }

    public static List<CafeteriaMeal> getMeals(LocalDate day) {
        long epochDay = day.toEpochDay();
        return meals.stream()
                .filter(meal -> meal.getEpochDay() == epochDay)
                .collect(Collectors.toList());
    }

}
