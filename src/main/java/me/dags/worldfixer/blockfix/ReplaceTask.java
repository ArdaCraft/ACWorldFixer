package me.dags.worldfixer.blockfix;

import me.dags.worldfixer.blockfix.replacers.Replacer;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.pepsoft.minecraft.*;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dags <dags@dags.me>
 */
public class ReplaceTask implements Runnable {

    private final Replacer[][] replacers;
    private final Set<String> entities = new HashSet<>();
    private final Set<String> tileEntities = new HashSet<>();
    private final AtomicInteger globalCount;
    private final File regionFile;

    ReplaceTask(AtomicInteger globalCount, File regionFile, Replacer[][] replacers) {
        this.globalCount = globalCount;
        this.regionFile = regionFile;
        this.replacers = replacers;
    }

    ReplaceTask withEntities(Collection<String> entities) {
        entities.stream().map(String::toLowerCase).forEach(this.entities::add);
        return this;
    }

    ReplaceTask withTileEntities(Collection<String> tileEntities) {
        tileEntities.stream().map(String::toLowerCase).forEach(this.tileEntities::add);
        return this;
    }

    @Override
    public void run() {
        try {
            RegionFile region = loadRegionFile();
            for (int x = -100; x < 100; x++) {
                for (int z = -100; z < 100; z++) {
                    if (!region.containsChunk(x, z)) {
                        continue;
                    }
                    Chunk chunk = readChunk(region, x, z);
                    if (chunk == null) {
                        continue;
                    }
                    processChunk(chunk);
                    writeChunk(region, chunk, x, z);
                }
            }
            globalCount.getAndAdd(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private RegionFile loadRegionFile() throws IOException {
        return new RegionFile(regionFile);
    }

    private Chunk readChunk(RegionFile region, int i, int j) throws IOException {
        try (NBTInputStream stream = new NBTInputStream(region.getChunkDataInputStream(i, j))) {
            CompoundTag tag = (CompoundTag) stream.readTag();
            return new ChunkImpl2(tag, 256);
        }
    }

    private void writeChunk(RegionFile region, Chunk chunk, int x, int z) throws IOException {
        DataOutputStream outputStream = region.getChunkDataOutputStream(x, z);
        if (outputStream == null) {
            return;
        }
        NBTOutputStream nbt = new NBTOutputStream(outputStream);
        nbt.writeTag(chunk.toNBT());
        nbt.close();
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
