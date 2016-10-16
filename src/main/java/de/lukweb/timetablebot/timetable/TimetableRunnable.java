package de.lukweb.timetablebot.timetable;

import de.lukweb.timetablebot.config.TimetableBotConfig;
import de.lukweb.timetablebot.sql.DB;
import de.lukweb.timetablebot.telegram.TelegramRank;
import de.lukweb.timetablebot.telegram.Users;
import de.lukweb.timetablebot.timetable.repres.Amendment;
import de.lukweb.timetablebot.timetable.sql.AmendmentSQL;
import de.lukweb.timetablebot.timetable.sql.TeachersSQL;
import de.lukweb.timetablebot.utils.CatchingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TimetableRunnable implements Runnable {

    private Logger logger;
    private TimetableBotConfig config;
    private List<Amendment> amendments;
    private ExecutorService sqlExecutorService;
    private ExecutorService telegramExecutorService;
    private int ameCountBefore;

    public TimetableRunnable(TimetableBotConfig config) {
        super();
        this.config = config;

        logger = LoggerFactory.getLogger(getClass());
        amendments = DB.get().withHandle(handle -> new AmendmentSQL(handle).getLastTwoDays());
        sqlExecutorService = Executors.newFixedThreadPool(2, new CatchingThreadFactory("mysql"));
        telegramExecutorService = Executors.newFixedThreadPool(2, new CatchingThreadFactory("telegram"));
        // TODO: Why double call at the start?
    }

    public List<Amendment> getCachedAmendments() {
        return amendments;
    }

    @Override
    public void run() {
        List<Amendment> currAme = new TimetableParser(config).parse();
        List<Amendment> newAme = new ArrayList<>();
        List<Amendment> removedAme = new ArrayList<>();

        if (currAme == null) {
            // There was an error while reading the timetable
            return;
        }

        // Inform the admin if no amendments were found
        if (currAme.isEmpty()) {
            Users.getAll()
                    .stream()
                    .filter(user -> user.getRank().equals(TelegramRank.ADMIN))
                    .forEach(admin -> telegramExecutorService.execute(() -> {
                        admin.messages().send("Es wurde keine einzige Vertretung gefunden!");
                    }));
            return;
        }

        // Alert the admin if there are fewer amendments; should be only once per day
        if (currAme.size() < ameCountBefore) {
            telegramExecutorService.execute(() -> {
                String message = "Fewer amendments than before " + ameCountBefore + " -> " + currAme.size();
                Users.get(154988148).messages().send(message);
            });
        }
        ameCountBefore = currAme.size();

        // Figure out the start date to load missing amendments
        OptionalLong startDateOptional = currAme.stream()
                .mapToLong(Amendment::getDate)
                .min();
        long startDate = startDateOptional.orElse(Long.MAX_VALUE);
        loadMissingAmendments(startDate);

        // Filtering the new amendments
        currAme.stream().filter(a -> !amendments.contains(a)).forEach(newAme::add);

        // Filtering the removed amendments
        amendments.stream()
                .filter(a -> a.getDate() >= startDate)
                .filter(a -> !findSimilarAmendments(currAme, a))
                .forEach(removedAme::add);

        // Logging the changes
        String logMsg = currAme.size() + " scanned amendment(s)";
        if (newAme.size() > 0) {
            logMsg += " - " + newAme.size() + " new amendment(s)";
        }
        if (removedAme.size() > 0) {
            logMsg += " - " + removedAme.size() + " removed amendment(s)";
        }
        logger.info(logMsg);

        // Adding all amendments to the local cache
        amendments.addAll(newAme);

        // Syncing the database (add & remove amendments)
        sqlExecutorService.execute(() -> DB.get().useHandle(handle -> {
            AmendmentSQL sql = new AmendmentSQL(handle);
            newAme.forEach(sql::insert);
            removedAme.forEach(sql::delete);
        }));
        // Update the teachers
        sqlExecutorService.execute(() -> {
            List<String> teachers = newAme.stream().map(Amendment::getTeacher).collect(Collectors.toList());
            DB.get().useHandle(handle -> new TeachersSQL(handle).addMissingTeachers(teachers));
        });

        // Sending all changes to users
        newAme.forEach(a -> telegramExecutorService.execute(() -> Users.notify(a, false)));
        // removedAme.forEach(a -> telegramExecutorService.execute(() -> Users.notify(a, true)));

        // Send all removals to me for review
        removedAme.forEach(amendment -> telegramExecutorService.execute(() ->
                amendment.sendRemove(Users.get(154988148))));
        amendments.removeAll(removedAme);

        cleanupCache(startDate);
    }

    private void loadMissingAmendments(Long date) {
        List<Amendment> databaseAmendments = DB.get()
                .withHandle(handle -> new AmendmentSQL(handle).getAllFromDate(date));

        int sizeBefore = amendments.size();

        databaseAmendments.stream()
                .filter(a -> !amendments.contains(a))
                .forEach(amendments::add);

        int sizeAfter = amendments.size();
        if (sizeAfter > sizeBefore) {
            logger.info("Loaded {} amendment(s) from the database", sizeAfter - sizeBefore);
        }
    }

    private void cleanupCache(long date) {
        // Remove amendment which date is lower than the removeDate
        // Also count the number of removed elements
        int elementsBefore = amendments.size();
        amendments.removeIf(amendment -> amendment.getDate() < date);

        int removedCount = elementsBefore - amendments.size();
        if (removedCount > 0) {
            logger.info("Removed {} amendment(s) from the cache ({} elements before)", removedCount, elementsBefore);
        }
    }

    private boolean findSimilarAmendments(List<Amendment> source, Amendment search) {

        for (Amendment amendment : source) {
            if (amendment.equals(search)) {
                return true;
            }

            if (!amendment.getSubject().equalsIgnoreCase(search.getSubject())) {
                continue;
            }

            if (!amendment.getGrade().equals(search.getGrade())) {
                continue;
            }

            if (!search.getParsedLesson().contains(amendment.getParsedLesson())) {
                continue;
            }

            return true;
        }

        return false;
    }

    public void stop() {
        sqlExecutorService.shutdown();
        telegramExecutorService.shutdown();
        try {
            sqlExecutorService.awaitTermination(15, TimeUnit.SECONDS);
            telegramExecutorService.awaitTermination(15, TimeUnit.SECONDS);
            if (sqlExecutorService.isTerminated() && telegramExecutorService.isTerminated()) return;
        } catch (InterruptedException ex) {
            logger.error("Interrupted while stopping executor services", ex);
        }
        sqlExecutorService.shutdownNow();
        telegramExecutorService.shutdownNow();
    }

}
