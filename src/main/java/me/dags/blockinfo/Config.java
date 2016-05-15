package me.dags.blockinfo;

import me.dags.data.json.JsonSerializer;
import me.dags.data.node.Node;
import me.dags.data.node.NodeAdapter;
import me.dags.data.node.NodeArray;
import me.dags.data.node.NodeObject;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    public final List<BlockInfo> blocks = new ArrayList<>();
    public final Map<String, Integer> removeBlocks = new HashMap<>();
    public final Set<String> entities = new HashSet<>();
    public final Set<String> tileEntities = new HashSet<>();

    public static class Adapter implements NodeAdapter<Config> {

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
                    NodeArray array = object.get("blocks").asNodeArray();
                    array.values().stream()
                            .map(n ->  JsonSerializer.pretty().deserialize(n, BlockInfo.class))
                            .filter(b -> b != null)
                            .forEach(config.blocks::add);
                }
                if (object.contains("remove_blocks")) {
                    NodeObject remove = object.get("remove_blocks").asNodeObject();
                    remove.entries().stream()
                            .forEach(e -> config.removeBlocks.put(e.getKey().asString(), e.getValue().asNumber().intValue()));
                }
                if (object.contains("entities")) {
                    NodeArray array = object.get("entities").asNodeArray();
                    array.values().stream().map(n -> n.asString().toLowerCase()).forEach(config.entities::add);
                }
                if (object.contains("tile_entities")) {
                    NodeArray array = object.get("tile_entities").asNodeArray();
                    array.values().stream().map(n -> n.asString().toLowerCase()).forEach(config.tileEntities::add);
                }
            }
            return config;
        }
    }
}
