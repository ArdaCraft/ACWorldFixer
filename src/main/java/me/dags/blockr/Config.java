package me.dags.blockr;

import me.dags.blockr.block.BlockInfo;
import me.dags.data.node.*;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    public static transient boolean auto_remap = false;
    public static transient boolean do_entities = true;

    public final List<BlockInfo> blocks = new ArrayList<>();
    public final List<BlockInfo> copyBelow = new ArrayList<>();
    public final Map<String, Integer> removeBlocks = new HashMap<>();
    public final Set<String> entities = new HashSet<>();
    public final Set<String> tileEntities = new HashSet<>();

    public static class Adapter implements NodeTypeAdapter<Config> {

        @Override
        public Node toNode(Config config) {
            NodeObject object = new NodeObject();
            NodeArray blocks = new NodeArray();
            config.blocks.forEach(blocks::add);
            object.put("blocks", blocks);

            NodeArray belowBlocks = new NodeArray();
            config.copyBelow.forEach(belowBlocks::add);
            object.put("copy_below", belowBlocks);

            NodeObject removeBlocks = new NodeObject();
            config.removeBlocks.entrySet().forEach(e -> removeBlocks.put(e.getKey(), e.getValue()));
            object.put("remove_blocks", removeBlocks);
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
                            .filter(Objects::nonNull)
                            .forEach(config.blocks::add);
                }
                if (object.contains("copy_below")) {
                    NodeArray below = object.getArray("copy_below");
                    below.values().stream()
                            .map(n -> NodeTypeAdapters.of(BlockInfo.class).fromNode(n))
                            .filter(Objects::nonNull)
                            .forEach(config.copyBelow::add);
                }
            }
            return config;
        }
    }
}
