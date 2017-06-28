package me.dags.blockr.world;

import me.dags.blockr.block.BlockRegistry;
import org.jnbt.*;
import org.pepsoft.minecraft.Block;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public abstract class WorldData {

    private CompoundTag cachedLevel;
    public final BlockRegistry blockRegistry = new BlockRegistry();

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

    public static CompoundTag mergeLevels(WorldData oldData, WorldData newData) {
        CompoundTag old = oldData.getLevelData().clone();

        CompoundTag Data = (CompoundTag) old.getTag("Data");
        CompoundTag FML = (CompoundTag) newData.getLevelData().getTag("FML").clone();
        CompoundTag Forge = (CompoundTag) old.getTag("Forge");

        CompoundTag oldFml = (CompoundTag) old.getTag("FML");
        if (oldFml != null) {
            // not sure if necessary, probably gets overwritten anyway
            FML.setTag("ModList", oldFml.getTag("ModList").clone());
        }

        CompoundTag level = new CompoundTag("", new HashMap<>());
        level.setTag("Data", Data);
        level.setTag("FML", FML);
        level.setTag("Forge", Forge);

        return level;
    }

    public static void writeLevelData(Tag tag, File outFile) {
        System.out.println("WRITING LEVEL TO: " + outFile);

        try {
            if (!outFile.exists()) {
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();
            }
            try (OutputStream output = new FileOutputStream(outFile)) {
                try (NBTOutputStream out = new NBTOutputStream(new GZIPOutputStream(output))) {
                    out.writeTag(tag);
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
}
