package me.dags.massblockr.client;

import me.dags.massblockr.util.Stats;

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
            while (Stats.running.get()) {
                setDimTaskProgress(Stats.dimTasksComplete.get(), Stats.dimTaskTotal.get());
                setGlobalTaskProgress(Stats.globalTasksComplete.get(), Stats.globalTaskTotal.get());
                update();
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
