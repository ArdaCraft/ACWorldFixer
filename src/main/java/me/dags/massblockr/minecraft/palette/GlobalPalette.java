package me.dags.massblockr.minecraft.palette;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.ListTag;
import me.dags.massblockr.jnbt.Tag;
import me.dags.massblockr.minecraft.block.Block;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.registry.Registry;
import me.dags.massblockr.minecraft.world.LegacyBlockHandler;

/**
 * @author dags <dags@dags.me>
 */
public class GlobalPalette extends AbstractPalette implements LegacyBlockHandler {

    private final Registry registry;

    public GlobalPalette(Registry registry) {
        this.registry = registry;
    }

    @Override
    public BlockState getState(int stateId) {
        if (stateId == 0) {
            return BlockState.AIR;
        }
        return super.getState(stateId);
    }

    public void load(ListTag blocks) {
        for (Tag tag : blocks.getValue()) {
            CompoundTag entry = (CompoundTag) tag;
            loadId(entry);
        }
    }

    private void loadId(CompoundTag data) {
        String key = data.getTag("K").getValue().toString();
        int id = (int) data.getTag("V").getValue();
        Block block = registry.getBlock(key);
        block.forEachMeta((meta, state) -> {
            int stateId = getStateId(id, meta);
            register(stateId, state);
        });
    }

    @Override
    protected int createID() {
        return 0;
    }
}
