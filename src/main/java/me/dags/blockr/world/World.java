package me.dags.blockr.world;

import me.dags.blockr.Config;
import me.dags.blockr.app.SetupWindow;
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

/**
 * @author dags <dags@dags.me>
 */
public class World {

    private static List<String> conversions = new LinkedList<>();

    private final File outputDirRoot;
    private final WorldData fromWorld;
    private final WorldData toWorld;
    private final Dimension main;
    private final List<Dimension> dimensions = new ArrayList<>();
    private final int cores;

    public World(File sourceDir, File outputDir, WorldData fromWorld, WorldData toWorld, Config config, int cores) {
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
        final ExecutorService service = Executors.newFixedThreadPool(cores);

        final JLabel overallLabel = new JLabel();
        overallLabel.setText("Overall:");

        final JLabel dimensionLabel = new JLabel();
        dimensionLabel.setText("World:");
        dimensionLabel.setPreferredSize(new java.awt.Dimension(100, 30));

        final JProgressBar overallProgress = new JProgressBar();
        overallProgress.setPreferredSize(new java.awt.Dimension(200, 30));
        overallProgress.setValue(0);
        overallProgress.setMinimum(0);
        overallProgress.setMaximum(dimensions.size());

        final JProgressBar currentProgress = new JProgressBar();
        currentProgress.setPreferredSize(new java.awt.Dimension(200, 30));
        currentProgress.setValue(0);
        currentProgress.setMinimum(0);
        currentProgress.setMaximum(1);

        final JPanel panel = new JPanel();
        panel.add(overallLabel);
        panel.add(overallProgress);
        panel.add(dimensionLabel);
        panel.add(currentProgress);

        final JFrame frame = new JFrame();
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowStateListener(e -> {
            if (!e.getWindow().isVisible()) {
                System.out.println("Shutting down...");
                service.shutdownNow();
            }
        });

        new Thread(() -> {
            while (ChangeStats.running.get()) {
                currentProgress.setMaximum(ChangeStats.regionCount.get());
                currentProgress.setValue(ChangeStats.regionProgress.get());

                overallProgress.setMaximum(ChangeStats.overallRegionCount.get());
                overallProgress.setValue(ChangeStats.getProgress());

                currentProgress.repaint();
                overallProgress.repaint();

                float progress = 100F * ChangeStats.getProgress() / (float) ChangeStats.overallRegionCount.get();
                frame.setTitle(String.format("Converting: %.2f%%", progress));

                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        for (Dimension dimension : dimensions) {
            ChangeStats.overallRegionCount.getAndAdd(dimension.countRegionFiles());
        }

        ChangeStats.punchIn();

        for (Dimension dimension : dimensions) {
            try {
                ChangeStats.regionProgress.set(0);
                dimensionLabel.setText(String.format("Dim: %s", dimension.getName()));

                List<RegionTask> tasks = dimension.getRegionTasks(ChangeStats.regionProgress);
                ChangeStats.regionCount.set(tasks.size());

                dimension.mkdirs();
                service.invokeAll(tasks);
                dimension.copyData();

                ChangeStats.incDimensionCount();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Replace 'from' world's registries with 'to' world's
        fromWorld.copyRegistries(toWorld);
        fromWorld.writeLevelData(main.getOutputLevelFile());

        ChangeStats.punchOut();

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ChangeStats.running.set(false);
        ChangeStats.displayResults(cores);
        JOptionPane.showMessageDialog(null, "Conversion Complete!");
        frame.dispose();

        try {
            Desktop.getDesktop().open(outputDirRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SetupWindow.ok.setEnabled(true);
    }

    private Replacer[][] getRules(Config config) {
        int max = 0;
        Map<Integer, List<Replacer>> filter = new HashMap<>();

        for (BlockInfo block: config.copyBelow) {
            int id = fromWorld.blockRegistry.getId(block.name);
            int fromMin = block.min;
            int fromMax = 15;

            List<Replacer> list = filter.getOrDefault(id, new ArrayList<>());
            list.add(Replacers.matchTypeAndDataReplaceWithBelow(id, fromMin, fromMax));
            filter.put(id, list);
        }

        // Loop over the config block mappings and interpret into Replacers
        for (BlockInfo blockInfo : config.blocks) {
            Integer id = fromWorld.blockRegistry.getId(blockInfo.name);
            if (id == null) {
                System.out.println("Skipping unknown block " + blockInfo.name);
                continue;
            }
            max = id > max ? id : max;
            List<Replacer> list = filter.getOrDefault(id, new ArrayList<>());
            addReplacer(list, blockInfo, config);
            filter.put(id, list);
        }

        if (Config.auto_remap) {
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
                filter.put(fromId, list);
                max = fromId > max ? fromId : max;

                record("(Auto) ", block, block, fromId, toId, -1, -1);
            }
        }

        Collections.sort(conversions);
        conversions.forEach(System.out::println);

        Replacer[][] replacers = new Replacer[max + 1][];
        filter.entrySet().forEach(e -> {
            List<Replacer> list = filter.get(e.getKey());
            replacers[e.getKey()] = list.toArray(new Replacer[list.size()]);
        });

        return replacers;
    }

    private void addReplacer(List<Replacer> list, BlockInfo blockInfo, Config config) {
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

        record(blockInfo, fromId, toId);

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

    private static void record(BlockInfo info, int from, int to) {
        conversions.add(String.format("%s (%s:%s)  ->  %s (%s:%s)", info.name, from, info.min, info.to.name, to, info.to.min));
    }

    private static void record(String pref, String fromName, String toName, int from, int to, int fromDat, int toDat) {
        conversions.add(pref + String.format("%s (%s:%s)  ->  %s (%s:%s)", fromName, from, fromDat, toName, to, toDat));
    }
}
