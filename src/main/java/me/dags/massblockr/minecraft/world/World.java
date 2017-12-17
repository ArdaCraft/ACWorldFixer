package me.dags.massblockr.minecraft.world;

import com.google.common.collect.ImmutableList;
import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.minecraft.registry.Registry;
import me.dags.massblockr.minecraft.world.chunk.Chunk;
import me.dags.massblockr.minecraft.world.chunk.LegacyChunk;
import me.dags.massblockr.minecraft.world.chunk.NewChunk;
import me.dags.massblockr.minecraft.world.dimension.Dimension;
import me.dags.massblockr.minecraft.world.dimension.WorldDimension;
import me.dags.massblockr.minecraft.world.volume.VolumeWorker;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public interface World {

    int LEGACY_SCHEMA = 0;
    int FUTURE_SCHEMA = 1;

    int getSchema();

    File getDirectory();

    Registry getRegistry();

    List<Dimension> getDimensions();

    Dimension createDimension(String name);

    default int getTaskCount() {
        int count = 0;
        for (Dimension dimension : getDimensions()) {
            count += dimension.getRegionCount();
        }
        return count;
    }

    default Chunk readChunk(int x, int z, CompoundTag data) {
        switch (getSchema()) {
            case World.LEGACY_SCHEMA:
                return new LegacyChunk(this, x, z, data);
            case World.FUTURE_SCHEMA:
                return new NewChunk(this, x, z, data);
            default:
                throw new IllegalStateException("Invalid world schema: " + getSchema());
        }
    }

    default VolumeWorker<Chunk> chunkWorker(Chunk chunkIn) {
        switch (getSchema()) {
            case World.LEGACY_SCHEMA:
                return new VolumeWorker<>(chunkIn, LegacyChunk.createNewChunk(this, chunkIn));
            case World.FUTURE_SCHEMA:
                return new VolumeWorker<>(chunkIn, NewChunk.createNewChunk(this, chunkIn));
            default:
                return null;
        }
    }

    static List<Dimension> findDimensions(World world) {
        File[] files = world.getDirectory().listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        ImmutableList.Builder<Dimension> builder = ImmutableList.builder();
        builder.add(new WorldDimension(world));

        for (File file : files) {
            if (!Dimension.isValidDimension(file)) {
                continue;
            }
            builder.add(new WorldDimension(world, file));
        }
        return builder.build();
    }

    static World of(WorldOptions options, int schema) throws IOException {
        return new WorldImpl(options, schema);
    }
}
