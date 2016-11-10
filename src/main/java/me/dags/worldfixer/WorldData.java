package me.dags.worldfixer;

import me.dags.worldfixer.block.BlockRegistry;
import org.jnbt.*;
import org.pepsoft.minecraft.Block;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class WorldData {

    private final File level;
    private CompoundTag cachedLevel;
    public final BlockRegistry blockRegistry = new BlockRegistry();

    // NOTE! this expects an old level.dat format, needs to be updated to suited Forge's newer scheme
    public WorldData(File level) {
        this.level = level;
    }

    public void loadRegistry() {
        if (getLevelData().containsTag("FML")) {
            CompoundTag FML = (CompoundTag) getLevelData().getTag("FML");
            if (FML.containsTag("ItemData")) {
                ListTag repaired = ((ListTag) FML.getTag("ItemData"));
                repaired.getValue().forEach(this::register);
            } else if (FML.containsTag("Registries")) {
                CompoundTag registries = (CompoundTag) FML.getTag("Registries");
                CompoundTag blocks = (CompoundTag) registries.getTag("minecraft:blocks");
                ListTag ids = (ListTag) blocks.getTag("ids");
                ids.getValue().forEach(this::register);
            }
        } else {
            for (Block block : Block.BLOCKS) {
                if (block.name == null) continue;
                String name = "minecraft:" + block.name.toLowerCase().replace(' ', '_');
                blockRegistry.register(name, block.id);
            }
        }
    }

    public CompoundTag getLevelData() {
        if (cachedLevel != null) {
            return cachedLevel;
        }
        try (NBTInputStream out = new NBTInputStream(new GZIPInputStream(new FileInputStream(level)))) {
            Tag tag = out.readTag();
            if (tag instanceof CompoundTag) {
                return cachedLevel = (CompoundTag) tag;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private void register(Tag tag) {
        CompoundTag mapping = (CompoundTag) tag;
        String name = mapping.getTag("K").getValue().toString().trim();
        int id = (int) mapping.getTag("V").getValue();
        blockRegistry.register(name, id);
    }

    public void writeLevelData() {
        try (NBTOutputStream out = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(level)))) {
            out.writeTag(cachedLevel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean validate() {
        return level.exists();
    }

    public String error() {
        if (!level.exists()) {
            return "level.dat is missing!";
        }
        return "region directory is missing!";
    }
}
