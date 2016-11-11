package me.dags.blockr.block;

import me.dags.blockr.block.replacers.Replacer;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.pepsoft.minecraft.*;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public class ReplaceTask implements Runnable {

    private final Replacer[][] replacers;
    private final Set<String> entities = new HashSet<>();
    private final Set<String> tileEntities = new HashSet<>();
    private final File inputFile;
    private final File outputFile;

    public ReplaceTask(File inputFile, File outputFile, Replacer[][] replacers) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.replacers = replacers;
    }

    public ReplaceTask withEntities(Collection<String> entities) {
        entities.stream().map(String::toLowerCase).forEach(this.entities::add);
        return this;
    }

    public ReplaceTask withTileEntities(Collection<String> tileEntities) {
        tileEntities.stream().map(String::toLowerCase).forEach(this.tileEntities::add);
        return this;
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
                    ChangeStats.incChunkCount();
                }
            }
            ChangeStats.incRegionsCount();
        } catch (IOException e) {
            e.printStackTrace();
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

        if (entities.size() > 0) {
            processEntities(chunk);
        }

        if (tileEntities.size() > 0) {
            processTileEntities(chunk);
        }
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

    private void processEntities(Chunk chunk) {
        Iterator<Entity> eIterator = chunk.getEntities().iterator();
        while (eIterator.hasNext()) {
            Entity entity = eIterator.next();
            if (entities.contains(entity.getId().toLowerCase())) {
                ChangeStats.incEntityCount();
                eIterator.remove();
            }
        }
    }

    private void processTileEntities(Chunk chunk) {
        Iterator<TileEntity> tEIterator = chunk.getTileEntities().iterator();
        while (tEIterator.hasNext()) {
            TileEntity entity = tEIterator.next();
            if (tileEntities.contains(entity.getId().toLowerCase())) {
                ChangeStats.incTileEntityCount();
                tEIterator.remove();
            }
        }
    }
}
