package me.dags.worldfixer;

import org.jnbt.*;
import org.pepsoft.minecraft.Block;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class LevelFixer {

    public final WorldData worldData;
    private final List<CompoundTag> itemData = new ArrayList<>();
    private Type type = Type.FORGE;

    public LevelFixer(WorldData worldData) {
        this.worldData = worldData;
    }

    public void loadRegistry() {
        if (itemData.isEmpty()) {
            loadItemData();
        }
        itemData.forEach(i -> {
            String name = i.getTag("K").getValue().toString().trim();
            int id = (int) i.getTag("V").getValue();
            worldData.blockRegistry.register(name, id);
        });
    }

    private void loadItemData() {
        if (worldData.getLevelData().containsTag("FML")) {
            CompoundTag FML = FML();
            if (FML.containsTag("ItemData")) {
                loadLegacyForge();
                return;
            } else if (FML.containsTag("registries")) {
                return;
            }
        }
        loadVanilla();
    }

    private void loadLegacyForge() {
        type = Type.FORGE_LEGACY;
        ListTag itemData = (ListTag) FML().getTag("ItemData");
        processItemData(itemData);
    }

    private void loadVanilla() {
        type = Type.VANILLA;
        for (Block block : Block.BLOCKS) {
            if (block.name == null) continue;
            Map<String, Tag> map = new HashMap<>();
            String name = "minecraft:" + block.name.toLowerCase().replace(' ', '_');
            map.put("K", new StringTag("K", name));
            map.put("V", new IntTag("V", block.id));
            itemData.add(new CompoundTag("K", map));
        }
    }

    public void writeChanges() {
        if (type == Type.FORGE_LEGACY) {
            writeForgeLegacy();
            worldData.writeLevelData();
        }
    }

    private void writeForgeLegacy() {
        List<Tag> itemData = new ArrayList<>(this.itemData);
        ListTag ItemData = new ListTag("ItemData", CompoundTag.class, itemData);
        FML().setTag("ItemData", ItemData);
    }


    public void removeBlocks(Collection<String> blocks) {
        Iterator<CompoundTag> iterator = itemData.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next().getTag("K").getValue().toString().trim();
            if (blocks.contains(name)) {
                iterator.remove();
            }
        }
    }

    private CompoundTag FML() {
        CompoundTag level = worldData.getLevelData();
        if (level == null) {
            throw new NullPointerException();
        }
        return (CompoundTag) level.getTag("FML");
    }

    private void processItemData(ListTag ItemData) {
        ItemData.getValue().forEach(tag -> {
            if (tag instanceof CompoundTag) {
                CompoundTag block = (CompoundTag) tag;
                Result result = processEntry(block);
                if (result == Result.KEEP) {
                    itemData.add(block);
                }
            }
        });
    }

    private Result processEntry(CompoundTag tag) {
        String name = tag.getTag("K").getValue().toString().toLowerCase();
        if (name.contains("minecraft:")) {
            return Result.KEEP;
        }
        if (name.contains("biblio")) {
            return Result.DISCARD;
        }
        tag.setTag("K", new StringTag("K", correctBlockName(name)));
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

    private enum Type {
        VANILLA,
        FORGE_LEGACY,
        FORGE,
        ;
    }
}
