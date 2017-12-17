package me.dags.massblockr.minecraft.registry;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.ListTag;
import me.dags.massblockr.jnbt.StringTag;
import me.dags.massblockr.minecraft.block.Block;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.palette.GlobalPalette;
import me.dags.massblockr.minecraft.palette.LocalPalette;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class Registry {

    private final Map<String, Block> namesToBlocks = new HashMap<>();
    private final Map<String, BlockState> namesToStates = new HashMap<>();
    private final GlobalPalette globalPalette = new GlobalPalette(this);

    public LocalPalette createLocalPalette(ListTag tag) {
        return new LocalPalette(this, tag);
    }

    public GlobalPalette getGlobalPalette() {
        return globalPalette;
    }

    public Block getBlock(String name) {
        return namesToBlocks.getOrDefault(name, Block.AIR);
    }

    public void iterateStates(Consumer<BlockState> consumer) {
        for (Block block : namesToBlocks.values()) {
            block.forEach((i, s) -> consumer.accept(s));
        }
    }

    public BlockState matchState(BlockState in) {
        return namesToStates.getOrDefault(in.toString(), BlockState.AIR);
    }

    public BlockState parseState(CompoundTag tag) {
        String name = ((StringTag) tag.getTag("Name")).getValue();
        CompoundTag properties = (CompoundTag) tag.getTag("Properties");
        Block block = getBlock(name);
        if (block.isAir()) {
            return BlockState.AIR;
        }
        return block.parse(properties.getValue()).orElse(block.getDefault());
    }

    void register(Block block) {
        if (block.getId().equals(Block.AIR.getId())) {
            return;
        }

        namesToBlocks.put(block.getId(), block);
        block.forEach((i, s) -> namesToStates.put(s.toString(), s));
    }
}
