package me.dags.blockr.block;

import me.dags.data.node.Node;
import me.dags.data.node.NodeObject;
import me.dags.data.node.NodeTypeAdapter;

/**
 * @author dags <dags@dags.me>
 */
public class BlockInfo {

    public static BlockInfo EMPTY = new BlockInfo();

    public final String name;
    public final int min;
    public final int max;
    public final int biome;
    public final BlockInfo to;

    private BlockInfo() {
        this.name = "EMPTY";
        this.min = -1;
        this.max = -1;
        this.biome = -1;
        this.to = this;
    }

    private BlockInfo(BlockInfo info) {
        this.name = info.name;
        this.min = info.min;
        this.max = info.max;
        this.biome = info.biome;
        this.to = info.to;
    }

    public BlockInfo(String name, int biome, int min, int max, BlockInfo to) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.to = to;
        this.biome = biome;
    }

    public int dataRange() {
        return max - min;
    }

    public boolean validRange() {
        return min + dataRange() < 16;
    }

    public boolean present() {
        return this != EMPTY;
    }

    public BlockInfo copy() {
        return present() ? new BlockInfo(name, biome, min, max, (to.present() ? new BlockInfo(to) : EMPTY)) : EMPTY;
    }

    @Override
    public String toString() {
        return "from=" + name + " to=" + to;
    }

    public static class Adapter implements NodeTypeAdapter<BlockInfo> {

        @Override
        public Node toNode(BlockInfo blockInfo) {
            NodeObject nodeObject = new NodeObject();
            nodeObject.put("name", blockInfo.name);
            nodeObject.put("min", blockInfo.min);
            nodeObject.put("max", blockInfo.max);
            nodeObject.put("biome", blockInfo.biome);
            if (blockInfo.to.present()) {
                NodeObject to = new NodeObject();
                to.put("name", blockInfo.to.name);
                to.put("data", blockInfo.to.min);
                nodeObject.put("to", to);
            }
            return nodeObject;
        }

        @Override
        public BlockInfo fromNode(Node node) {
            System.out.println(node);
            if (node.isNodeObject()) {
                String name = node.asNodeObject().get("name").asString();
                int min = node.asNodeObject().get("min").asNumber().intValue();
                int max = node.asNodeObject().get("max").asNumber().intValue();
                int biome = -1;
                if (node.asNodeObject().contains("biome")) {
                    biome = node.asNodeObject().get("biome").asNumber().intValue();
                }
                if (node.asNodeObject().contains("to")) {
                    NodeObject toObject = node.asNodeObject().get("to").asNodeObject();
                    String toName = toObject.get("name").asString();
                    int toMin = toObject.get("data").asNumber().intValue();
                    return new BlockInfo(name, biome, min, max, new BlockInfo(toName, -1, toMin, toMin, EMPTY));
                }
                return new BlockInfo(name, biome, min, min, EMPTY);
            }
            return null;
        }
    }
}