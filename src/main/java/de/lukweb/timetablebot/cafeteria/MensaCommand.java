package de.lukweb.timetablebot.cafeteria;

import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class MensaCommand extends TelegramCommand {

    public MensaCommand() {
        super("mensa", "Mensaplan anzeigen", "Zeigt den Mensaplan an");
    }

    @Override
    protected void run(Message message, TelegramUser user) {
        CafeteriaDays days = new CafeteriaDays();

        String dayOneString = mealDayToString(days.today(), days);
        String dayTwoString = mealDayToString(days.tomorrow(), days);

        String caferteriaString = "Die Mensagerichte:\n\n" + dayOneString + "\n\n" + dayTwoString + "\n\n" +
                "Zusatzstoffe: \nhttp://alte-landesschule.de/mensa";
        user.messages().sendNoPreview(caferteriaString);
    }


    private String mealDayToString(LocalDate date, CafeteriaDays days) {
        List<CafeteriaMeal> meals = CafeteriaRunnable.getMeals(date);

        if (meals.isEmpty()) {
            return "Es sind keine Gerichte für " + days.formatLong(date) + " eingetragen.";
        } else {
            String mealsString = meals.stream()
                    .map(this::mealToString)
                    .collect(Collectors.joining("\n"));

            String dateDay = " (_" + days.formatShort(date) + "_)";

            if (date.equals(LocalDate.now())) {
                dateDay = "Heute" + dateDay;
            } else if (date.equals(LocalDate.now().plusDays(1))) {
                dateDay = "Morgen" + dateDay;
            } else {
                dateDay = "Am " + days.formatLong(date);
            }

            return dateDay + " gibt es:\n" + mealsString;
        }
    }

    private String mealToString(CafeteriaMeal meal) {
        // https://emojipedia.org/green-salad/ or https://emojipedia.org/poultry-leg/
        String vegietarian = meal.isVegetarian() ? "\uD83E\uDD57" : "\uD83C\uDF57";
        String price = String.format("%1.2f€", meal.getPrice());

        return vegietarian + " " + meal.getDescription() + " für " + price;
    }

}
