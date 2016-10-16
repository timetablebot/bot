package de.lukweb.timetablebot.news;

import de.lukweb.timetablebot.telegram.TelegramUser;
import de.lukweb.timetablebot.telegram.commands.TelegramCommand;
import de.lukweb.timetablebot.timetable.repres.News;
import de.lukweb.timetablebot.timetable.repres.TimetableDay;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public class NewsC extends TelegramCommand {

    public NewsC() {
        super("news", "Neuigkeiten anzeigen");
    }

    @Override
    public void run(Message message, TelegramUser user) {
        List<News> newsList = NewsRunnable.getNews();
        newsList.forEach(news -> user.messages().send(getDayDescription(news.getDay()) + "\n\n" + news.getContent()));
    }

    private String getDayDescription(TimetableDay day) {
        switch (day) {
            case TODAY:
                return "Heutige Nachrichten";
            case TOMORROW:
                return "Morgige Nachrichten";
            default:
                return "";
        }
    }
}
