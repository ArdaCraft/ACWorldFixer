package me.dags.massblockr.minecraft.world.chunk;

import me.dags.massblockr.jnbt.*;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.palette.LocalPalette;
import me.dags.massblockr.minecraft.palette.Palette;
import me.dags.massblockr.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class NewChunk implements Chunk {

    private final CompoundTag chunk;
    private final LongArrayTag[] sections;
    private final ByteArrayTag biomes;
    private final LocalPalette[] palettes;
    private final int sectionCount;
    private final int maxHeight;
    private final int x, z;

    public NewChunk(World world, int x, int z, CompoundTag chunk) {
        CompoundTag level = (CompoundTag) chunk.getTag("Level");
        ListTag sections = (ListTag) level.getTag("Sections");
        this.x = x;
        this.z = z;
        this.chunk = chunk;
        this.sectionCount = sections.getValue().size();
        this.sections = new LongArrayTag[sectionCount];
        this.palettes = new LocalPalette[sectionCount];
        this.biomes = (ByteArrayTag) level.getTag("Biomes");
        this.maxHeight = sectionCount << 4;
        for (int i = 0; i < sectionCount; i++) {
            CompoundTag section = (CompoundTag) sections.getValue().get(i);
            ListTag palette = (ListTag) section.getTag("Palette");
            LongArrayTag states = (LongArrayTag) section.getTag("BlockStates");
            this.sections[i] = states;
            this.palettes[i] = world.getRegistry().createLocalPalette(palette);
        }
    }

    @Override
    public int getHeight() {
        return maxHeight;
    }

    @Override
    public int getSectionCount() {
        return sectionCount;
    }

    @Override
    public CompoundTag getTag() {
        CompoundTag level = (CompoundTag) chunk.getTag("Level");
        ListTag sections = (ListTag) level.getTag("Sections");
        for (int i = 0; i < sections.getValue().size(); i++) {
            CompoundTag section = (CompoundTag) sections.getValue().get(i);
            LocalPalette palette = palettes[i];
            section.setTag("Palette", palette.toNBT());
        }
        return chunk;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public int getBiome(int biomeIndex) {
        return biomes.getValue()[biomeIndex];
    }

    @Override
    public Palette getPalette(int sectionIndex) {
        return palettes[sectionIndex];
    }

    @Override
    public void setBiome(int biomeIndex, int biome) {
        biomes.getValue()[biomeIndex] = (byte) biome;
    }

    @Override
    public BlockState getState(int sectionIndex, int blockIndex) {
        long[] section = sections[sectionIndex].getValue();
        int longIndex = getLongIndex(blockIndex);
        int shortIndex = getShortIndex(blockIndex);
        long value = section[longIndex];
        int blockStateId = getShort(value, shortIndex);
        BlockState state = getPalette(sectionIndex).getState(blockStateId);
        if (state == null) {
            System.out.println("NULL STATE FOR ID: " + blockStateId);
        }
        return state;
    }

    @Override
    public void setState(int sectionIndex, int blockIndex, BlockState state) {
        int blockStateId = getPalette(sectionIndex).getOrCreateStateId(state);
        int longIndex = getLongIndex(blockIndex);
        int shortIndex = getShortIndex(blockIndex);
        long[] section = sections[sectionIndex].getValue();
        long value = section[longIndex];
        section[longIndex] = setShort(value, blockStateId, shortIndex);
    }

    // converts index 0-4095 to 0-255 (256 longs in a section)
    private static int getLongIndex(int blockIndex) {
        return blockIndex >> 5;
    }

    // converts index 0-4095 a 0-3 (4 shorts in a long)
    private static int getShortIndex(int blockIndex) {
        return blockIndex & 3;
    }

    private static long setShort(long l, int value, int index) {
        int shift = (3 - index) << 4; // might be `index << 4`
        return l | (((long) value & 0xff) << shift);
    }

    private static int getShort(long l, int index) {
        int shift = (3 - index) << 4; // might be `index << 4`
        short val = (short) (l >> shift);
        return val & 0xff;
    }

    public static Chunk createNewChunk(World world, Chunk input) {
        List<Tag> sections = new ArrayList<>(input.getSectionCount());
        for (int i = 0; i < input.getSectionCount(); i++) {
            CompoundTag section = new CompoundTag("", new HashMap<>());
            section.setTag("Y", new ByteTag("Y", (byte) i));
            section.setTag("Palette", new ListTag("Palette", NBTConstants.TYPE_COMPOUND, Collections.emptyList()));
            section.setTag("BlockStates", new LongArrayTag("BlockStates", new long[256]));
            sections.add(section);
        }

        CompoundTag level = new CompoundTag("Level", new HashMap<>());
        level.setTag("Sections", new ListTag("Sections", NBTConstants.TYPE_COMPOUND, sections));

        CompoundTag chunk = new CompoundTag("", new HashMap<>());
        chunk.setTag("Level", level);
        chunk.setTag("DataVersion", new IntTag("DataVersion", 1452));

        Chunk.copy(input.getTag(), chunk);
        return new NewChunk(world, input.getX(), input.getZ(), chunk);
    }
}
