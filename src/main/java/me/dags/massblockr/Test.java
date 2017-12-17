package me.dags.massblockr;

import me.dags.massblockr.client.Client;
import me.dags.massblockr.client.headless.HeadlessClient;
import me.dags.massblockr.mapper.Mapper;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.world.LegacyWorld;
import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.minecraft.world.WorldOptions;
import me.dags.massblockr.minecraft.world.chunk.Chunk;
import me.dags.massblockr.minecraft.world.dimension.Dimension;
import me.dags.massblockr.minecraft.world.region.Region;
import me.dags.massblockr.minecraft.world.volume.VolumeWorker;
import me.dags.massblockr.util.StatCounters;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class Test {

    public static void main(String[] args) throws IOException {
        WorldOptions in = new WorldOptions();
        in.setDirectory(new File("world"));
        in.setRegistry(new File("src/main/resources/registry.json"));
        in.setLevelData(new File("src/main/resources/level.dat"));

        WorldOptions out = new WorldOptions(true);
        out.setDirectory(new File("world-converted"));
        out.setRegistry(new File("src/main/resources/registry.json"));
        out.setLevelData(new File("src/main/resources/level.dat"));

        World input = new LegacyWorld(in);
        World output = new LegacyWorld(out);

        Client client = new HeadlessClient(6);
        process(client, input, output);
    }

    private static void process(Client client, World in, World out) {
        StatCounters.running.set(true);
        client.setup();
        client.start();

        BlockState match = in.getRegistry().getState("minecraft:stone[variant=diorite]");
        BlockState replace = out.getRegistry().getState("minecraft:stone[variant=andesite]");
        Mapper mapper = Mapper.single(match, replace);

        StatCounters.punchIn();
        Collection<Dimension> dimensions = in.getDimensions();
        StatCounters.globalTaskTotal.set(in.getTaskCount());

        for (Dimension dimension : dimensions) {
            Dimension dimOut = out.createDimension(dimension.getName());
            List<Callable<Boolean>> tasks = dimension.getRegions()
                    .map(region -> task(out, dimOut, region, mapper))
                    .collect(Collectors.toList());

            StatCounters.dimTasksComplete.set(0);
            StatCounters.dimTaskTotal.set(tasks.size());

            try {
                client.getExecutor().invokeAll(tasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            StatCounters.dimVisits.incrementAndGet();
        }

        StatCounters.punchOut();

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        StatCounters.running.set(false);
        client.finish(out.getDirectory());
    }

    private static Callable<Boolean> task(World worldOut, Dimension dimOut, Region regionIn, Mapper mapper) {
        return () -> {
            processRegion(worldOut, dimOut, regionIn, mapper);
            return true;
        };
    }

    private static void processRegion(World worldOut, Dimension dimOut, Region regionIn, Mapper mapper) {
        Region regionOut = dimOut.createRegion(regionIn.getName());

        regionOut.writeChunks(regionIn.getChunks()
                .map(chunkIn -> VolumeWorker.newChunkWorker(worldOut, chunkIn))
                .map(worker -> work(worker, mapper))
        );

        StatCounters.dimTasksComplete.incrementAndGet();
        StatCounters.globalTasksComplete.incrementAndGet();
    }

    private static Chunk work(VolumeWorker<Chunk> worker, Mapper mapper) {
        for (int section = 0; section < worker.getSectionCount(); section++) {
            for (int block = 0; block < worker.getSectionSize(); block++) {
                BlockState in = worker.getState(section, block);
                BlockState out = mapper.map(in);
                worker.setState(section, block, out);

                StatCounters.blockVisits.incrementAndGet();
                if (in != out) {
                    StatCounters.blockChanges.incrementAndGet();
                }
            }
        }
        StatCounters.incExtentCount();
        return worker.getOutput();
    }
}
