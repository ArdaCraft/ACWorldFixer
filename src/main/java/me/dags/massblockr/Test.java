package me.dags.massblockr;

import me.dags.massblockr.client.Client;
import me.dags.massblockr.client.headless.HeadlessClient;
import me.dags.massblockr.minecraft.block.Mapper;
import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.minecraft.world.WorldOptions;
import me.dags.massblockr.minecraft.world.dimension.Dimension;
import me.dags.massblockr.minecraft.world.region.Region;
import me.dags.massblockr.util.Mappings;
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

        World input = World.of(in, World.LEGACY_SCHEMA);
        World output = World.of(out, World.FUTURE_SCHEMA);
        Mapper mapper = Mappings.load(input, output, new File("src/main/resources/mappings.txt"));

        System.out.println("Mappings:");
        mapper.print(System.out);

        Client client = new HeadlessClient(6);
        process(client, input, output, mapper);
    }

    private static void process(Client client, World worldIn, World worldOut, Mapper mappings) {
        StatCounters.running.set(true);
        client.setup();
        client.start();

        try {
            StatCounters.punchIn();
            Collection<Dimension> dimensions = worldIn.getDimensions();
            StatCounters.globalTaskTotal.set(worldIn.getTaskCount());

            for (Dimension dimension : dimensions) {
                try {
                    processDimension(client, worldOut, dimension, mappings);
                } finally {
                    StatCounters.dimVisits.incrementAndGet();
                }
            }

            StatCounters.punchOut();

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            StatCounters.running.set(false);
            client.finish(worldOut.getDirectory());
        }
    }

    private static Callable<Boolean> task(Dimension dimOut, Region regionIn, Mapper mapper) {
        return () -> processRegion(dimOut, regionIn, mapper);
    }

    private static void processDimension(Client client, World worldOut, Dimension dimension, Mapper mapper) {
        client.setDimension(dimension.getName());

        Dimension dimOut = worldOut.createDimension(dimension.getName());
        List<Callable<Boolean>> tasks = dimension.getRegions()
                .map(region -> task(dimOut, region, mapper))
                .collect(Collectors.toList());

        StatCounters.dimTasksComplete.set(0);
        StatCounters.dimTaskTotal.set(tasks.size());

        try {
            client.getExecutor().invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean processRegion(Dimension dimOut, Region regionIn, Mapper mapper) {
        try {
            Region regionOut = dimOut.createRegion(regionIn.getName());
            regionOut.writeChunks(regionIn.getChunks()
                    .map(chunkIn -> dimOut.getWorld().chunkWorker(chunkIn))
                    .map(worker -> worker.apply(mapper).getOutput())
            );
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        } finally {
            StatCounters.dimTasksComplete.incrementAndGet();
            StatCounters.globalTasksComplete.incrementAndGet();
        }
    }
}
