package me.dags.massblockr.minecraft.world.schematic;

import me.dags.massblockr.jnbt.ByteArrayTag;
import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.NBTInputStream;
import me.dags.massblockr.jnbt.Tag;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.palette.Palette;
import me.dags.massblockr.minecraft.world.LegacyBlockHandler;
import me.dags.massblockr.minecraft.world.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * @author dags <dags@dags.me>
 */
public class LegacySchematic implements Schematic, LegacyBlockHandler {

    private static final byte[] NONE = new byte[0];
    private static final ByteArrayTag EMPTY = new ByteArrayTag("EMPTY", NONE);

    private final File file;
    private final CompoundTag root;
    private final short width, height, length;
    private final int area;
    private final ByteArrayTag blocks;
    private final ByteArrayTag data;
    private final Palette palette;

    private ByteArrayTag adds;

    public LegacySchematic(World world, File file) throws IOException {
        try (NBTInputStream in = new NBTInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            CompoundTag root = (CompoundTag) in.readTag();
            this.file = file;
            this.root = root;
            this.width = get(root, "Width", Short.class, (short) -1);
            this.height = get(root, "Height", Short.class, (short) -1);
            this.length = get(root, "Length", Short.class, (short) -1);
            this.area = width * length;

            palette = world.getRegistry().getGlobalPalette();
            blocks = (ByteArrayTag) root.getTag("Blocks");
            adds = root.containsTag("AddBlocks") ? (ByteArrayTag) root.getTag("AddBlocks") : EMPTY;
            data = (ByteArrayTag) root.getTag("Data");
        }

        if (width * height * length != blocks.getValue().length) {
            throw new IllegalStateException("Invalid schematic!");
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getSectionCount() {
        return 0;
    }

    @Override
    public int getSectionSize() {
        return width * height * length;
    }

    @Override
    public int getBiome(int biomeIndex) {
        return 0;
    }

    @Override
    public Palette getPalette(int sectionIndex) {
        return palette;
    }

    @Override
    public int getSectionIndex(int y) {
        return 0;
    }

    @Override
    public int getBlockIndex(int x, int y, int z) {
        return (y * area) + (z * length) + x;
    }

    @Override
    public BlockState getState(int sectionIndex, int blockIndex) {
        int id = getShort(blockIndex, blocks.getValue(), adds.getValue());
        int meta = getNibble(blockIndex, data.getValue());
        int stateId = getStateId(id, meta);
        return getPalette(sectionIndex).getState(stateId);
    }

    @Override
    public void setBiome(int biomeIndex, int biome) {

    }

    @Override
    public void setState(int sectionIndex, int blockIndex, BlockState state) {
        int stateId = getPalette(sectionIndex).getOrCreateStateId(state);
        int id = getBlockId(stateId);
        int meta = getMetaData(stateId);
        byte[] adds = setShort(blockIndex, id, blocks.getValue(), this.adds.getValue());
        setNibble(blockIndex, meta, data.getValue());
        if (adds != this.adds.getValue()) {
            this.adds = new ByteArrayTag("AddBlocks", adds);
        }
    }

    private static <T> T get(CompoundTag root, String key, Class<T> valType, T def) {
        Tag tag = root.getTag(key);
        if (tag == null) {
            return def;
        }
        Object value = tag.getValue();
        return valType.cast(value);
    }

    @Override
    public CompoundTag getTag() {
        return null;
    }
}
