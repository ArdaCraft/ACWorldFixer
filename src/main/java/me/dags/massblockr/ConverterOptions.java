package me.dags.massblockr;

import me.dags.massblockr.minecraft.world.WorldOptions;

import java.io.File;

/**
 * @author dags <dags@dags.me>
 */
public class ConverterOptions {

    public String worldIn = "";
    public String levelIn = "";
    public String registryIn = "";

    public String worldOut = "";
    public String levelOut = "";
    public String registryOut = "";

    public String mappings = "";

    public int threadCount = 0;
    public boolean schemsOnly = false;

    public WorldOptions getInputWorld() {
        WorldOptions options = new WorldOptions();
        setOptions(options, worldIn, levelIn, registryIn);
        return options;
    }

    public WorldOptions getOutputWorld() {
        WorldOptions options = new WorldOptions(true);
        if (worldOut.isEmpty()) {
            worldOut = worldIn + "-converted-" + System.currentTimeMillis();
        }
        setOptions(options, worldOut, levelOut, registryOut);
        return options;
    }

    @Override
    public String toString() {
        return "ConverterOptions{" +
                "worldIn='" + worldIn + '\'' +
                ", levelIn='" + levelIn + '\'' +
                ", registryIn='" + registryIn + '\'' +
                ", worldOut='" + worldOut + '\'' +
                ", levelOut='" + levelOut + '\'' +
                ", registryOut='" + registryOut + '\'' +
                ", mappings='" + mappings + '\'' +
                ", threadCount=" + threadCount +
                ", schemsOnly=" + schemsOnly +
                '}';
    }

    private void setOptions(WorldOptions options, String dir, String level, String registry) {
        options.setDirectory(new File(worldIn).getAbsoluteFile());

        if (!levelIn.isEmpty()) {
            options.setLevelData(new File(levelIn).getAbsoluteFile());
        }

        if (!registryIn.isEmpty()) {
            options.setRegistry(new File(registryIn).getAbsoluteFile());
        }
    }
}
