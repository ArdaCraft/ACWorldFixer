package me.dags.massblockr.mapper;

import me.dags.massblockr.minecraft.block.BlockState;

/**
 * @author dags <dags@dags.me>
 */
public interface Mapper {

    BlockState map(BlockState state);

    Mapper PASS_THROUGH = state -> state;

    static Mapper single(BlockState match, BlockState replace) {
        return state -> state == match ? replace : state;
    }
}
