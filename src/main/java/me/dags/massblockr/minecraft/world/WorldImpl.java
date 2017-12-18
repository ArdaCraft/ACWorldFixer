package me.dags.massblockr.minecraft.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.ListTag;
import me.dags.massblockr.jnbt.Tag;
import me.dags.massblockr.minecraft.registry.Loader;
import me.dags.massblockr.minecraft.registry.Registry;
import me.dags.massblockr.minecraft.world.dimension.Dimension;
import me.dags.massblockr.minecraft.world.dimension.WorldDimension;
import me.dags.massblockr.util.FileUtils;

import java.io.*;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class WorldImpl implements World {

    private final int schema;
    private final Level level;
    private final File worldDir;
    private final Registry registry;
    private final List<Dimension> dimensions;

    WorldImpl(WorldOptions options, Level level) throws IOException {
        options.validate();
        this.level = level;
        this.schema = level.getSchema();
        this.worldDir = options.getDirectory();
        this.registry = Loader.load(level, options);
        this.dimensions = World.findDimensions(this);
        if (level.getSchema() == World.PRE_1_13_SCHEMA) {
            loadGlobalRegistry();
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

    private void loadGlobalRegistry() throws IOException {
        Tag ids = CompoundTag.getTag(level.getRoot(), "FML", "Registries", "minecraft:blocks", "ids");
        if (ids == null) {
            String resource = String.format("/legacy/%s.json", level.getMainVersion());
            try (InputStream inputStream = WorldImpl.class.getResourceAsStream(resource)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("No block id mappings found world version: " + level.getMainVersion());
                }
                try (Reader reader = new InputStreamReader(inputStream)) {
                    JsonElement element = new JsonParser().parse(reader);
                    this.registry.getGlobalPalette().loadJson(element);
                }
            }
        } else {
            this.registry.getGlobalPalette().loadNBT((ListTag) ids);
        }
    }
}
