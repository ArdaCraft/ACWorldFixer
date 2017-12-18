package me.dags.massblockr.minecraft.world;

import com.google.common.base.Preconditions;
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
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class WorldImpl implements World {

    private final int schema;
    private final File worldDir;
    private final Registry registry;
    private final List<Dimension> dimensions;

    WorldImpl(WorldOptions options, Level level) throws IOException {
        options.validate();
        this.schema = level.getSchema();
        this.worldDir = options.getDirectory();
        this.registry = Loader.load(options.getRegistryStream());
        this.dimensions = World.findDimensions(this);
        if (level.getSchema() == World.PRE_1_13_SCHEMA) {
            Tag ids = CompoundTag.getTag(level.getRoot(), "FML", "Registries", "minecraft:blocks", "ids");
            Preconditions.checkNotNull(ids, "Level.dat is missing the forge block id registry!");
            this.registry.getGlobalPalette().load((ListTag) ids);
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
}
