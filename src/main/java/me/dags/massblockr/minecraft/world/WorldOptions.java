package me.dags.massblockr.minecraft.world;

import java.io.*;

/**
 * @author dags <dags@dags.me>
 */
public class WorldOptions {

    private File directory;
    private File levelData;
    private File registry;
    private final boolean output;

    public WorldOptions() {
        this(false);
    }

    public WorldOptions(boolean output) {
        this.output = output;
    }

    public void setDirectory(File file) {
        this.directory = file;
    }

    public void setRegistry(File file) {
        this.registry = file;
    }

    public void setLevelData(File file) {
        this.levelData = file;
    }

    public File getDirectory() {
        return directory;
    }

    public InputStream getRegistryStream() throws FileNotFoundException {
        if (registry == null || !registry.exists()) {
            return WorldOptions.class.getResourceAsStream("/registry.json");
        }
        return new FileInputStream(registry);
    }

    public InputStream getLevelDataStream() throws IOException {
        if (levelData == null || !levelData.exists()) {
            return WorldOptions.class.getResourceAsStream("/level.dat");
        }
        return new FileInputStream(levelData);
    }

    public void validate() throws IllegalStateException {
        if (directory == null) {
            throw new IllegalStateException("World directory has not been set!");
        }

        if (!output) {
            File level = new File(directory, "level.dat");
            if (!level.exists()) {
                throw new IllegalStateException("World directory does not contain a level.dat file: " + level);
            }
        }

        if (registry != null && !registry.exists()) {
            throw new IllegalStateException("Invalid registry file: " + registry);
        }
    }
}
