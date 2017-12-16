package me.dags.massblockr.minecraft.block;

import me.dags.massblockr.util.EntryIterator;
import me.dags.massblockr.util.OrderedMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author dags <dags@dags.me>
 */
public class Block {

    public static final Block AIR = new Block("minecraft:air", Collections.emptyList());

    private final String id;
    private final BlockState defaultState;
    private final BlockState[] metaStates;
    private final OrderedMap<Integer, BlockState> states;

    public Block(String id, List<BlockState.Builder> properties) {
        BlockState def = null;
        OrderedMap<Integer, BlockState> unique = new OrderedMap<>();
        OrderedMap<Integer, BlockState> states = new OrderedMap<>();

        int max = 0;
        for (BlockState.Builder builder : properties) {
            BlockState state = builder.build(this);
            states.put(state.hashCode(), state);
            unique.put(state.getMeta(), state);
            max = Math.max(max, state.getMeta());
            if (def == null) {
                def = state;
            }
        }

        this.id = id;
        this.states = states;
        this.defaultState = def;
        this.metaStates = new BlockState[max + 1];

        for (int i = 0; i < metaStates.length; i++) {
            metaStates[i] = unique.getOrDefault(i, defaultState);
        }
    }

    public void forEach(BiConsumer<Integer, BlockState> consumer) {
        EntryIterator<Integer, BlockState> iterator = states.iterator();
        while (iterator.next()) {
            consumer.accept(iterator.key(), iterator.value());
        }
    }

    public void forEachMeta(BiConsumer<Integer, BlockState> consumer) {
        for (int i = 0; i < metaStates.length; i++) {
            consumer.accept(i, metaStates[i]);
        }
    }

    public boolean isAir() {
        return this == AIR;
    }

    public BlockState parse(Map<String, ?> properties) {
        int hash = properties.hashCode();
        return states.getOrDefault(hash, getDefault());
    }

    public String getId() {
        return id;
    }

    public BlockState getDefault() {
        return defaultState;
    }

    @Override
    public String toString() {
        return id;
    }
}
