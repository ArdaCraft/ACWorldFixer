package me.dags.blockr.task;

import me.dags.blockr.replacer.Replacer;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.ChunkImpl2;
import org.pepsoft.minecraft.RegionFile;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author dags <dags@dags.me>
 */
public class RegionTask extends ExtentTask<Chunk> {

    private static final int CHUNK_SIZE = 16 * 256 * 16;

    public RegionTask(File inputFile, File outputFile, Replacer[][] replacers) {
        super(inputFile, outputFile, replacers);
    }

    @Override
    public void process(File file) throws IOException {
        try (RegionFile region = new RegionFile(file)) {
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    Chunk chunk = readChunk(region, x, z);
                    if (chunk == null) {
                        continue;
                    }
                    processExtent(chunk);
                    writeChunk(region, chunk, x, z);
                    ChangeStats.incBlockVisits(CHUNK_SIZE);
                }
            }
        }
    }

    private Chunk readChunk(RegionFile region, int i, int j) throws IOException {
        try (InputStream input = region.getChunkDataInputStream(i, j)) {
            if (input == null) {
                return null;
            }
            try (NBTInputStream stream = new NBTInputStream(input)) {
                CompoundTag tag = (CompoundTag) stream.readTag();
                return new ChunkImpl2(tag, 256);
            }
        }
    }

    private void writeChunk(RegionFile region, Chunk chunk, int x, int z) throws IOException {
        try (DataOutputStream outputStream = region.getChunkDataOutputStream(x, z)) {
            if (outputStream == null) {
                return;
            }
            NBTOutputStream nbt = new NBTOutputStream(outputStream);
            nbt.writeTag(chunk.toNBT());
            nbt.close();
        }
    }
}
