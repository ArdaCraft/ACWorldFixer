package me.dags.massblockr.minecraft.world;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.ListTag;
import me.dags.massblockr.jnbt.Tag;
import me.dags.massblockr.minecraft.registry.Loader;
import me.dags.massblockr.minecraft.registry.Registry;
import me.dags.massblockr.minecraft.world.dimension.Dimension;
import me.dags.massblockr.minecraft.world.dimension.WorldDimension;
import me.dags.massblockr.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class WorldImpl implements World {

    private final int schema;
    private final File worldDir;
    private final Registry registry;
    private final List<Dimension> dimensions;

    WorldImpl(WorldOptions options, int schema) throws IOException {
        options.validate();
        this.schema = schema;
        this.worldDir = options.getDirectory();
        this.registry = Loader.load(options.getRegistryStream());
        this.dimensions = World.findDimensions(this);
        if (schema == World.LEGACY_SCHEMA) {
            ListTag ids = loadIdRegistry(options.getLevelDataStream());
            this.registry.getGlobalPalette().load(ids);
        }
    }

    @Override
    public int getSchema() {
        return schema;
    }

    @Override
    public File getDirectory() {
        return worldDir;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    @Override
    public List<Dimension> getDimensions() {
        return dimensions;
    }

    @Override
    public Dimension createDimension(String name) {
        if (name.equalsIgnoreCase(this.getDirectory().getName())) {
            return new WorldDimension(this);
        }
        return new WorldDimension(this, FileUtils.mustDir(worldDir, name));
    }

    private static ListTag loadIdRegistry(InputStream inputStream) throws IOException {
        CompoundTag root = (CompoundTag) FileUtils.readNBT(inputStream, FileUtils.VERSION_GZIP);
        Tag blocks = getTag(root, "FML", "Registries", "minecraft:blocks", "ids");
        if (blocks == null) {
            throw new IllegalArgumentException("Invalid level.dat file!");
        }
        return (ListTag) blocks;
    }

    private static Tag getTag(CompoundTag parent, String... path) {
        Tag tag = parent;

        for (String s : path) {
            if (parent == null) {
                return null;
            }

            tag = parent.getTag(s);

            if (tag instanceof CompoundTag) {
                parent = (CompoundTag) tag;
            } else {
                parent = null;
            }
        }

        return tag;
    }
}
