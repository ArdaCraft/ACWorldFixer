package me.dags.massblockr.client.headless;

import me.dags.massblockr.App;
import me.dags.massblockr.client.Client;
import me.dags.massblockr.client.Options;
import me.dags.massblockr.util.Flags;

import java.io.File;

/**
 * @author dags <dags@dags.me>
 */
public class HeadlessApp implements App {

    private static final String TARGET_WORLD = "world";
    private static final String CUSTOM_LEVEL = "level";
    private static final String THREAD_COUNT = "cores";
    private static final String REMAP = "remap";
    private static final String REMAP_ONLY = "remapOnly";
    private static final String SCHEMS_ONLY = "schemOnly";

    @Override
    public Client newClient(Options options) {
        return new HeadlessClient(options.threads);
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void launch(String[] args) {
        for (String arg : args) {
            if (arg.equals("-?") || arg.equals("-help")) {
                printHelp();
                return;
            }
        }

        Flags flags = Flags.parse(args);
        if (!flags.containsKey(TARGET_WORLD)) {
            System.out.printf("World directory required: --%s 'path/to/world'\n", TARGET_WORLD);
            return;
        }

        Options options = new Options();
        options.schemsOnly = flags.getFlag(SCHEMS_ONLY, Boolean::new, false);
        options.remapOnly = flags.getFlag(REMAP_ONLY, Boolean::new, false);
        options.remap = options.remapOnly || flags.getFlag(REMAP, Boolean::new, false);
        options.threads = flags.getFlag(THREAD_COUNT, Integer::new, Runtime.getRuntime().availableProcessors());
        options.world = new File(flags.get(TARGET_WORLD)).getAbsoluteFile();
        options.level = flags.getFlag(CUSTOM_LEVEL, s -> new File(s).getAbsoluteFile(), null);

        checkWorld(options.world);
        checkLevel(options.level);

        submit(options);
    }

    private void checkWorld(File file) {
        if (file.exists()) {
            if (!new File(file, "level.dat").exists()) {
                throw new IllegalArgumentException("The provided world mustDir does not contain a level.dat file");
            }
        } else {
            throw new IllegalArgumentException("The provided world mustDir does not exist: " + file);
        }
    }

    private void checkLevel(File file) {
        if (file != null && !file.exists()) {
            throw new IllegalArgumentException("The provided custom level.dat does not exist: " + file);
        }
    }

    private void printHelp() {
        System.out.println("Help:");
        System.out.println(" -help               - prints the commands");
        System.out.println(" -?                  - prints the commands");

        System.out.println();
        System.out.println("Required:");
        System.out.println("--world 'mustDir/path'   - the world directory");

        System.out.println();
        System.out.println("Optional:");
        System.out.println("--level 'level.dat'  - use a custom level.dat file    - default = internal");
        System.out.println("--cores #integer     - the number of threads to use   - default = number of cores");
        System.out.println("--remap #boolean     - fix mismatching block ids      - default = false");
        System.out.println(" -remap                                               - value   = true");
        System.out.println("--remapOnly #boolean - only fix mismatching block ids - default = false");
        System.out.println(" -remapOnly                                           - value   = false");
        System.out.println("--schemOnly #boolean - only convert schematics        - default = false");
        System.out.println(" -schemOnly          - as above                       - value   = false");
    }
}
