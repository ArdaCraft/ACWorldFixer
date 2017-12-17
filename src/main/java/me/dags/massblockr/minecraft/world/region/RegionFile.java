package me.dags.massblockr.minecraft.world.region;

import me.dags.massblockr.jnbt.NBTOutputStream;
import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.minecraft.world.chunk.Chunk;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author dags <dags@dags.me>
 */
public class RegionFile implements Region, AutoCloseable {

    private final World world;
    private final File file;
    private MojangRegionFile regionFile = null;

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
        return StreamSupport.stream(new RegionSpliterator(this), false);
    }

    @Override
    public void writeChunks(Stream<Chunk> chunks) {
        chunks.forEach(chunk -> {
            MojangRegionFile regionFile = getRegionFile();
            try (DataOutputStream out = regionFile.getChunkDataOutputStream(chunk.getX(), chunk.getZ())) {
                if (out == null) {
                    return;
                }
                NBTOutputStream nbt = new NBTOutputStream(out);
                nbt.writeTag(chunk.getTag());
                nbt.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void close() throws Exception {
        if (regionFile != null) {
            regionFile.close();
        }
    }

    World getWorld() {
        return world;
    }

    MojangRegionFile getRegionFile() {
        if (regionFile == null) {
            regionFile = new MojangRegionFile(file);
        }
        return regionFile;
    }

    public static boolean isValid(File file) {
        return file.isFile();
    }
}
