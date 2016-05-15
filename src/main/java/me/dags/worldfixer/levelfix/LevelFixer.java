package me.dags.worldfixer.levelfix;

import me.dags.blockinfo.Config;
import me.dags.worldfixer.WorldData;
import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class LevelFixer {

    private final Config config;
    private final WorldData worldData;

    public LevelFixer(Config config, WorldData worldData) {
        this.config = config;
        this.worldData = worldData;
    }

    public void fix() {
        CompoundTag level = worldData.getLevelData();
        if (level == null) {
            throw new NullPointerException();
        }
        if (level.containsTag("FML")) {
            CompoundTag FML = (CompoundTag) level.getTag("FML");
            if (FML.containsTag("ItemData")) {
                ListTag repaired = processItemData((ListTag) FML.getTag("ItemData"));
                FML.setTag("ItemData", repaired);
                worldData.writeLevelData(level);
            }
        }
    }

    private ListTag processItemData(ListTag ItemData) {
        List<Tag> list = new ArrayList<>();
        ItemData.getValue().forEach(tag -> {
            if (tag instanceof CompoundTag) {
                CompoundTag block = (CompoundTag) tag;
                Result result = processEntry(block);
                if (result == Result.KEEP) {
                    list.add(block);
                    String name = block.getTag("K").getValue().toString();
                    Integer id = (Integer) block.getTag("V").getValue();
                    worldData.blockRegistry.register(name, id);
                } else {
                    System.out.println("Discarding block " + block.toString());
                }
            }
        });
        return new ListTag("ItemData", CompoundTag.class, list);
    }

    private Result processEntry(CompoundTag tag) {
        String name = tag.getTag("K").getValue().toString().toLowerCase();
        if (name.contains("minecraft:")) {
            return Result.KEEP;
        }
        if (name.contains("biblio")) {
            return Result.DISCARD;
        }
        name = correctBlockName(name);
        if (config.removeBlocks.containsKey(name.trim())) {
            return Result.DISCARD;
        }
        tag.setTag("K", new StringTag("K", name));
        return Result.KEEP;
    }

    private String correctBlockName(String in) {
        return in
                .replace("tile.", "")
                .replace("leaf", "leaves")
                .replace("trap-door", "trap_door")
                .replace("stained-glass", "glass_stained")
                .replace("stained-pane", "pane_stained")
                .replace("doubleslab", "double_slab")
                .replace("_ctm", "")
                .replace("gate", "fence_gate")
                .replace("tall-plant", "double_plant")
                .replace("ghost-pane", "_ghost_pane")
                .replace(".name", "");
    }

    private enum Result {
        DISCARD,
        KEEP,
        ;
    }
}
