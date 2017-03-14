package me.dags.blockr.world;

import me.dags.blockr.block.replacers.Replacer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dags <dags@dags.me>
 */
public class Dimension {

    private final File dimensionIn;
    private final File dimensionOut;
    private final File regionsIn;
    private final File regionsOut;
    private final Replacer[][] replacers;

    public Dimension(File dimensionIn, File outputParentDir, Replacer[][] replacers) {
        this(dimensionIn, outputParentDir, dimensionIn.getName(), replacers);
    }

    public Dimension(File dimensionIn, File outputParentDir, String name, Replacer[][] replacers) {
        this.dimensionIn = dimensionIn;
        this.dimensionOut = new File(outputParentDir, name);
        this.regionsIn = new File(dimensionIn, "region");
        this.regionsOut = new File(dimensionOut, "region");
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

    public List<RegionTask> getRegionTasks(final AtomicInteger progress) {
        List<RegionTask> tasks = new ArrayList<>();
        for (File regionIn : World.listDir(regionsIn)) {
            if (regionIn.getName().endsWith(".mca")) {
                File regionOut = new File(regionsOut, regionIn.getName());
                RegionTask task = new RegionTask(regionIn, regionOut, replacers, progress);
                tasks.add(task);
            }
        }
        return tasks;
    }

    private static void copy(File from, File outputParent) {
        File to = new File(outputParent, from.getName());
        if (from.isDirectory()) {
            if (from.getName().equalsIgnoreCase("region") || World.hasRegionsDir(from)) {
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
