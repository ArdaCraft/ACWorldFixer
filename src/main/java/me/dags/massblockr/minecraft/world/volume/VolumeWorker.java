package me.dags.massblockr.minecraft.world.volume;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.block.Mapper;
import me.dags.massblockr.minecraft.palette.Palette;
import me.dags.massblockr.util.StatCounters;

/**
 * @author dags <dags@dags.me>
 */
public class VolumeWorker<T extends VolumeInput & VolumeOutput> implements VolumeInput, VolumeOutput {

    private final T input;
    private final T output;

    public VolumeWorker(T input, T output) {
        this.input = input;
        this.output = output;
    }

    public T getInput() {
        return input;
    }

    public T getOutput() {
        return output;
    }

    public VolumeWorker<T> apply(Mapper mapper) {
        try {
            for (int section = 0; section < getSectionCount(); section++) {
                for (int block = 0; block < getSectionSize(); block++) {
                    BlockState in = getState(section, block);
                    BlockState out = mapper.map(in);
                    setState(section, block, out);

                    StatCounters.blockVisits.incrementAndGet();
                    if (in != out) {
                        StatCounters.blockChanges.incrementAndGet();
                    }
                }
            }
        } finally {
            StatCounters.chunkVisits.incrementAndGet();
        }
        return this;
    }

    @Override
    public CompoundTag getTag() {
        return getOutput().getTag();
    }

    @Override
    public void setBiome(int biomeIndex, int biome) {
        getOutput().setBiome(biomeIndex, biome);
    }

    @Override
    public void setState(int sectionIndex, int blockIndex, BlockState state) {
        getOutput().setState(sectionIndex, blockIndex, state);
    }

    @Override
    public int getWidth() {
        return getInput().getWidth();
    }

    @Override
    public int getHeight() {
        return getInput().getHeight();
    }

    @Override
    public int getLength() {
        return getInput().getLength();
    }

    @Override
    public int getSectionCount() {
        return input.getSectionCount();
    }

    @Override
    public int getSectionSize() {
        return input.getSectionSize();
    }

    @Override
    public int getBiome(int biomeIndex) {
        return getInput().getBiome(biomeIndex);
    }

    @Override
    public Palette getPalette(int sectionIndex) {
        return getInput().getPalette(sectionIndex);
    }

    @Override
    public BlockState getState(int sectionIndex, int blockIndex) {
        return getInput().getState(sectionIndex, blockIndex);
    }
}
