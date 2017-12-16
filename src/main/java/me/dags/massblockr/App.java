package me.dags.massblockr;

import me.dags.massblockr.client.Client;
import me.dags.massblockr.client.Options;

/**
 * @author dags <dags@dags.me>
 */
public interface App {

    void launch(String[] args);

    void onError(Throwable t);

    Client newClient(Options options);

    default void submit(Options options) {
        System.out.printf("Converting with options: %s\n", options);

//        final WorldFolder world;
//
//        try (InputStream in = App.class.getResourceAsStream("/mappings.json")) {
//            File source = options.world;
//            File output = new File(source.getParent());
//
//            WorldData from = new WorldDataFile(new File(source, "level.dat"));
//            from.loadRegistry();
//
//            WorldData to = options.level == null ? new WorldDataResource("/level.dat") : new WorldDataFile(options.level);
//            to.loadRegistry();
//
//            Config config = new Config();// NodeTypeAdapters.of(Config.class).fromNode(NodeAdapter.json().from(in));
//            config.autoRemap = options.remap;
//            config.onlyRemap = options.remapOnly;
//            config.schematicsOnly = options.schemsOnly;
//
//            world = new WorldFolder(source, output, from, to, config);
//        } catch (Throwable t) {
//            onError(t);
//            return;
//        }
//
//        Client client = newClient(options);
//        new Thread(() -> world.convert(client)).start();
    }
}
