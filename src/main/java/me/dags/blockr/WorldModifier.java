package me.dags.blockr;

import me.dags.blockr.block.ChangeStats;
import me.dags.blockr.block.ReplaceTask;
import me.dags.blockr.block.replacers.Replacer;
import me.dags.blockr.block.replacers.Replacers;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class WorldModifier {

    private static volatile boolean finished = false;

    private final WorldData fromWorld;
    private final WorldData toWorld;
    private final Config config;
    private final File worldIn;
    private final File worldOut;
    private final File regionsIn;
    private final int cores;

    public WorldModifier(Config config, WorldData fromWorld, WorldData toWorld, File worldDir, File outputDir, int cores) {
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        this.config = config;
        this.cores = cores;
        this.worldIn = worldDir;
        this.worldOut = new File(outputDir, worldDir.getName());
        this.regionsIn = new File(worldIn, "region");
    }

    public void run(JFrame frame, JProgressBar progressBar) {
        try {
            copy(worldIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Replacer[][] replacers = getRules();
        List<ReplaceTask> tasks = getTasks(replacers);
        submitTasks(tasks, frame, progressBar);
    }

    private void submitTasks(List<ReplaceTask> tasks, JFrame frame, JProgressBar progressBar) {
        final int total = tasks.size();

        progressBar.setMinimum(0);
        progressBar.setMaximum(total);

        ChangeStats.punchIn();

        execute(tasks, cores);
        long time = System.currentTimeMillis();
        while (!finished) {
            if (System.currentTimeMillis() - time > 500) {
                time = System.currentTimeMillis();
                int complete = ChangeStats.getProgress();
                progressBar.setValue(complete);
                progressBar.setString(String.format("Processed %s of %s regions!", complete, total));
                progressBar.repaint();
            }
        }
        toWorld.writeLevelData(new File(worldOut, "level.dat"));
        frame.dispose();
        ChangeStats.punchOut();
        ChangeStats.displayResults(cores);
    }

    private void execute(List<ReplaceTask> tasks, int cores) {
        new Thread(() -> {
            ExecutorService service = Executors.newFixedThreadPool(cores);
            tasks.forEach(service::execute);
            service.shutdown();
            while (!service.isTerminated()) {
                try {
                    service.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            finished = true;
        }).start();
    }

    private void copy(File in) throws IOException {
        if (in.isDirectory()) {
            if (in.equals(regionsIn)) {
                toOutputFile(in).mkdirs();
                return;
            }
            File[] files = in.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        System.out.println(file);
                    }
                    copy(file);
                }
            }
        } else {
            File out = toOutputFile(in);
            out.getParentFile().mkdirs();
            try (FileInputStream from = new FileInputStream(in); FileOutputStream to = new FileOutputStream(out)) {
                from.getChannel().transferTo(0, Long.MAX_VALUE, to.getChannel());
            }
        }
    }

    private File toOutputFile(File in) {
        String path = in.getAbsolutePath().substring(worldIn.getAbsolutePath().length());
        return new File(worldOut, path);
    }

    private Replacer[][] getRules() {
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
            List<String> remappedBlocks = toWorld.blockRegistry.getRemmappedBlocks(fromWorld.blockRegistry);
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

    private List<ReplaceTask> getTasks(Replacer[][] replacers) {
        return getRegionFiles(regionsIn)
                .map(file -> new ReplaceTask(file, toOutputFile(file), replacers)
                        .withEntities(config.entities)
                        .withTileEntities(config.tileEntities))
                .collect(Collectors.toList());
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
                if (toMinData == toMaxData) { // convert range to single meta value
                    replacer = Replacers.rangeMatchTypeReplaceData(fromId, fromMinData, fromMaxData, toMinData);
                } else if (blockInfo.to.validRange() && blockInfo.dataRange() == blockInfo.to.dataRange()) { // convert range to another range
                    replacer = Replacers.rangeMatchTypeReplaceDataRange(fromId, fromMinData, fromMaxData, toMinData);
                }
            } else {
                if (toMinData == toMaxData) { // convert range to single meta value
                    replacer = Replacers.rangeMatchTypeReplaceTypeAndData(fromId, toId, fromMinData, fromMaxData, toMinData);
                } else if (blockInfo.to.validRange() && blockInfo.dataRange() == blockInfo.to.dataRange()) { // convert range to another range
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

    private static Stream<File> getRegionFiles(File region) {
        File[] files = region.listFiles();
        if (files != null && files.length > 0) {
            return Stream.of(files).filter(file -> file.getName().endsWith(".mca"));
        }
        return Stream.empty();
    }
}
