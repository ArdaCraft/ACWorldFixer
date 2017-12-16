package me.dags.massblockr.minecraft.palette;

import me.dags.massblockr.minecraft.block.BlockState;

/**
 * @author dags <dags@dags.me>
 */
public interface Palette {

    BlockState getState(int stateId);

    int getStateId(BlockState state);

    int getOrCreateStateId(BlockState state);
}
