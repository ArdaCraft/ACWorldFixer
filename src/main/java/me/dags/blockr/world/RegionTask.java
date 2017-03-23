package me.dags.blockr.world;

import me.dags.blockr.Config;
import me.dags.blockr.block.replacers.Replacer;
import me.dags.blockr.extra.Art;
import me.dags.blockr.extra.LegacyArt;
import org.jnbt.*;
import org.pepsoft.minecraft.*;

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
        try (RandomAccessFile from = new RandomAccessFile(inputFile, "rw");
            RandomAccessFile to = new RandomAccessFile(outputFile, "rw")) {

            from.getChannel().transferTo(0, Long.MAX_VALUE, to.getChannel());

            try (RegionFile region = new RegionFile(outputFile)) {
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
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            counter.getAndAdd(1);
            ChangeStats.incRegionsCount();
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

    private void processChunk(Chunk chunk) {
        if (replacers.length > 0) {
            processBlocks(chunk);
        }

        if (Config.do_entities) {
            processEntities(chunk);
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

    private static final byte SOUTH = 0;
    private static final byte WEST = 1;

    private void processEntities(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            String id = entity.getId();
            if (id.startsWith("acpaintings")) {
                CompoundTag tag = (CompoundTag) entity.toNBT();

                String newId = "conquest" + id.substring("acpaintings".length());
                tag.setTag(Constants.TAG_ID, new StringTag(Constants.TAG_ID, newId));
                
                String artName = tag.getTag("Name").getValue().toString();
                LegacyArt legacyArt = LegacyArt.forName(artName);
                Art art = legacyArt.toArt();
                tag.setTag("Name", null);
                tag.setTag("ArtID", new IntTag("ArtID", art.index()));

                if (art.sizeX == 32 || art.sizeX == 64) {
                    byte face = (byte) tag.getTag("Facing").getValue();

                    if (face == SOUTH) {
                        int TileX = (int) tag.getTag("TileX").getValue();
                        tag.setTag("TileX", new IntTag("TileX", TileX + 1));
                    } else if (face == WEST) {
                        int TileZ = (int) tag.getTag("TileZ").getValue();
                        tag.setTag("TileZ", new IntTag("TileZ", TileZ + 1));
                    }
                }


                ChangeStats.incEntityCount();
            }
        }
    }
}
