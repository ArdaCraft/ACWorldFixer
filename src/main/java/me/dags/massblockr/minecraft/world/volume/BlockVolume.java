package me.dags.massblockr.minecraft.world.volume;

import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.palette.Palette;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockVolume {

    int getWidth();

    int getHeight();

    int getLength();

    int getSectionSize();

    int getSectionCount();

    int getBiome(int biomeIndex);

    Palette getPalette(int sectionIndex);

    BlockState getState(int sectionIndex, int blockIndex);

    default int getBiome(int x, int z) {
        return getBiome(getBiomeIndex(x, z));
    }

    default BlockState getState(int x, int y, int z) {
        return getState(getSectionIndex(y), getBlockIndex(x, y, z));
    }

    default int getSectionIndex(int y) {
        return 0;
    }

    default int getBiomeIndex(int x, int z) {
        return (z * getLength()) + x;
    }

    default int getBlockIndex(int x, int y, int z) {
        return (y * getWidth() * getLength()) + (z * getLength()) + x;
    }
}
