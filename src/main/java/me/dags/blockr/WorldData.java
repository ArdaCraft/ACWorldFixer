package me.dags.blockr;

import me.dags.blockr.block.BlockRegistry;
import org.jnbt.*;
import org.pepsoft.minecraft.Block;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class WorldData {

    private final File levelIn;
    private CompoundTag cachedLevel;
    public final BlockRegistry blockRegistry = new BlockRegistry();

    public WorldData(File levelIn) {
        this.levelIn = levelIn;
    }

    public void loadRegistry() {
        if (getLevelData().containsTag("FML")) {
            CompoundTag FML = (CompoundTag) getLevelData().getTag("FML");
            if (FML.containsTag("ItemData")) {
                System.out.println("Loading legacy FML level.dat");
                ListTag repaired = ((ListTag) FML.getTag("ItemData"));
                repaired.getValue().forEach(this::register);
            } else if (FML.containsTag("Registries")) {
                System.out.println("Loading FML level.dat");
                CompoundTag registries = (CompoundTag) FML.getTag("Registries");
                CompoundTag blocks = (CompoundTag) registries.getTag("minecraft:blocks");
                ListTag ids = (ListTag) blocks.getTag("ids");
                ids.getValue().forEach(this::register);
            }
        } else {
            System.out.println("Loading Vanilla level.dat");
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
        try (FileInputStream input = new FileInputStream(levelIn)){
            try (NBTInputStream out = new NBTInputStream(new GZIPInputStream(input))) {
                Tag tag = out.readTag();
                if (tag instanceof CompoundTag) {
                    return cachedLevel = (CompoundTag) tag;
                }
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    public void writeLevelData(File outFile) {
        try {
            if (!outFile.exists()) {
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();
            }
            try (FileOutputStream output = new FileOutputStream(outFile)) {
                try (NBTOutputStream out = new NBTOutputStream(new GZIPOutputStream(output))) {
                    out.writeTag(cachedLevel);
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void register(Tag tag) {
        CompoundTag mapping = (CompoundTag) tag;
        String name = mapping.getTag("K").getValue().toString().trim();
        int id = (int) mapping.getTag("V").getValue();
        blockRegistry.register(name, id);
    }

    public boolean validate() {
        return levelIn.exists();
    }

    public String error() {
        if (!levelIn.exists()) {
            return "level.dat is missing!";
        }
        return "region directory is missing!";
    }
}
