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

    int SECTION_AREA = 16 * 16;

    CompoundTag getTag();

    @Override
    default int getWidth() {
        return 16;
    }

    @Override
    default int getLength() {
        return 16;
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

    static void shallowCopy(Chunk in, Chunk out) {
        if (in == out) {
            return;
        }

        CompoundTag levelIn = (CompoundTag) in.getTag().getTag("Level");
        CompoundTag levelOut = (CompoundTag) out.getTag().getTag("Level");
        shallowCopy(levelIn, levelOut);
    }

    static void shallowCopy(CompoundTag in, CompoundTag out) {
        if (in == out) {
            return;
        }

        for (Map.Entry<String, Tag> entry : in.getValue().entrySet()) {
            if (entry.getKey().equals("Sections")) {
                continue;
            }
            out.setTag(entry.getKey(), entry.getValue());
        }

        ListTag sectionsIn = (ListTag) in.getTag("Sections");
        ListTag sectionsOut = (ListTag) out.getTag("Sections");
        for (int i = 0; i < sectionsIn.getValue().size(); i++) {
            CompoundTag sectionIn = (CompoundTag) sectionsIn.getValue().get(i);
            CompoundTag sectionUut = (CompoundTag) sectionsOut.getValue().get(i);
            sectionUut.setTag("Y", sectionIn.getTag("Y"));
            sectionUut.setTag("BlockLight", sectionIn.getTag("BlockLight"));
            sectionUut.setTag("SkyLight", sectionIn.getTag("SkyLight"));
        }
    }
}
