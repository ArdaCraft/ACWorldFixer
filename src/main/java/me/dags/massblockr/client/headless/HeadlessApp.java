package me.dags.massblockr.client.headless;

import com.google.common.base.Preconditions;
import me.dags.massblockr.App;
import me.dags.massblockr.ConverterOptions;
import me.dags.massblockr.client.Client;
import me.dags.massblockr.util.Flags;

/**
 * @author dags <dags@dags.me>
 */
public class HeadlessApp implements App {

    private static final String WORLD_IN = "worldIn";
    private static final String WORLD_OUT = "worldOut";
    private static final String REGISTRY_IN = "regIn";
    private static final String REGISTRY_OUT = "regOut";
    private static final String CUSTOM_LEVEL = "level";
    private static final String THREAD_COUNT = "cores";
    private static final String SCHEMS_ONLY = "schemOnly";

    @Override
    public Client newClient(ConverterOptions options) {
        return new HeadlessClient(options.threadCount);
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
        if (!flags.containsKey(WORLD_IN)) {
            System.out.printf("World directory required: --%s 'path/to/world'\n", WORLD_IN);
            return;
        }

        ConverterOptions options = new ConverterOptions();
        options.worldIn = flags.getOrDefault(WORLD_IN, "");
        options.worldOut = flags.getOrDefault(WORLD_OUT, "");
        options.registryIn = flags.getOrDefault(REGISTRY_IN, "");
        options.registryOut = flags.getOrDefault(REGISTRY_OUT, "");
        options.levelOut = flags.getOrDefault(CUSTOM_LEVEL, "");
        options.schemsOnly = flags.getFlag(SCHEMS_ONLY, Boolean::new, false);
        options.threadCount = flags.getFlag(THREAD_COUNT, Integer::new, Runtime.getRuntime().availableProcessors());

        Preconditions.checkState(options.worldIn.isEmpty(), "No world directory provided: --worldIn");
        submit(options);
    }

    private void printHelp() {
        System.out.println("Help:");
        System.out.println(" -help               - prints the commands");
        System.out.println(" -?                  - prints the commands");

        System.out.println();
        System.out.println("Required:");
        System.out.println("--worldIn 'dir/path'   - the world directory");

        System.out.println();
        System.out.println("Optional:");
        System.out.println("--worldOut 'output'      - set a specific output dir        - default = -converted");
        System.out.println("--regIn 'registry.json'  - provide a registry for worldIn   - default = internal");
        System.out.println("--regOut 'registry.json' - provide a registry for worldOut  - default = internal");
        System.out.println("--level 'level.dat'      - provide a level.dat for worldOut - default = internal");
        System.out.println("--threads #integer       - the number of threads to use     - default = number of cores");
        System.out.println("--schemOnly #boolean     - only convert schematics          - default = false");
        System.out.println(" -schemOnly              - as above                         - value   = false");
    }
}
