package de.lukweb.timetablebot.cafeteria;

import org.junit.Test;

import java.util.List;

public class CafeteriaParserTests {

    @Test
    public void testParser() {
        CafeteriaParser parser = new CafeteriaParser();
        List<CafeteriaMeal> meals = parser.parse();

        for (CafeteriaMeal meal : meals) {
            System.out.printf("Meal at %d: %s for %f. Veg? %b\n",
                    meal.getEpochDay(), meal.getDescription(), meal.getPrice(), meal.isVegetarian());
        }
    }

}
