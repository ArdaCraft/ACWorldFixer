package me.dags.worldfixer;

import me.dags.worldfixer.block.ChangeStats;
import me.dags.worldfixer.block.ReplaceTask;
import me.dags.worldfixer.block.replacers.Replacer;
import me.dags.worldfixer.block.replacers.Replacers;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class WorldModifier {

    private static volatile boolean finished = false;

    private final WorldData worldData;
    private final Config config;
    private final File worldIn;
    private final File worldOut;
    private final File regionsIn;
    private final int cores;

    public WorldModifier(Config config, WorldData worldData, File worldDir, File outputDir, int cores) {
        this.worldData = worldData;
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
                progressBar.setString(String.format("Processed %s of %s regionsIn!", complete, total));
                progressBar.repaint();
            }
        }
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
        return getRegionFiles(regionsIn).stream()
                .map(file -> new ReplaceTask(file, toOutputFile(file), replacers)
                        .withEntities(config.entities)
                        .withTileEntities(config.tileEntities))
                .collect(Collectors.toList());
    }

    private void addReplacer(List<Replacer> list, BlockInfo blockInfo) {
        if (!blockInfo.present() || blockInfo.to == null) {
            System.out.println("Skipping " + blockInfo);
            return;
        }
        int fromId = worldData.blockRegistry.getId(blockInfo.name);
        int toId = worldData.blockRegistry.getId(blockInfo.to.name);
        int toData = blockInfo.to.min;
        Replacer replacer;
        if (blockInfo.min == blockInfo.max) { // match one specific meta data value
            if (toData == blockInfo.min) { // only change block's id
                replacer = Replacers.matchTypeAndDataReplaceType(fromId, toId, blockInfo.min);
            } else if (toId == fromId) { // only change block's meta data
                replacer = Replacers.matchTypeAndDataReplaceData(fromId, blockInfo.min, toData);
            } else { // change both id and data
                replacer = Replacers.matchTypeAndDataReplaceTypeAndData(fromId, toId, blockInfo.min, toData);
            }
        } else { // match a range of meta data values
            if (toId == fromId) { // not changing block's id
                replacer = Replacers.rangeMatchTypeReplaceData(fromId, blockInfo.min, blockInfo.max, toData);
            } else { // change both id and data
                replacer = Replacers.rangeMatchTypeReplaceTypeAndData(fromId, toId, blockInfo.min, blockInfo.max, toData);
            }
        }

        if (blockInfo.biome >= 0) {
            replacer = Replacers.matchBiomeWithReplacer(blockInfo.biome, replacer);
        }

        list.add(replacer);
    }

    private static List<File> getRegionFiles(File region) {
        File[] files = region.listFiles();
        if (files != null && files.length > 0) {
            List<File> mca_files = new ArrayList<>();
            for (File f : files) {
                if (f.getName().endsWith(".mca")) {
                    mca_files.add(f);
                }
            }
            return mca_files;
        }
        return Collections.emptyList();
    }
}
