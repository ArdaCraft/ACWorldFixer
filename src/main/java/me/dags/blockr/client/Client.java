package me.dags.blockr.client;

import me.dags.blockr.task.ChangeStats;

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
            while (ChangeStats.running.get()) {
                setDimTaskProgress(ChangeStats.dimTasksComplete.get(), ChangeStats.dimTaskTotal.get());
                setGlobalTaskProgress(ChangeStats.globalTasksComplete.get(), ChangeStats.globalTaskTotal.get());
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
