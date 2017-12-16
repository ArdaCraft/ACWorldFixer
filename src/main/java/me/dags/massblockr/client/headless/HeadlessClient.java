package me.dags.massblockr.client.headless;

import me.dags.massblockr.client.Client;
import me.dags.massblockr.util.Stats;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dags <dags@dags.me>
 */
public class HeadlessClient implements Client {

    private final ExecutorService executorService;
    private final ProgressBar dimProgress = new ProgressBar(25);
    private final ProgressBar overallProgress = new ProgressBar(25);
    private final AtomicReference<String> dimension = new AtomicReference<>("Dim");

    public HeadlessClient(int coreCount) {
        executorService = Executors.newFixedThreadPool(coreCount);
    }

    @Override
    public ExecutorService getExecutor() {
        return executorService;
    }

    @Override
    public void setup() {

    }

    @Override
    public void update() {
        StringBuilder builder = new StringBuilder(128);
        builder.append('\r');

        builder.append("Overall: ");
        overallProgress.appendTo(builder);

        builder.append(dimension.get()).append(": ");
        dimProgress.appendTo(builder);

        System.out.print(builder.toString());
    }

    @Override
    public void finish(File outputDir) {
        executorService.shutdown();
        System.out.println();
        System.out.println();

        printStats();

        System.out.println();
        System.out.println();
        System.out.println(outputDir);
    }

    @Override
    public void setDimension(String dimension) {
        this.dimension.set(dimension);
    }

    @Override
    public void setDimTaskProgress(int tasksComplete, int taskTotal) {
        dimProgress.setProgress(tasksComplete, taskTotal);
    }

    @Override
    public void setGlobalTaskProgress(int tasksComplete, int taskTotal) {
        overallProgress.setProgress(tasksComplete, taskTotal);
    }

    private static void printStats() {
        int dimensions = Stats.dimVisits.get();
        long extents = Math.abs(Stats.extentVisits.get());
        long totalBlocks = Math.abs(Stats.blockVisits.get());
        long blockChanged = Math.abs(Stats.blockChanges.get());
        long entities = Math.abs(Stats.entityChanges.get());
        long tileEntities = Math.abs(Stats.tileEntityChanges.get());

        Double time = (Stats.finish - Stats.start) / 1000D;
        Double bps = totalBlocks / time;
        Double tps = Stats.globalTasksComplete.get() / time;

        System.out.printf("Dimensions: %s\n", Stats.numFormat(dimensions));
        System.out.printf("Regions: %s\n", Stats.numFormat(Stats.globalTasksComplete.get()));
        System.out.printf("Extents: %s\n", Stats.numFormat(extents));
        System.out.printf("Blocks: %s\n", Stats.numFormat(blockChanged));
        System.out.printf("Entities: %s\n", Stats.numFormat(entities));
        System.out.printf("TileEntities: %s\n", Stats.numFormat(tileEntities));
        System.out.printf("Time Taken: %ss\n", Stats.numFormat(time));
        System.out.printf("Blocks Per Sec: %s\n", Stats.numFormat(bps));
        System.out.printf("Tasks Per Sec: %s\n", Stats.numFormat(tps));
    }
}
