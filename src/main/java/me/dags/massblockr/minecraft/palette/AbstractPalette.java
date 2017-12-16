package me.dags.massblockr.minecraft.palette;

import me.dags.massblockr.minecraft.block.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public abstract class AbstractPalette implements Palette {

    private final Map<BlockState, Integer> stateToIds = new HashMap<>();
    private final Map<Integer, BlockState> idToStates = new HashMap<>();

    @Override
    public BlockState getState(int stateId) {
        return idToStates.getOrDefault(stateId, BlockState.AIR);
    }

    @Override
    public int getStateId(BlockState state) {
        return stateToIds.getOrDefault(state, 0);
    }

    @Override
    public int getOrCreateStateId(BlockState state) {
        Integer id = stateToIds.get(state);
        if (id == null) {
            id = createID();
            stateToIds.put(state, id);
            idToStates.put(id, state);
        }
        return id;
    }

    protected void register(int id, BlockState state) {
        stateToIds.put(state, id);
        idToStates.put(id, state);
    }

    protected abstract int createID();
}
