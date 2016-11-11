package me.dags.blockr;

import me.dags.data.node.*;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    private static transient boolean auto_remap = false;

    public final List<BlockInfo> blocks = new ArrayList<>();
    public final Map<String, Integer> removeBlocks = new HashMap<>();
    public final Set<String> entities = new HashSet<>();
    public final Set<String> tileEntities = new HashSet<>();

    public static boolean autoRemap() {
        return Config.auto_remap;
    }

    public static void setAutoRemap(boolean b) {
        Config.auto_remap = b;
    }

    public static class Adapter implements NodeTypeAdapter<Config> {

        @Override
        public Node toNode(Config config) {
            NodeObject object = new NodeObject();
            NodeArray blocks = new NodeArray();
            config.blocks.forEach(blocks::add);
            object.put("blocks", blocks);

            NodeObject removeBlocks = new NodeObject();
            config.removeBlocks.entrySet().forEach(e -> removeBlocks.put(e.getKey(), e.getValue()));
            object.put("remove_blocks", removeBlocks);

            NodeArray entities = new NodeArray();
            config.entities.forEach(entities::add);
            object.put("entities", entities);

            NodeArray tileEntities = new NodeArray();
            config.tileEntities.forEach(tileEntities::add);
            object.put("tile_entities", tileEntities);
            return object;
        }

        @Override
        public Config fromNode(Node node) {
            if (!node.isNodeObject()) {
                return new Config();
            }
            Config config = new Config();
            if (node.isNodeObject()) {
                NodeObject object = node.asNodeObject();
                if (object.contains("blocks")) {
                    NodeArray array = object.getArray("blocks");
                    array.values().stream()
                            .map(n -> NodeTypeAdapters.of(BlockInfo.class).fromNode(n))
                            .filter(b -> b != null)
                            .forEach(config.blocks::add);
                }
                if (object.contains("remove_blocks")) {
                    NodeObject remove = object.getObject("remove_blocks");
                    remove.entries().forEach(e -> config.removeBlocks.put(e.getKey().asString(), e.getValue().asNumber().intValue()));
                }
                if (object.contains("entities")) {
                    NodeArray array = object.getArray("entities");
                    array.values().stream().map(n -> n.asString().toLowerCase()).forEach(config.entities::add);
                }
                if (object.contains("tile_entities")) {
                    NodeArray array = object.getArray("tile_entities");
                    array.values().stream().map(n -> n.asString().toLowerCase()).forEach(config.tileEntities::add);
                }
            }
            return config;
        }
    }
}
