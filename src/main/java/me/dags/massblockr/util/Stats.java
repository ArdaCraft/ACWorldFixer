package me.dags.massblockr.util;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dags <dags@dags.me>
 */
public class Stats {

    public static final AtomicInteger dimVisits = new AtomicInteger(0);
    public static final AtomicLong extentVisits = new AtomicLong(0);
    public static final AtomicLong blockVisits = new AtomicLong(0);
    public static final AtomicLong blockChanges = new AtomicLong(0);
    public static final AtomicLong entityChanges = new AtomicLong(0);
    public static final AtomicLong tileEntityChanges = new AtomicLong(0);

    public static final AtomicBoolean running = new AtomicBoolean(true);
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

    public static void incDimensionCount() {
        dimVisits.getAndAdd(1);
    }

    public static void incExtentCount() {
        extentVisits.addAndGet(1);
    }

    public static void incBlockVisits(long count) {
        blockVisits.getAndAdd(count);
    }

    public static void incBlockChanges() {
        blockChanges.getAndAdd(1);
    }

    public static void incEntityChanges() {
        entityChanges.getAndAdd(1);
    }

    public static void incTileEntityChanges() {
        tileEntityChanges.getAndAdd(1);
    }

    public static String numFormat(Number in) {
        String s = NumberFormat.getInstance().format(in);
        if (in instanceof Double) {
            return s.length() > 15 ? s.substring(0, 15) : s;
        }
        return s;
    }
}
