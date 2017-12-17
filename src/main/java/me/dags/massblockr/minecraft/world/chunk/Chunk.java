package me.dags.massblockr.minecraft.world.chunk;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.ListTag;
import me.dags.massblockr.jnbt.Tag;
import me.dags.massblockr.minecraft.world.volume.VolumeInput;
import me.dags.massblockr.minecraft.world.volume.VolumeOutput;

import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public interface Chunk extends VolumeInput, VolumeOutput {

    CompoundTag getTag();

    int getX();

    int getZ();

    @Override
    default int getWidth() {
        return 16;
    }

    @Override
    default int getLength() {
        return 16;
    }

    default int getSectionSize() {
        return 4096; // 16 ^ 3
    }

    @Override
    default int getSectionIndex(int y) {
        return y >> 4;
    }

    @Override
    default int getBiomeIndex(int x, int z) {
        return (z << 4) + x;
    }

    @Override
    default int getBlockIndex(int x, int y, int z) {
        return x | ((z | ((y & 0xF) << 4)) << 4);
    }

    static void copy(CompoundTag in, CompoundTag out) {
        if (in == out) {
            return;
        }

        for (Map.Entry<String, Tag> entry : in.getValue().entrySet()) {
            if (out.getValue().containsKey(entry.getKey())) {
                continue;
            }
            out.setTag(entry.getKey(), entry.getValue());
        }

        in = (CompoundTag) in.getTag("Level");
        out = (CompoundTag) out.getTag("Level");

        for (Map.Entry<String, Tag> entry : in.getValue().entrySet()) {
            if (out.getValue().containsKey(entry.getKey())) {
                continue;
            }
            out.setTag(entry.getKey(), entry.getValue());
        }

        ListTag sectionsIn = (ListTag) in.getTag("Sections");
        ListTag sectionsOut = (ListTag) out.getTag("Sections");
        for (int i = 0; i < sectionsIn.getValue().size(); i++) {
            CompoundTag sectionIn = (CompoundTag) sectionsIn.getValue().get(i);
            CompoundTag sectionOut = (CompoundTag) sectionsOut.getValue().get(i);
            sectionOut.setTag("BlockLight", sectionIn.getTag("BlockLight"));
            sectionOut.setTag("SkyLight", sectionIn.getTag("SkyLight"));
        }
    }
}
