package de.lukweb.timetablebot.news;

import de.lukweb.timetablebot.config.TimetableBotConfig;
import de.lukweb.timetablebot.timetable.repres.News;

import java.util.List;

public class NewsRunnable implements Runnable {

    private static List<News> news;

    private TimetableBotConfig config;

    public NewsRunnable(TimetableBotConfig config) {
        super();

        this.config = config;
    }

    @Override
    public void run() {
        news = new NewsParser(config).parse();
    }

    public static List<News> getNews() {
        return news;
    }
}
