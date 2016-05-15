package me.dags.blockinfo;

import me.dags.data.node.Node;
import me.dags.data.node.NodeAdapter;
import me.dags.data.node.NodeObject;

/**
 * @author dags <dags@dags.me>
 */
public class BlockInfo {

    public final String name;
    public final int min;
    public final int max;
    public final BlockInfo to;

    public BlockInfo(String name, int data, boolean derp) {
        this.name = name;
        this.min = data;
        this.max = data;
        this.to = null;
    }

    public BlockInfo(String name, int data) {
        this(name, data, new BlockInfo(name, 0, false));
    }

    public BlockInfo(String name, int data, BlockInfo to) {
        this.name = name;
        this.min = data;
        this.max = 16;
        this.to = to;
    }

    public BlockInfo(String name, int min, int max, BlockInfo to) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.to = to;
    }

    public static class Adapter implements NodeAdapter<BlockInfo> {

        @Override
        public Node toNode(BlockInfo blockInfo) {
            NodeObject nodeObject = new NodeObject();
            nodeObject.put("name", blockInfo.name);
            nodeObject.put("min", blockInfo.min);
            nodeObject.put("max", blockInfo.max);
            if (blockInfo.to != null) {
                NodeObject to = new NodeObject();
                to.put("name", blockInfo.to.name);
                to.put("data", blockInfo.to.min);
                nodeObject.put("to", to);
            }
            return nodeObject;
        }

        @Override
        public BlockInfo fromNode(Node node) {
            if (node.isNodeObject()) {
                String name = node.asNodeObject().get("name").asString();
                int min = node.asNodeObject().get("min").asNumber().intValue();
                int max = node.asNodeObject().get("max").asNumber().intValue();
                if (node.asNodeObject().contains("to")) {
                    NodeObject toObject = node.asNodeObject().get("to").asNodeObject();
                    String toName = toObject.get("name").asString();
                    int toMin = toObject.get("data").asNumber().intValue();
                    return new BlockInfo(name, min, max, new BlockInfo(toName, toMin, false));
                }
                return new BlockInfo(name, min);
            }
            return null;
        }
    }
}