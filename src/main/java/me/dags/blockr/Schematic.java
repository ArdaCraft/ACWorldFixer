package me.dags.blockr;

import org.jnbt.*;
import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Extent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class Schematic implements Extent {

    private final File file;
    private final CompoundTag root;
    private final short width, height, length;
    private final int[] blocks;
    private final byte[] data;

    public Schematic(File file) throws IOException {
        try (NBTInputStream in = new NBTInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            CompoundTag root = (CompoundTag) in.readTag();
            this.file = file;
            this.root = root;
            this.width = get(root, "Width", Short.class, (short) -1);
            this.height = get(root, "Height", Short.class, (short) -1);
            this.length = get(root, "Length", Short.class, (short) -1);

            byte[] ids = get(root, "Blocks", byte[].class, new byte[0]);
            byte[] adds = get(root, "AddBlocks", byte[].class, new byte[0]);
            byte[] data = get(root, "Data", byte[].class, new byte[0]);
            int[] blocks = new int[ids.length];
            for (int index = 0; index < ids.length; index++) {
                if ((index >> 1) >= adds.length) {
                    blocks[index] = ids[index] & 0xFF;
                } else {
                    if ((index & 1) == 0) {
                        blocks[index] = ((adds[index >> 1] & 0x0F) << 8) + (ids[index] & 0xFF);
                    } else {
                        blocks[index] = ((adds[index >> 1] & 0xF0) << 4) + (ids[index] & 0xFF);
                    }
                }
            }

            this.blocks = blocks;
            this.data = data;
        }

        if (width * height * length != blocks.length) {
            throw new IllegalStateException("Invalid schematic!");
        }
    }

    public void write() throws IOException {
        byte[] blocks = new byte[this.blocks.length];
        byte[] adds = new byte[(blocks.length >> 1) + 1]; // nibbles
        byte[] data = new byte[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            int id = this.blocks[i];
            byte meta = this.data[i];
            if (id > 255) {
                if ((i & 1) == 0) {
                    adds[i >> 1] = (byte) (adds[i >> 1] & 0xF0 | (id >> 8) & 0xF);
                } else {
                    adds[i >> 1] = (byte) (adds[i >> 1] & 0xF | ((id >> 8) & 0xF) << 4);
                }
            }
            blocks[i] = (byte) id;
            data[i] = meta;
        }

        root.setTag("Blocks", new ByteArrayTag("Blocks", blocks));
        root.setTag("AddBlocks", new ByteArrayTag("AddBlocks", adds)); // wp expects this tag even if every value is zero
        root.setTag("Data", new ByteArrayTag("Data", data));

        try (NBTOutputStream outputStream = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            outputStream.writeTag(root);
        }
    }

    public int getSize() {
        return blocks.length;
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
    public List<Entity> getEntities() {
        return Collections.emptyList();
    }

    @Override
    public int getBlockType(int x, int y, int z) {
        return blocks[getIndex(x, y, z)];
    }

    @Override
    public int getDataValue(int x, int y, int z) {
        return data[getIndex(x, y, z)];
    }

    @Override
    public int getBiome(int x, int z) {
        return 0;
    }

    @Override
    public void setBlockType(int x, int y, int z, int type) {
        blocks[getIndex(x, y, z)] = type;
    }

    @Override
    public void setDataValue(int x, int y, int z, int data) {
        this.data[getIndex(x, y, z)] = (byte) data;
    }

    private int getIndex(int x, int y, int z) {
        return y * width * length + z * width + x;
    }

    private static <T> T get(CompoundTag root, String key, Class<T> valType, T def) {
        Tag tag = root.getTag(key);
        if (tag == null) {
            return def;
        }
        Object value = tag.getValue();
        return valType.cast(value);
    }
}
