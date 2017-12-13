package me.dags.blockr;

import me.dags.blockr.client.Client;
import me.dags.blockr.world.World;
import me.dags.blockr.world.WorldData;
import me.dags.blockr.world.WorldDataFile;
import me.dags.blockr.world.WorldDataResource;
import me.dags.data.NodeAdapter;
import me.dags.data.node.NodeTypeAdapters;

import java.io.File;
import java.io.InputStream;

/**
 * @author dags <dags@dags.me>
 */
public interface App {

    void launch(String[] args);

    void onError(Throwable t);

    Client newClient(Options options);

    default void submit(Options options) {
        System.out.printf("Converting with options: %s\n", options);

        final World world;

        try (InputStream in = App.class.getResourceAsStream("/mappings.json")) {
            File source = options.world;
            File output = new File(source.getParent());

            WorldData from = new WorldDataFile(new File(source, "level.dat"));
            from.loadRegistry();

            WorldData to = options.level == null ? new WorldDataResource("/level.dat") : new WorldDataFile(options.level);
            to.loadRegistry();

            Config config = NodeTypeAdapters.of(Config.class).fromNode(NodeAdapter.json().from(in));
            config.autoRemap = options.remap;
            config.onlyRemap = options.remapOnly;
            config.schematicsOnly = options.schemsOnly;

            world = new World(source, output, from, to, config);
        } catch (Throwable t) {
            onError(t);
            return;
        }

        Client client = newClient(options);
        new Thread(() -> world.convert(client)).start();
    }
}
