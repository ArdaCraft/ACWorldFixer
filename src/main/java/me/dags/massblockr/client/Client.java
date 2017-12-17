package me.dags.massblockr.client;

import me.dags.massblockr.util.StatCounters;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * @author dags <dags@dags.me>
 */
public interface Client {

    ExecutorService getExecutor();

    void setup();

    void update();

    void finish(File outputDir);

    void setDimension(String dimension);

    void setDimTaskProgress(int tasksComplete, int taskTotal);

    void setGlobalTaskProgress(int tasksComplete, int taskTotal);

    default void start() {
        new Thread(() -> {
            System.out.println();
            System.out.println("Progress:");
            StatCounters.running.set(true);

            while (StatCounters.running.get()) {
                setDimTaskProgress(StatCounters.dimTasksComplete.get(), StatCounters.dimTaskTotal.get());
                setGlobalTaskProgress(StatCounters.globalTasksComplete.get(), StatCounters.globalTaskTotal.get());
                update();
                try {
                    Thread.sleep(300L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
