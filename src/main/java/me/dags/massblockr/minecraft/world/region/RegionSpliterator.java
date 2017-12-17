package me.dags.massblockr.minecraft.world.region;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.minecraft.world.chunk.Chunk;
import me.dags.massblockr.util.FileUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class RegionSpliterator implements Spliterator<Chunk> {

    private int x = 0;
    private int z = -1;
    private int startZ, endX, endZ, size;
    private final RegionFile region;

    public RegionSpliterator(RegionFile regionFile) {
        this(regionFile, 0, -1, 32, 32);
    }

    private RegionSpliterator(RegionFile region, int x, int z, int endX, int endZ) {
        this.region = region;
        this.x = x;
        this.z = z;
        this.startZ = z;
        this.endX = endX;
        this.endZ = endZ;
        this.size = (endX - x) * (endZ * z);
    }

    @Override
    public boolean tryAdvance(Consumer<? super Chunk> action) {
        if (++z >= endZ) {
            z = startZ + 1;
            if (++x >= endX) {
                return false;
            }
        }

        DataInputStream in = region.getRegionFile().getChunkDataInputStream(x, z);
        if (in != null) {
            try {
                CompoundTag tag = (CompoundTag) FileUtils.readNBT(in);
                Chunk chunk = region.getWorld().createChunk(x, z, tag);
                action.accept(chunk);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tryAdvance(action);
    }

    @Override
    public Spliterator<Chunk> trySplit() {
        int dx = (endX - x) / 2;
        int dz = (endZ - x) / 2;

        int endX = this.endX;
        int endZ = this.endZ;

        this.endX = x + dx;
        this.endZ = z + dz;
        this.size = (this.endX - x) * (this.endZ * z);

        int startX = this.endX;
        int startZ = this.endZ;

        return new RegionSpliterator(region, startX, startZ - 1, endX, endZ);
    }

    @Override
    public long estimateSize() {
        return size;
    }

    @Override
    public int characteristics() {
        return Spliterator.DISTINCT;
    }
}
