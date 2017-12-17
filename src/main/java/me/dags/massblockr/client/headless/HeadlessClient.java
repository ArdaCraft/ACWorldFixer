package me.dags.massblockr.client.headless;

import me.dags.massblockr.client.Client;
import me.dags.massblockr.util.StatCounters;

import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dags <dags@dags.me>
 */
public class HeadlessClient implements Client {

    private final ExecutorService executorService;
    private final ProgressBar dimProgress = new ProgressBar(35);
    private final ProgressBar overallProgress = new ProgressBar(35);
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
        PrintStream stream = System.out;
        stream.append('\r');
        stream.append("Overall");
        overallProgress.write(stream);
        stream.append(' ');
        stream.append(dimension.get()).append(':').append(' ');
        dimProgress.write(stream);
    }

    @Override
    public void finish(File outputDir) {
        executorService.shutdown();
        System.out.println();
        System.out.println();
        printStats();
        System.out.println();
        System.out.println("Output:");
        System.out.println(outputDir.getAbsoluteFile());
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
        int dimensions = StatCounters.dimVisits.get();
        long extents = Math.abs(StatCounters.chunkVisits.get());
        long totalBlocks = Math.abs(StatCounters.blockVisits.get());
        long blockChanged = Math.abs(StatCounters.blockChanges.get());
        long entities = Math.abs(StatCounters.entityChanges.get());
        long tileEntities = Math.abs(StatCounters.tileEntityChanges.get());

        Double time = (StatCounters.finish - StatCounters.start) / 1000D;
        Double bps = totalBlocks / time;
        Double tps = StatCounters.globalTasksComplete.get() / time;

        System.out.println("Stats:");
        System.out.printf("Dimensions: %s\n", StatCounters.numFormat(dimensions));
        System.out.printf("Regions: %s\n", StatCounters.numFormat(StatCounters.globalTasksComplete.get()));
        System.out.printf("Extents: %s\n", StatCounters.numFormat(extents));
        System.out.printf("Blocks Changed: %s\n", StatCounters.numFormat(blockChanged));
        System.out.printf("Blocks Visited: %s\n", StatCounters.numFormat(totalBlocks));
        System.out.printf("Entities: %s\n", StatCounters.numFormat(entities));
        System.out.printf("TileEntities: %s\n", StatCounters.numFormat(tileEntities));
        System.out.printf("Time Taken: %ss\n", StatCounters.numFormat(time));
        System.out.printf("Blocks Per Sec: %s\n", StatCounters.numFormat(bps));
        System.out.printf("Tasks Per Sec: %s\n", StatCounters.numFormat(tps));
    }
}
