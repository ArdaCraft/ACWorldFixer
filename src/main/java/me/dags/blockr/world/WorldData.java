package me.dags.blockr.world;

import me.dags.blockr.block.BlockRegistry;
import org.jnbt.*;
import org.pepsoft.minecraft.Block;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public abstract class WorldData {

    private CompoundTag cachedLevel;
    final BlockRegistry blockRegistry = new BlockRegistry();

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

    public void copyRegistries(WorldData from) {
        CompoundTag FML = (CompoundTag) from.getLevelData().getTag("FML");
        CompoundTag registries = (CompoundTag) FML.getTag("Registries");

        CompoundTag toFML = (CompoundTag) getLevelData().getTag("FML");
        if (toFML == null) {
            getLevelData().setTag("FML", FML.clone());
        } else {
            toFML.setTag("Registries", registries);
        }
    }

    public CompoundTag getLevelData() {
        if (cachedLevel != null) {
            return cachedLevel;
        }
        try (InputStream input = getInputStream()){
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
            try (OutputStream output = getOutputStream()) {
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

    public abstract boolean validate();

    public abstract String error();

    abstract InputStream getInputStream() throws IOException;

    abstract OutputStream getOutputStream() throws IOException;
}
