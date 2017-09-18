package me.dags.blockr.world;

import me.dags.blockr.Config;
import me.dags.blockr.replacer.Replacer;
import me.dags.blockr.task.ExtentTask;
import me.dags.blockr.task.RegionTask;
import me.dags.blockr.task.SchematicTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Dimension {

    private final File dimensionIn;
    private final File dimensionOut;
    private final File regionsIn;
    private final File regionsOut;
    private final File schematicsIn;
    private final File schematicsOut;
    private final Replacer[][] replacers;

    public Dimension(File dimensionIn, File outputParentDir, Replacer[][] replacers) {
        this(dimensionIn, outputParentDir, dimensionIn.getName(), replacers);
    }

    public Dimension(File dimensionIn, File outputParentDir, String name, Replacer[][] replacers) {
        this.dimensionIn = dimensionIn;
        this.dimensionOut = new File(outputParentDir, name);
        this.regionsIn = new File(dimensionIn, "region");
        this.regionsOut = new File(dimensionOut, "region");
        this.schematicsIn = new File(dimensionIn, "schematic");
        this.schematicsOut = new File(dimensionOut, "schematic");
        this.replacers = replacers;
    }

    public File getOutputLevelFile() {
        return new File(dimensionOut, "level.dat");
    }

    public File getOutputDirectory() {
        return dimensionOut;
    }

    public String getName() {
        return dimensionIn.getName();
    }

    public void mkdirs() {
        dimensionOut.mkdirs();
        regionsOut.mkdirs();
    }

    public void copyData() {
        for (File file : World.listDir(dimensionIn)) {
            copy(file, dimensionOut);
        }
    }

    public int countRegionFiles(Config config) {
        if (!config.schematicsOnly) {
            int count = 0;
            for (File regionIn : World.listDir(regionsIn)) {
                if (regionIn.getName().endsWith(".mca")) {
                    count++;
                }
            }
            return count;
        }
        return 0;
    }

    public int countSchematicFiles(Config config) {
        int count = 0;
        for (File schematicIn : World.listDir(schematicsIn)) {
            if (schematicIn.getName().endsWith(".schematic")) {
                count++;
            }
        }
        return count;
    }

    public void addRegionTasks(final Config config, final List<ExtentTask<?>> tasks) {
        if (!config.schematicsOnly) {
            for (File regionIn : World.listDir(regionsIn)) {
                if (regionIn.getName().endsWith(".mca")) {
                    File regionOut = new File(regionsOut, regionIn.getName());
                    RegionTask task = new RegionTask(regionIn, regionOut, replacers);
                    tasks.add(task);
                }
            }
        }
    }

    public void addSchematicTasks(final Config config, final List<ExtentTask<?>> tasks) {
        List<File> list = new LinkedList<>();
        World.listDirRecursive(schematicsIn, list, false);
        for (File schemIn : list) {
            if (schemIn.getName().endsWith(".schematic")) {
                String relative = schemIn.getAbsolutePath().substring(schematicsIn.getAbsolutePath().length());
                File schemOut = new File(schematicsOut, relative);
                SchematicTask task = new SchematicTask(schemIn, schemOut, replacers);
                tasks.add(task);
            }
        }
    }

    private static void copy(File from, File outputParent) {
        File to = new File(outputParent, from.getName());
        if (from.isDirectory()) {
            if (from.getName().equalsIgnoreCase("region") || World.hasRegionsDir(from)) {
                return;
            }
            if (from.getName().equalsIgnoreCase("schematic")) {
                return;
            }
            for (File file : World.listDir(from)) {
                copy(file, to);
            }
        } else {
            try {
                to.getParentFile().mkdirs();
                to.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (FileInputStream input = new FileInputStream(from); FileChannel in = input.getChannel()) {
                try (FileOutputStream output = new FileOutputStream(to); FileChannel out = output.getChannel()) {
                    in.transferTo(0L, Long.MAX_VALUE, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
