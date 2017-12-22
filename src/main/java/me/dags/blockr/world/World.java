package me.dags.blockr.world;

import me.dags.blockr.Config;
import me.dags.blockr.block.BlockInfo;
import me.dags.blockr.client.Client;
import me.dags.blockr.replacer.Replacer;
import me.dags.blockr.replacer.Replacers;
import me.dags.blockr.task.ChangeStats;
import me.dags.blockr.task.ExtentTask;
import org.jnbt.CompoundTag;

import java.io.File;
import java.util.*;

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
    private final Config config;

    public World(File sourceDir, File outputDir, WorldData fromWorld, WorldData toWorld, Config config) {
        this.outputDirRoot = outputDir;
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        this.config = config;

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

    public void convert(Client client) {
        countTasks();
        client.setup();
        client.start();

        ChangeStats.punchIn();
        for (Dimension dimension : dimensions) {
            try {
                ChangeStats.dimTasksComplete.set(0);
                client.setDimension(dimension.getName());

                List<ExtentTask<?>> tasks = new LinkedList<>();
                dimension.addRegionTasks(config, tasks);
                dimension.addSchematicTasks(config, tasks);
                ChangeStats.dimTaskTotal.set(tasks.size());

                dimension.mkdirs();
                client.getExecutor().invokeAll(tasks);
                dimension.copyData();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                ChangeStats.incDimensionCount();
            }
        }

        CompoundTag level = WorldData.mergeLevels(fromWorld, toWorld);
        WorldData.writeLevelData(level, main.getOutputLevelFile());
        ChangeStats.punchOut();

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ChangeStats.running.set(false);
        client.finish(outputDirRoot);
    }

    private void countTasks() {
        for (Dimension dimension : dimensions) {
            ChangeStats.globalTaskTotal.getAndAdd(dimension.countRegionFiles(config));
            ChangeStats.globalTaskTotal.getAndAdd(dimension.countSchematicFiles(config));
        }
    }

    private Replacer[][] getRules(Config config) {
        int max = 0;
        Map<Integer, List<Replacer>> filter = new HashMap<>();

        if (!config.onlyRemap) {
            for (BlockInfo block : config.copyBelow) {
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
        }

        if (config.autoRemap) {
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

        if (conversions.size() == 0) {
            System.out.println("No conversions to run!");
        } else {
            Collections.sort(conversions);
            conversions.forEach(System.out::println);
        }

        Replacer[][] replacers = new Replacer[max + 1][];
        filter.forEach((key, value) -> {
            List<Replacer> list = filter.get(key);
            replacers[key] = list.toArray(new Replacer[list.size()]);
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

    static void listDirRecursive(File file, List<File> collector, boolean includeDirs) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    listDirRecursive(f, collector, includeDirs);
                }
            }
            if (includeDirs) {
                collector.add(file);
            }
        } else {
            collector.add(file);
        }
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
