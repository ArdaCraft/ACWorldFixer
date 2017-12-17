package me.dags.massblockr.minecraft.block;

import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.util.OrderedMap;

import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Merger {

    private final Block blockOut;
    private final Matcher matcher;
    private final Map<String, Object> properties;

    public Merger(Block blockOut, Matcher matcher, Map<String, Object> properties) {
        this.blockOut = blockOut;
        this.matcher = matcher;
        this.properties = properties;
    }

    public Block getBlock() {
        return blockOut;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public boolean matches(BlockState in) {
        return matcher.matches(in);
    }

    public Optional<BlockState> merge(BlockState input) {
        BlockState outBaseState = blockOut.getDefault();

        Map<String, Object> mergedProperties = new OrderedMap<>();
        outBaseState.forEach((property, baseValue) -> {
            // get property if specified by the merger, otherwise get the default value from the base state
            Object merge = properties.getOrDefault(property, baseValue);

            // if merger specifies ANY as the property value, copy it over from the input blockstate
            if (merge == Matcher.ANY) {
                merge = input.getProperty(property);
                if (merge == null) {
                    merge = baseValue; // if the input state doesn't own the property, fall back to using the base value
                }
            }

            mergedProperties.put(property, merge);
        });

        // get the blockstate for the merged set of properties
        return blockOut.parse(mergedProperties);
    }

    public static Merger of(World worldIn, String input, World worldOut, String output) {
        Matcher inputMatcher = Matcher.parse(worldIn, input);
        Matcher outputMatcher = Matcher.parse(worldOut, output);
        return outputMatcher.toMerger(inputMatcher);
    }
}
