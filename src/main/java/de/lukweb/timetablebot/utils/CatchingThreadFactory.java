package de.lukweb.timetablebot.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatchingThreadFactory implements ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatchingThreadFactory.class);

    private String namePrefix;
    private AtomicInteger threadNumber;
    private ThreadGroup threadGroup;

    public CatchingThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
        this.threadNumber = new AtomicInteger(1);
        this.threadGroup = Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(threadGroup, r);
        thread.setName("pool-" + namePrefix + "-" + threadNumber.incrementAndGet());
        thread.setUncaughtExceptionHandler((th, ex) -> LOGGER.error("Error in thread " + th.getName(), ex));
        return thread;
    }
}
