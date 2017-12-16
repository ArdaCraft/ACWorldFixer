package me.dags.massblockr;

import me.dags.massblockr.mapper.Mapper;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.world.LegacyWorld;
import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.minecraft.world.WorldOptions;
import me.dags.massblockr.minecraft.world.chunk.Chunk;
import me.dags.massblockr.minecraft.world.dimension.Dimension;
import me.dags.massblockr.minecraft.world.dimension.RegionFile;
import me.dags.massblockr.minecraft.world.volume.VolumeWorker;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

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

        process(input, output);
    }

    private static void process(World in, World out) {
        BlockState match = in.getRegistry().getState("minecraft:stone[variant=diorite]");
        BlockState replace = out.getRegistry().getState("minecraft:stone[variant=andesite]");
        Mapper mapper = Mapper.single(match, replace);

        Collection<Dimension> dimensions = in.getDimensions();
        for (Dimension dimIn : dimensions) {
            Dimension dimOut = out.createDimension(dimIn.getName());
            dimIn.getRegions().forEach(regionIn -> {
                RegionFile regionOut = dimOut.createRegion(regionIn.getName());
                regionOut.writeChunks(regionIn.getChunks()
                        .map(chunkIn -> VolumeWorker.newChunkWorker(out, chunkIn))
                        .map(worker -> work(worker, mapper)));
            });
        }
    }

    private static Chunk work(VolumeWorker<Chunk> worker, Mapper mapper) {
        for (int section = 0; section < worker.getMaxSectionIndex(); section++) {
            for (int block = 0; block < worker.getMaxBlockIndex(); block++) {
                BlockState state = worker.getState(section, block);
                state = mapper.map(state);
                worker.setState(section, block, state);
            }
        }
        return worker.getOutput();
    }
}
