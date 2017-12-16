package me.dags.massblockr.minecraft.block;

import me.dags.massblockr.util.OrderedMap;

/**
 * @author dags <dags@dags.me>
 */
public class BlockState {

    private static final OrderedMap<String, Object> EMPTY = new OrderedMap<>();
    public static final BlockState AIR = new BlockState(Block.AIR);

    private final int meta;
    private final Block block;
    private final OrderedMap<String, Object> properties;

    public BlockState(Block block) {
        this(block, EMPTY, 0);
    }

    public BlockState(Block block, OrderedMap<String, Object> properties, int meta) {
        this.meta = meta;
        this.block = block;
        this.properties = properties;
    }

    public int getMeta() {
        return meta;
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(48);
        sb.append(block);
        properties.appendTo(sb);
        return sb.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private OrderedMap<String, Object> map = EMPTY;
        private int meta = 0;

        public Builder put(String key, Object value) {
            if (map == EMPTY) {
                map = new OrderedMap<>();
            }
            map.put(key, value);
            return this;
        }

        public Builder meta(int meta) {
            this.meta = meta;
            return this;
        }

        public BlockState build(Block block) {
            return new BlockState(block, map, meta);
        }
    }

    public static void parse(BlockState.Builder builder, String properties) {
        if (properties.length() == 0) {
            return;
        }

        for (int start = 0, end; start < properties.length(); start = end) {
            end = properties.indexOf('=', start);
            String key = properties.substring(start, end);

            start = end + 1;
            end = (end = properties.indexOf(',', start)) == -1 ? properties .length() : end;

            String value = properties.substring(start, end);
            end += 1;

            builder.put(key, value);
        }
    }
}
