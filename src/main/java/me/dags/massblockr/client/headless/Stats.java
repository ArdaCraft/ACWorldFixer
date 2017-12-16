package me.dags.massblockr.client.headless;

/**
 * @author dags <dags@dags.me>
 */
public class Stats {

    static void printStats() {
        int dimensions = me.dags.massblockr.util.Stats.dimVisits.get();
        long extents = Math.abs(me.dags.massblockr.util.Stats.extentVisits.get());
        long totalBlocks = Math.abs(me.dags.massblockr.util.Stats.blockVisits.get());
        long blockChanged = Math.abs(me.dags.massblockr.util.Stats.blockChanges.get());
        long entities = Math.abs(me.dags.massblockr.util.Stats.entityChanges.get());
        long tileEntities = Math.abs(me.dags.massblockr.util.Stats.tileEntityChanges.get());

        Double time = (me.dags.massblockr.util.Stats.finish - me.dags.massblockr.util.Stats.start) / 1000D;
        Double bps = totalBlocks / time;
        Double tps = me.dags.massblockr.util.Stats.globalTasksComplete.get() / time;

        System.out.printf("Dimensions: %s\n", me.dags.massblockr.util.Stats.numFormat(dimensions));
        System.out.printf("Regions: %s\n", me.dags.massblockr.util.Stats.numFormat(me.dags.massblockr.util.Stats.globalTasksComplete.get()));
        System.out.printf("Extents: %s\n", me.dags.massblockr.util.Stats.numFormat(extents));
        System.out.printf("Blocks: %s\n", me.dags.massblockr.util.Stats.numFormat(blockChanged));
        System.out.printf("Entities: %s\n", me.dags.massblockr.util.Stats.numFormat(entities));
        System.out.printf("TileEntities: %s\n", me.dags.massblockr.util.Stats.numFormat(tileEntities));
        System.out.printf("Time Taken: %ss\n", me.dags.massblockr.util.Stats.numFormat(time));
        System.out.printf("Blocks Per Sec: %s\n", me.dags.massblockr.util.Stats.numFormat(bps));
        System.out.printf("Tasks Per Sec: %s\n", me.dags.massblockr.util.Stats.numFormat(tps));
    }
}
