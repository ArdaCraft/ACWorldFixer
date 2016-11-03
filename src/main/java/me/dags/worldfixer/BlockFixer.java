package me.dags.worldfixer;

import me.dags.blockinfo.BlockInfo;
import me.dags.blockinfo.Config;
import me.dags.worldfixer.block.ChangeStats;
import me.dags.worldfixer.block.ReplaceTask;
import me.dags.worldfixer.block.replacers.Replacer;
import me.dags.worldfixer.block.replacers.Replacers;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class BlockFixer {

    private static volatile boolean finished = false;

    private final WorldData worldData;
    private final Config config;
    private final int cores;

    public BlockFixer(Config config, WorldData worldData, int cores) {
        this.worldData = worldData;
        this.config = config;
        this.cores = cores;
    }

    public void run(JFrame frame, JProgressBar progressBar) {
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
            }
        }
        frame.dispose();
        ChangeStats.punchOut();
        ChangeStats.displayResults(cores);
    }

    private void execute(List<ReplaceTask> tasks, int cores) {
        new Thread(() -> {
            ExecutorService service = Executors.newFixedThreadPool(cores);
            tasks.forEach(service::submit);
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

    private Replacer[][] getRules() {
        int max = 0;
        Map<Integer, List<Replacer>> filter = new HashMap<>();
        for (BlockInfo blockInfo : config.blocks) {
            Integer id = worldData.blockRegistry.getId(blockInfo.name);
            if (id == null) {
                System.out.println("Skipping unknown block " + blockInfo.name);
                continue;
            }
            max = id > max ? id : max;
            List<Replacer> list = filter.getOrDefault(id, new ArrayList<>());
            addReplacer(list, blockInfo);
            filter.put(id, list);
        }
        Replacer[][] replacers = new Replacer[max + 1][];
        filter.entrySet().forEach(e -> {
            List<Replacer> list = filter.get(e.getKey());
            replacers[e.getKey()] = list.toArray(new Replacer[list.size()]);
        });
        return replacers;
    }

    private List<ReplaceTask> getTasks(Replacer[][] replacers) {
        return worldData.getRegionFiles()
                .stream()
                .map(file -> new ReplaceTask(file, replacers)
                        .withEntities(config.entities)
                        .withTileEntities(config.tileEntities))
                .collect(Collectors.toList());
    }

    private void addReplacer(List<Replacer> list, BlockInfo blockInfo) {
        if (blockInfo.to == null) {
            return;
        }
        int fromId = worldData.blockRegistry.getId(blockInfo.name);
        int toId = worldData.blockRegistry.getId(blockInfo.to.name);
        int toData = blockInfo.to.min;
        Replacer replacer;
        if (blockInfo.min == blockInfo.max) { // matching specific meta data value
            if (toData == blockInfo.min) { // only change block's id
                replacer = Replacers.matchTypeAndDataReplaceType(fromId, toId, blockInfo.min);
            } else if (toId == fromId) { // only change block's meta data
                replacer = Replacers.matchTypeAndDataReplaceData(fromId, blockInfo.min, toData);
            } else { // change both id and data
                replacer = Replacers.matchTypeAndDataReplaceTypeAndData(fromId, toId, blockInfo.min, toData);
            }
        } else { // matching a range of meta data values
            if (toId == fromId) { // not changing block's id
                replacer = Replacers.rangeMatchTypeReplaceData(fromId, blockInfo.min, blockInfo.max, toData);
            } else { // change both id and data
                replacer = Replacers.matchTypeAndDataReplaceTypeAndData(fromId, toId, blockInfo.min, toData);
            }
        }

        if (blockInfo.biome >= 0) {
            replacer = Replacers.matchBiomeWithReplacer(blockInfo.biome, replacer);
        }

        list.add(replacer);
    }
}
