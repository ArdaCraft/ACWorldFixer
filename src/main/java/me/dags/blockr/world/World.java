package me.dags.blockr.world;

import me.dags.blockr.Config;
import me.dags.blockr.WorldData;
import me.dags.blockr.block.BlockInfo;
import me.dags.blockr.block.replacers.Replacer;
import me.dags.blockr.block.replacers.Replacers;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dags <dags@dags.me>
 */
public class World {

    private final File sourceDirRoot;
    private final File outputDirRoot;
    private final WorldData fromWorld;
    private final WorldData toWorld;
    private final Dimension main;
    private final List<Dimension> dimensions = new ArrayList<>();
    private final int cores;

    public World(File sourceDir, File outputDir, WorldData fromWorld, WorldData toWorld, Config config, int cores) {
        this.sourceDirRoot = sourceDir;
        this.outputDirRoot = outputDir;
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        this.cores = cores;

        Replacer[][] conversions = getRules(config);

        // Add main world
        main = new Dimension(sourceDir, outputDir, sourceDir.getName() + "-converted", conversions);
        dimensions.add(main);

        // Look for other dimensions within the main world dir
        for (File candidate : listDir(sourceDir)) {
            if (hasRegionsDir(candidate)) {
                System.out.println(candidate.getName());

                dimensions.add(new Dimension(candidate, main.getOutputDirectory(), conversions));
            }
        }
    }

