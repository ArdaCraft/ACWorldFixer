package me.dags.massblockr.minecraft.world.volume;

import me.dags.massblockr.minecraft.block.BlockState;

/**
 * @author dags <dags@dags.me>
 */
public interface MutableBlockVolume extends BlockVolume {

    void setBiome(int biomeIndex, int biome);

    void setState(int sectionIndex, int blockIndex, BlockState state);

    default void setBiome(int x, int z, int biome) {
        setBiome(getBiomeIndex(x, z), biome);
    }

    default void setState(int x, int y, int z, BlockState state) {
        setState(getSectionIndex(y), getBlockIndex(x, y, z), state);
    }
}
