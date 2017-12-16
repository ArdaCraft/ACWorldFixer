package me.dags.massblockr.minecraft.world.volume;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.palette.Palette;
import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.minecraft.world.chunk.Chunk;
import me.dags.massblockr.minecraft.world.chunk.LegacyChunk;
import me.dags.massblockr.minecraft.world.chunk.NewChunk;
import me.dags.massblockr.minecraft.world.schematic.Schematic;

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
    public int getMaxSectionIndex() {
        return input.getMaxSectionIndex();
    }

    @Override
    public int getMaxBlockIndex() {
        return input.getMaxBlockIndex();
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

    public static VolumeWorker<Chunk> newChunkWorker(World worldOut, Chunk chunkIn) {
        switch (worldOut.getSchema()) {
            case World.LEGACY_SCHEMA:
                return new VolumeWorker<>(chunkIn, LegacyChunk.createNewChunk(worldOut, chunkIn));
            case World.FUTURE_SCHEMA:
                return new VolumeWorker<>(chunkIn, NewChunk.createNewChunk(worldOut, chunkIn));
            default:
                return null;
        }
    }

    public static VolumeWorker<Schematic> newSchemWorker(World worldOut, Schematic schemIn) {
        return null;
    }
}
