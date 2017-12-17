package me.dags.massblockr.util;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dags <dags@dags.me>
 */
public class StatCounters {

    public static final AtomicInteger dimVisits = new AtomicInteger(0);
    public static final AtomicLong chunkVisits = new AtomicLong(0);
    public static final AtomicLong schemVisits = new AtomicLong(0);
    public static final AtomicLong blockVisits = new AtomicLong(0);
    public static final AtomicLong blockChanges = new AtomicLong(0);
    public static final AtomicLong entityChanges = new AtomicLong(0);
    public static final AtomicLong tileEntityChanges = new AtomicLong(0);

    public static final AtomicBoolean running = new AtomicBoolean(false);
    public static final AtomicInteger dimTaskTotal = new AtomicInteger(0);
    public static final AtomicInteger dimTasksComplete = new AtomicInteger(0);
    public static final AtomicInteger globalTaskTotal = new AtomicInteger(0);
    public static final AtomicInteger globalTasksComplete = new AtomicInteger(0);

    public static volatile long start = 0L;
    public static volatile long finish = 0L;

    public static void punchIn() {
        start = System.currentTimeMillis();
    }

    public static void punchOut() {
        finish = System.currentTimeMillis();
    }

    public static String numFormat(Number in) {
        String s = NumberFormat.getInstance().format(in);
        if (in instanceof Double) {
            return s.length() > 15 ? s.substring(0, 15) : s;
        }
        return s;
    }
}
