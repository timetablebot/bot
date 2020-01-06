package de.lukweb.timetablebot.timetable;

import de.lukweb.timetablebot.config.TimetableBotConfig;
import de.lukweb.timetablebot.timetable.repres.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TimetableParser extends SvplanParser {

    private Logger logger;
    private Pattern refreshMetaPattern;

    public TimetableParser(TimetableBotConfig config) {
        super(config.getTableBasePath(), config.getTableAuth());
        logger = LoggerFactory.getLogger(getClass());
        refreshMetaPattern = Pattern.compile("subst_([\\d]+).htm");
    }

    public ArrayList<Amendment> parse() {
        HashMap<TimetableDay, ArrayList<Amendment>> amendmentsAtDays = new HashMap<>();
        for (TimetableDay day : TimetableDay.values()) {

            ArrayList<Amendment> amendments = new ArrayList<>();
            long dayTimestamp = -1;
            int page = 1;

            while (true) {
                // TODO: Rethrow error or return null
                Document document = requestHTML(generateUrl(day, page));
                if (document == null) {
                    return null;
                }

                if (page == 1) dayTimestamp = getDate(document);
                List<Amendment> scanned = parseHTML(document, dayTimestamp);
                amendments.addAll(scanned);

                int nextPage = getNextPage(document);
                if (nextPage == 1 || nextPage == -1) break;
                page = nextPage;
            }

            amendmentsAtDays.put(day, amendments);
        }
        ArrayList<Amendment> amendments = new ArrayList<>();
        amendmentsAtDays.values().forEach(amendments::addAll);
        return amendments;
    }

    private List<Amendment> parseHTML(Document document, long date) {
        // If there's no table containing data, we return an empty list
        Elements elements = document.getElementsByClass("mon_list");
        Element table = elements.first();
        if (table == null) return Collections.emptyList();

        // Parse the teacher names and their lessons
        return parseOldTeachers(table, date);
    }

    private List<Amendment> parseOldTeachers(Element table, long date) {
        Elements children = table.children();
        if (children.size() <= 0) return Collections.emptyList();

        Element tbody = children.get(0);
        if (tbody == null) return Collections.emptyList();

        return tbody.children().stream()
                .map(tr -> parseAmendment(tr, date))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Amendment parseAmendment(Element tr, long date) {
        if (tr.classNames().size() < 2) {
            return null;
        }
        if (tr.childNodeSize() < 9) {
            return null;
        }
        NonBreakingCleaner tds = new NonBreakingCleaner(tr.children());

        // If we can't read the grade range, we'll ignore it
        GradeRange grade = new GradeRange(tds.text(5));
        if (grade.hasError()) { // todo throw error
            return null;
        }

        AmendmentBuilder builder = new AmendmentBuilder()
                .setType(getAmendmentType(tds.text(0)))
                .setLesson(tds.text(1))
                .setSubject(tds.text(2))
                .setTeacher(tds.text(3))
                .setReplacementTeacher(tds.text(4))
                .setGrade(grade)
                .setRoom(tds.text(6))
                .setWrittenBy(tds.text(7))
                .setAdditionalInformation(tds.text(8))
                .setDate(date);

        return builder.build();
    }

    private AmendmentType getAmendmentType(String text) {
        AmendmentType type = AmendmentType.getByName(text);
        if (type == null) {
            logger.warn("There is an undefined AmendmentType: {}", text);
            type = AmendmentType.UNDEFINDED;
        }
        return type;
    }

    private int getNextPage(Document document) {
        // First we check for the refresh meta tag
        Elements elements = document.head().getElementsByAttributeValue("http-equiv", "refresh");
        if (elements == null || elements.size() < 1) return -1;
        // Then we get it
        Element metaRefresh = elements.first();
        String refreshContent = metaRefresh.attr("content");
        // And at the end we match the next page string and return it
        Matcher matcher = refreshMetaPattern.matcher(refreshContent);
        if (!matcher.find()) return -1;
        return Integer.parseInt(matcher.group(1));
    }

    private long getDate(Document document) {
        Elements elements = document.body().getElementsByClass("mon_title");
        Element dateDiv = elements.first();
        if (dateDiv == null) return -1;
        String dateStr = dateDiv.text().split(" ")[0];
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d.M.uuuu", Locale.GERMAN);
        return LocalDate.parse(dateStr, dtf).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    }

}