    public void convert() {
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicInteger regionCount = new AtomicInteger(0);
        AtomicInteger regionProgress = new AtomicInteger(0);
        AtomicInteger dimensionProgress = new AtomicInteger(0);

        final JLabel dimensionLabel = new JLabel();
        dimensionLabel.setText("Overall:");

        final JProgressBar dimensionBar = new JProgressBar();
        dimensionBar.setPreferredSize(new java.awt.Dimension(200, 30));
        dimensionBar.setValue(0);
        dimensionBar.setMinimum(0);
        dimensionBar.setMaximum(dimensions.size());

        final JProgressBar regionBar = new JProgressBar();
        regionBar.setPreferredSize(new java.awt.Dimension(200, 30));
        regionBar.setValue(0);
        regionBar.setMinimum(0);
        regionBar.setMaximum(1);

        final JPanel panel = new JPanel();
        panel.add(dimensionLabel);
        panel.add(dimensionBar);
        panel.add(new JLabel("Regions:"));
        panel.add(regionBar);

        final JFrame frame = new JFrame();
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread() {
            public void run() {
                while (running.get()) {
                    regionBar.setMaximum(regionCount.get());
                    regionBar.setValue(regionProgress.get());
                    dimensionBar.setValue(dimensionProgress.get());

                    regionBar.repaint();
                    dimensionBar.repaint();

                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        final ExecutorService service = Executors.newFixedThreadPool(cores);

        for (Dimension dimension : dimensions) {
            try {
                regionProgress.set(0);

                List<RegionTask> tasks = dimension.getRegionTasks(regionProgress);
                regionCount.set(tasks.size());

                dimension.mkdirs();
                service.invokeAll(tasks);
                dimension.copyData();

                dimensionProgress.getAndAdd(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        toWorld.writeLevelData(main.getOutputLevelFile());

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        running.set(false);

        JOptionPane.showMessageDialog(null, "Conversion Complete!");
        frame.dispose();

        try {
            Desktop.getDesktop().open(outputDirRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Replacer[][] getRules(Config config) {
        int max = 0;

        Map<Integer, List<Replacer>> filter = new HashMap<>();

        // Loop over the config block mappings and interpret into Replacers
        for (BlockInfo blockInfo : config.blocks) {
            Integer id = fromWorld.blockRegistry.getId(blockInfo.name);
            if (id == null) {
                System.out.println("Skipping unknown block " + blockInfo.name);
                continue;
            }
            max = id > max ? id : max;
            List<Replacer> list = filter.getOrDefault(id, new ArrayList<>());
            addReplacer(list, blockInfo);
            filter.put(id, list);
        }

        if (Config.autoRemap()) {
            // Search for blocks that have been mapped to different ids in each level.dat file and create
            // a new replace rule so that they transfer properly to the 'toWorld' level.dat registry.
            // Auto-remap rules will execute after any user defined rules
            List<String> remappedBlocks = toWorld.blockRegistry.getRemappedBlocks(fromWorld.blockRegistry);
            for (String block : remappedBlocks) {
                int fromId = fromWorld.blockRegistry.getId(block);
                int toId = toWorld.blockRegistry.getId(block);
                Replacer replacer = Replacers.matchTypeReplaceType(fromId, toId);
                List<Replacer> list = filter.getOrDefault(fromId, new ArrayList<>());
                list.add(replacer);
                max = fromId > max ? fromId : max;
            }
        }

        Replacer[][] replacers = new Replacer[max + 1][];
        filter.entrySet().forEach(e -> {
            List<Replacer> list = filter.get(e.getKey());
            replacers[e.getKey()] = list.toArray(new Replacer[list.size()]);
        });

        return replacers;
    }

    private void addReplacer(List<Replacer> list, BlockInfo blockInfo) {
        // Shouldn't occur unless user has error in config
        if (!blockInfo.present() || blockInfo.to == null) {
            return;
        }

        int fromId = fromWorld.blockRegistry.getId(blockInfo.name);
        int fromMinData = blockInfo.min;
        int fromMaxData = blockInfo.max;

        int toId = toWorld.blockRegistry.getId(blockInfo.to.name);
        int toMinData = blockInfo.to.min;
        int toMaxData = blockInfo.to.max;

        Replacer replacer = null;

        if (fromMinData == fromMaxData) { // match one specific meta data value
            if (toMinData == fromMinData) { // only change block's id
                replacer = Replacers.matchTypeAndDataReplaceType(fromId, toId, fromMinData);
            } else if (toId == fromId) { // only change block's meta data
                replacer = Replacers.matchTypeAndDataReplaceData(fromId, fromMinData, toMinData);
            } else { // change both id and data
                replacer = Replacers.matchTypeAndDataReplaceTypeAndData(fromId, toId, blockInfo.min, toMinData);
            }
        } else { // match a range of meta data values
            if (toId == fromId) { // only change meta data
                if (toMinData == toMaxData) { // world range to single meta value
                    replacer = Replacers.rangeMatchTypeReplaceData(fromId, fromMinData, fromMaxData, toMinData);
                } else if (blockInfo.to.validRange() && blockInfo.dataRange() == blockInfo.to.dataRange()) { // world range to another range
                    replacer = Replacers.rangeMatchTypeReplaceDataRange(fromId, fromMinData, fromMaxData, toMinData);
                }
            } else {
                if (toMinData == toMaxData) { // world range to single meta value
                    replacer = Replacers.rangeMatchTypeReplaceTypeAndData(fromId, toId, fromMinData, fromMaxData, toMinData);
                } else if (blockInfo.to.validRange() && blockInfo.dataRange() == blockInfo.to.dataRange()) { // world range to another range
                    replacer = Replacers.rangeMatchTypeReplaceTypeAndDataRange(fromId, toId, fromMinData, fromMaxData, toMinData);
                }
            }
        }

        if (replacer == null) {
            return;
        }

        // if a biome id is defined, link the currently interpreted replacer to a biome replacer
        if (blockInfo.biome >= 0) {
            replacer = Replacers.matchBiomeWithReplacer(blockInfo.biome, replacer);
        }

        list.add(replacer);
    }

    static List<File> listDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                return Arrays.asList(files);
            }
        }
        return Collections.emptyList();
    }

    static boolean hasRegionsDir(File dir) {
        for (File file : listDir(dir)) {
            if (file.isDirectory() && file.getName().equalsIgnoreCase("region")) {
                return true;
            }
        }
        return false;
    }
}
