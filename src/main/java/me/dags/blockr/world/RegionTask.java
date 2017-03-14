package me.dags.blockr.world;

import me.dags.blockr.block.replacers.Replacer;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.ChunkImpl2;
import org.pepsoft.minecraft.RegionFile;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dags <dags@dags.me>
 */
public class RegionTask implements Runnable, Callable<Object> {

    private final File inputFile;
    private final File outputFile;
    private final AtomicInteger counter;
    private final Replacer[][] replacers;

    public RegionTask(File inputFile, File outputFile, Replacer[][] replacers, AtomicInteger counter) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.replacers = replacers;
        this.counter = counter;
    }

    @Override
    public Object call() throws Exception {
        this.run();
        return true;
    }

    @Override
    public void run() {
        try {
            RandomAccessFile from = new RandomAccessFile(inputFile, "rw");
            RandomAccessFile to = new RandomAccessFile(outputFile, "rw");
            from.getChannel().transferTo(0, Long.MAX_VALUE, to.getChannel());

            RegionFile region = new RegionFile(outputFile);

            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    Chunk chunk = readChunk(region, x, z);
                    if (chunk == null) {
                        continue;
                    }
                    processChunk(chunk);
                    writeChunk(region, chunk, x, z);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            counter.getAndAdd(1);
            ChangeStats.incRegionsCount();
        }
    }

    private Chunk readChunk(RegionFile region, int i, int j) throws IOException {
        try (InputStream input = region.getChunkDataInputStream(i,  j)) {
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

    private void processChunk(Chunk chunk) {
        if (replacers.length > 0) {
            processBlocks(chunk);
        }
        ChangeStats.incChunkCount();
    }

    private void processBlocks(Chunk chunk) {
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int id = chunk.getBlockType(x, y, z);
                    if (id >= 0 && id < this.replacers.length) {
                        Replacer[] replacers = this.replacers[id];
                        if (replacers == null) {
                            continue;
                        }
                        for (Replacer replacer : replacers) {
                            if (replacer.apply(chunk, id, x, y, z)) {
                                ChangeStats.incBlockCount();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
