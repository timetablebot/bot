package de.lukweb.timetablebot.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {

    private static ExecutorService service = Executors.newSingleThreadExecutor(new CatchingThreadFactory("utils"));

    public static void schedule(Runnable runnable) {
        service.execute(runnable);
    }

}
