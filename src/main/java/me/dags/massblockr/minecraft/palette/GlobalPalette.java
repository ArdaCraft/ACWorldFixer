package me.dags.massblockr.minecraft.palette;

import com.google.gson.JsonElement;
import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.ListTag;
import me.dags.massblockr.jnbt.Tag;
import me.dags.massblockr.minecraft.block.Block;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.registry.Registry;
import me.dags.massblockr.minecraft.world.LegacyBlockHandler;

import java.util.Map;

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

    public void loadNBT(ListTag blocks) {
        for (Tag tag : blocks.getValue()) {
            CompoundTag entry = (CompoundTag) tag;
            String name = entry.getTag("K").getValue().toString();
            int id = (int) entry.getTag("V").getValue();
            register(name, id);
        }
    }

    public void loadJson(JsonElement json) {
        if (json.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                String name = entry.getKey();
                int id = entry.getValue().getAsInt();
                Block block = registry.getBlock(name);
                block.forEachMeta((meta, state) -> {
                    int stateId = getStateId(id, meta);
                    register(stateId, state);
                });
            }
        }
    }

    private void register(String name, int id) {
        Block block = registry.getBlock(name);
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
