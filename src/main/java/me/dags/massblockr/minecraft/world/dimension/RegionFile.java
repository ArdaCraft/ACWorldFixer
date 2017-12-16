package me.dags.massblockr.minecraft.world.dimension;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.NBTInputStream;
import me.dags.massblockr.jnbt.NBTOutputStream;
import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.minecraft.world.chunk.Chunk;
import me.dags.massblockr.minecraft.world.chunk.LegacyChunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * @author dags <dags@dags.me>
 */
public class RegionFile implements Region {

    private final World world;
    private final File file;

    public RegionFile(World world, File file) {
        this.world = world;
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public Stream<Chunk> getChunks() {
        CompoundTag region = loadRegion(file);
        if (region == null) {
            return Stream.empty();
        }
        return region.getValue().values().stream().map(tag -> (CompoundTag) tag).map(this::loadChunk);
    }

    @Override
    public void writeChunks(Stream<Chunk> chunks) {
        CompoundTag region = new CompoundTag("", new HashMap<>());
        chunks.forEach(chunk -> region.setTag("", chunk.getTag()));

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (NBTOutputStream out = new NBTOutputStream(new FileOutputStream(file))) {
                out.writeTag(region);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Chunk loadChunk(CompoundTag chunk) {
        return new LegacyChunk(world, chunk);
    }

    private CompoundTag loadRegion(File file) {
        try (NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            return (CompoundTag) nbt.readTag();
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean isValid(File file) {
        return file.isFile();
    }
}
