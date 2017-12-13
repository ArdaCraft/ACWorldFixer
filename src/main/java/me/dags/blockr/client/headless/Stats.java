package me.dags.blockr.client.headless;

import me.dags.blockr.task.ChangeStats;

/**
 * @author dags <dags@dags.me>
 */
public class Stats {

    static void printStats() {
        int dimensions = ChangeStats.dimVisits.get();
        long extents = Math.abs(ChangeStats.extentVisits.get());
        long totalBlocks = Math.abs(ChangeStats.blockVisits.get());
        long blockChanged = Math.abs(ChangeStats.blockChanges.get());
        long entities = Math.abs(ChangeStats.entityChanges.get());
        long tileEntities = Math.abs(ChangeStats.tileEntityChanges.get());

        Double time = (ChangeStats.finish - ChangeStats.start) / 1000D;
        Double bps = totalBlocks / time;
        Double tps = ChangeStats.globalTasksComplete.get() / time;

        System.out.printf("Dimensions: %s\n", ChangeStats.numFormat(dimensions));
        System.out.printf("Regions: %s\n", ChangeStats.numFormat(ChangeStats.globalTasksComplete.get()));
        System.out.printf("Extents: %s\n", ChangeStats.numFormat(extents));
        System.out.printf("Blocks: %s\n", ChangeStats.numFormat(blockChanged));
        System.out.printf("Entities: %s\n", ChangeStats.numFormat(entities));
        System.out.printf("TileEntities: %s\n", ChangeStats.numFormat(tileEntities));
        System.out.printf("Time Taken: %ss\n", ChangeStats.numFormat(time));
        System.out.printf("Blocks Per Sec: %s\n", ChangeStats.numFormat(bps));
        System.out.printf("Tasks Per Sec: %s\n", ChangeStats.numFormat(tps));
    }
}
