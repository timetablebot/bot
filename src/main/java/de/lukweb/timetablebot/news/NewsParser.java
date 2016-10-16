package de.lukweb.timetablebot.news;

import de.lukweb.timetablebot.config.TimetableBotConfig;
import de.lukweb.timetablebot.timetable.SvplanParser;
import de.lukweb.timetablebot.timetable.repres.News;
import de.lukweb.timetablebot.timetable.repres.TimetableDay;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NewsParser extends SvplanParser {

    public NewsParser(TimetableBotConfig config) {
        super(config.getTableBasePath(), config.getTableAuth());
    }

    public List<News> parse() {
        List<News> news = new ArrayList<>();
        for (TimetableDay day : TimetableDay.values()) {
            Document document = requestHTML(generateUrl(day, 1));
            if (document == null) continue;

            Optional<Element> infoTable = findInfoTable(document);
            if (!infoTable.isPresent()) continue;

            news.add(new News(day, trimTags(infoTable.get())));
        }
        return news;
    }

    private Optional<Element> findInfoTable(Document document) {
        return document.getElementsByClass("info")
                .stream()
                .filter(e -> e.tagName().equalsIgnoreCase("table"))
                .findFirst();
    }

    private String trimTags(Element table) {
        table.select("br").append("n2lb");
        return table.getElementsByClass("info")
                .stream()
                .filter(e -> e.tagName().equalsIgnoreCase("tr"))
                .map(Element::text)
                .skip(1)
                .filter(this::isNeeded)
                .map(s -> s.replaceAll("n2lb", "\n"))
                .collect(Collectors.joining("\n\n"));
    }

    private boolean isNeeded(String text) {
        text = text.toLowerCase();
        return !(text.contains("abwesende") || text.contains("ordnungsdienst"));
    }

}
