package me.dags.massblockr.client.headless;

import me.dags.massblockr.util.StatCounters;

/**
 * @author dags <dags@dags.me>
 */
public class Stats {

    static void printStats() {
        int dimensions = StatCounters.dimVisits.get();
        long extents = Math.abs(StatCounters.chunkVisits.get());
        long totalBlocks = Math.abs(StatCounters.blockVisits.get());
        long blockChanged = Math.abs(StatCounters.blockChanges.get());
        long entities = Math.abs(StatCounters.entityChanges.get());
        long tileEntities = Math.abs(StatCounters.tileEntityChanges.get());

        Double time = (StatCounters.finish - StatCounters.start) / 1000D;
        Double bps = totalBlocks / time;
        Double tps = StatCounters.globalTasksComplete.get() / time;

        System.out.printf("Dimensions: %s\n", StatCounters.numFormat(dimensions));
        System.out.printf("Regions: %s\n", StatCounters.numFormat(StatCounters.globalTasksComplete.get()));
        System.out.printf("Extents: %s\n", StatCounters.numFormat(extents));
        System.out.printf("Blocks Changed: %s / %s\n", StatCounters.numFormat(blockChanged), StatCounters.numFormat(totalBlocks));
        System.out.printf("Entities: %s\n", StatCounters.numFormat(entities));
        System.out.printf("TileEntities: %s\n", StatCounters.numFormat(tileEntities));
        System.out.printf("Time Taken: %ss\n", StatCounters.numFormat(time));
        System.out.printf("Blocks Per Sec: %s\n", StatCounters.numFormat(bps));
        System.out.printf("Tasks Per Sec: %s\n", StatCounters.numFormat(tps));
    }
}
