package me.dags.massblockr.minecraft.block;

import com.google.common.collect.ImmutableMap;
import me.dags.massblockr.minecraft.world.World;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
public class Matcher {

    static final Object ANY = new Object() {
        public String toString() {
            return "ANY";
        }
    };

    private final Block block;
    private final Map<String, Object> properties;

    public Matcher(Block block, Map<String, Object> properties) {
        this.block = block;
        this.properties = properties;
    }

    @Override
    public String toString() {
        return block.getId() + properties;
    }

    public Merger toMerger(Matcher matcher) {
        return new Merger(block, matcher, properties);
    }

    public Block getBlock() {
        return block;
    }

    public boolean matches(BlockState state) {
        if (state.getBlock() != block) {
            System.out.println("Skipping " + state);
            return false;
        }

        for (Map.Entry<String, Object> e : properties.entrySet()) {
            String lookup = e.getKey();
            Object match = e.getValue();

            Object value = state.getProperty(lookup);
            // state doesn't own the property, state's value not equal to match, match isn't the ANY wildcard
            if (value == null || (match != ANY && !value.equals(match))) {
                return false;
            }
        }

        return true;
    }

    public static Matcher parse(World world, String input) {
        int end = indexOf(input, '[', 0, String::length);
        String name = input.substring(0, end);
        Block block = world.getRegistry().getBlock(name);

        if (end + 2 < input.length()) {
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            for (int start = end + 1; start < input.length(); start = end) {
                end = input.indexOf('=', start);
                String key = input.substring(start, end);

                start = end + 1;
                end = (end = input.indexOf(',', start)) == -1 ? input.length() - 1 : end;

                Object value = input.substring(start, end);
                end += 1;

                if (value.equals("*")) {
                    value = ANY;
                }

                builder.put(key, value);
            }
            return new Matcher(block, builder.build());
        } else {
            return new Matcher(block, Collections.emptyMap());
        }
    }

    private static int indexOf(String in, char c, int from, Function<String, Integer> fallback) {
        int index = in.indexOf(c, from);
        if (index == -1) {
            return fallback.apply(in);
        }
        return index;
    }
}
