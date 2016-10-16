package de.lukweb.timetablebot.cafeteria;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CafeteriaParser {

    private final static String WEBSITE = "http://alte-landesschule.de/mensa";
    private final static Pattern TIME_PATTERN = Pattern.compile(
            "die Woche vom (\\d+).(\\d*).?\\s*-\\s*(?:\\d+).(\\d+).(\\d+)");
    private final static Pattern ADDITIVES_PATTERN = Pattern.compile(
            "(?:\\d| \\d|,\\d| ,\\d)([, 0-9a-i]+)");
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "(\\d+),(\\d+) *€");

    // TODO: Problem with Montag and Dienstag

    private Logger logger;

    public CafeteriaParser() {
        logger = LoggerFactory.getLogger(getClass());
    }

    public List<CafeteriaMeal> parse() {
        Document request = request();
        if (request == null) {
            return Collections.emptyList();
        }

        return parse(request);
    }

    private Document request() {
        try {
            return Jsoup.connect(WEBSITE).get();
        } catch (IOException ex) {
            logger.warn("Can't request the cafeteria plan", ex);
            return null;
        }
    }

    private List<CafeteriaMeal> parse(Document document) {
        LocalDate date = matchTime(document.html());
        if (date == null) {
            return Collections.emptyList();
        }

        Elements tbody = document.getElementsByTag("tbody");
        if (tbody.size() < 3) {
            logger.warn("Too few tbody's on the webpage. Expected {}, got {}", 3, tbody.size());
            return Collections.emptyList();
        }

        List<CafeteriaMeal> allMeals = new ArrayList<>();

        Element wantedTable = tbody.get(1);
        Elements tableRows = wantedTable.children();

        DayOfWeek dayEntryBefore = null;
        for (Element tr : tableRows) {

            List<CafeteriaMeal> meals = parseDayRow(tr, date, dayEntryBefore);
            if (meals.size() > 0) {
                dayEntryBefore = meals.get(meals.size() - 1).getDay().getDayOfWeek();
            }

            allMeals.addAll(meals);
        }

        if (allMeals.size() != 10) {
            logger.warn("There should be 10 meals this week, but only {} meals are parsed", allMeals.size());
        }

        return allMeals;
    }

    private LocalDate matchTime(String html) {
        Matcher matcher = TIME_PATTERN.matcher(html);

        if (!matcher.find()) {
            logger.warn("Can't parse the starting date.");
            return null;
        }

        String startDay = matcher.group(1);
        String startMonth = matcher.group(2);
        if (startMonth.trim().isEmpty()) {
            startMonth = matcher.group(3);
        }
        String startYear = matcher.group(4);

        LocalDate date;
        try {
            date = LocalDate.of(Integer.parseInt(startYear), Integer.parseInt(startMonth), Integer.parseInt(startDay));
        } catch (NumberFormatException ex) {
            logger.warn("Can't parse a date string to a number", ex);
            return null;
        }

        if (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            int dayDifference = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            date = date.minusDays(dayDifference);
        }

        return date;
    }

    private List<CafeteriaMeal> parseDayRow(Element tr, LocalDate baseDate, DayOfWeek before) {
        Elements tableData = tr.children();

        DayOfWeek dayOfWeek;
        if (tableData.size() >= 3) {
            String dayOfWeekString = tableData.get(0).text();
            dayOfWeek = findDayOfTheWeek(dayOfWeekString);

            if (dayOfWeek == null) {
                logger.warn("Can't find a DayOfWeek for the german day: '{}'", dayOfWeekString);
                return Collections.emptyList();
            }
        } else {
            if (before == null) {
                logger.warn("The DayOfWeek of the entry before this is null");
                return Collections.emptyList();
            }

            dayOfWeek = before;
        }

        LocalDate date = baseDate.plusDays(dayOfWeek.getValue() - 1);
        List<CafeteriaMeal> meals = parseMealRows(tableData, date);

        /*
        These checks doesn't work anymore, because of the two tr per day
         if (meals.size() > 2) {
            logger.warn("There are more than two meals in the meals list for {}", date.toString());
        } else if (meals.size() < 2) {
            logger.warn("There are fewer than two meals in the meals list for {}", date.toString());
        } else {
            if (meals.get(0).isVegetarian() == meals.get(1).isVegetarian()) {
                logger.warn("Both meals of the day {} are vegetarian or not", date.toString());
            }
        } */

        return meals;
    }

    private DayOfWeek findDayOfTheWeek(String string) {
        switch (string.toLowerCase().trim()) {
            case "montag":
                return DayOfWeek.MONDAY;
            case "dienstag":
                return DayOfWeek.TUESDAY;
            case "mittwoch":
                return DayOfWeek.WEDNESDAY;
            case "donnerstag":
                return DayOfWeek.THURSDAY;
            case "freitag":
                return DayOfWeek.FRIDAY;
            default:
                return null;
        }
    }

    private List<CafeteriaMeal> parseMealRows(Elements tableData, LocalDate date) {
        List<CafeteriaMeal> meals = new ArrayList<>();
        Elements pElements = tableData.get(tableData.size() - 2).children();
        int mealCounter = 0;
        List<String> textNodesBefore = new ArrayList<>();


        for (int i = 0; i < pElements.size(); i++) {
            Element pNode = pElements.get(i);
            String text = pNode.text();

            Matcher matcher = ADDITIVES_PATTERN.matcher(text);
            if (matcher.find()) {
                StringBuilder description = new StringBuilder(String.join(" ", textNodesBefore));
                if (description.length() > 0) {
                    description.append(" ");
                }
                description.append(text, 0, matcher.start());

                int mealNumber = mealCounter++;

                String priceText = tableData.get(tableData.size() - 1).children().get(mealNumber).text();
                double price = priceStringToNumber(priceText);
                if (price < 0) {
                    continue;
                }

                CafeteriaMeal meal = new CafeteriaMeal(date, price <= 3.5, description.toString(), price);
                meals.add(meal);
            } else {
                textNodesBefore.add(text);
            }
        }

        return meals;
    }

    private double priceStringToNumber(String string) {
        Matcher matcher = PRICE_PATTERN.matcher(string.trim());
        if (!matcher.matches()) {
            logger.warn("The price '{}' doesn't matches the pattern", string);
            return -1;
        }

        String combinedPriceString = matcher.group(1) + "." + matcher.group(2);
        try {
            return Double.parseDouble(combinedPriceString);
        } catch (NumberFormatException ex) {
            logger.warn("The combinded price string '{}' can't be parsed into a double", combinedPriceString);
            return -1;
        }
    }

}
