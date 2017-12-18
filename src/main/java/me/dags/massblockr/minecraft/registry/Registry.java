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

/**
 * @author dags <dags@dags.me>
 */
public class Registry {

    private final Map<String, Block> namesToBlocks = new HashMap<>();
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
    }
}
