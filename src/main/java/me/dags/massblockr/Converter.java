package me.dags.massblockr;

import me.dags.massblockr.client.Client;
import me.dags.massblockr.minecraft.block.Mapper;
import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.minecraft.world.WorldOptions;
import me.dags.massblockr.minecraft.world.dimension.Dimension;
import me.dags.massblockr.minecraft.world.region.Region;
import me.dags.massblockr.util.Mappings;
import me.dags.massblockr.util.StatCounters;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class Converter {

    public static void run(App app, Client client, ConverterOptions options) {
        try {
            WorldOptions optionsIn = options.getInputWorld();
            WorldOptions optionsOut = options.getOutputWorld();
            World worldIn = World.of(optionsIn);
            World worldOut = World.of(optionsOut);
            Mapper mapper = Mappings.load(worldIn, worldOut, options);
            process(client, worldIn, worldOut, mapper);
        } catch (Throwable t) {
            app.onError(t);
        }
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

    private static void processDimension(Client client, World worldOut, Dimension dimension, Mapper mapper) {
        client.setDimension(dimension.getName());

        Dimension dimOut = worldOut.createDimension(dimension.getName());
        List<Callable<Boolean>> tasks = dimension.getRegions()
                .map(region -> new RegionTask(dimOut, region, mapper))
                .collect(Collectors.toList());

        StatCounters.dimTasksComplete.set(0);
        StatCounters.dimTaskTotal.set(tasks.size());

        try {
            client.getExecutor().invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class RegionTask implements Callable<Boolean> {

        private final Dimension dimOut;
        private final Region regionIn;
        private final Mapper mapper;

        private RegionTask(Dimension dimOut, Region regionIn, Mapper mapper) {
            this.dimOut = dimOut;
            this.regionIn = regionIn;
            this.mapper = mapper;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                Region regionOut = dimOut.createRegion(regionIn.getName());
                regionOut.writeChunks(
                        regionIn.getChunks()
                                .map(chunkIn -> dimOut.getWorld().newChunkWorker(chunkIn))
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
}
