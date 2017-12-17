package me.dags.massblockr.minecraft.block;

import com.google.common.collect.ImmutableMap;
import me.dags.massblockr.minecraft.world.World;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public class Mapper {

    private final Map<BlockState, BlockState> mappings;

    public Mapper(Map<BlockState, BlockState> mappings) {
        this.mappings = mappings;
    }

    public BlockState map(BlockState in) {
        return mappings.getOrDefault(in, in);
    }

    public void print(Appendable out) throws IOException {
        for (Map.Entry<BlockState, BlockState> mapping : mappings.entrySet()) {
            out.append(mapping.getKey().toString());
            out.append(" => ");
            out.append(mapping.getValue().toString());
            out.append('\n');
        }
    }

    public static Mapper of(World worldIn, String input, World worldOut, String output) {
        return of(Merger.of(worldIn, input, worldOut, output));
    }

    public static Mapper of(Merger merger) {
        ImmutableMap.Builder<BlockState, BlockState> builder = ImmutableMap.builder();
        merger.getMatcher().getBlock().forEach((i, input) -> {
            if (merger.matches(input)) {
                Optional<BlockState> output = merger.merge(input);
                if (!output.isPresent()) {
                    System.out.printf("Unable to merge state: %s with merger: %s\n", input, merger);
                    return;
                }
                builder.put(input, output.get());
            }
        });
        return new Mapper(builder.build());
    }

    public static Builder build(World in, World out) {
        return new Builder(in, out);
    }

    public static class Builder {

        private final ImmutableMap.Builder<BlockState, BlockState> builder = ImmutableMap.builder();
        private final Set<BlockState> visited = new HashSet<>();
        private final World worldIn;
        private final World worldOut;

        private Builder(World worldIn, World worldOut) {
            this.worldIn = worldIn;
            this.worldOut = worldOut;
        }

        public Builder map(String inputString, String outputString) {
            Merger merger = Merger.of(worldIn, inputString, worldOut, outputString);
            merger.getMatcher().getBlock().forEach((i, input) -> {
                if (merger.matches(input)) {
                    Optional<BlockState> output = merger.merge(input);
                    if (!output.isPresent()) {
                        System.out.printf("Unable to merge state: %s with merger: %s\n", input, merger);
                        return;
                    }

                    if (visited.add(input)) {
                        builder.put(input, output.get());
                    }
                }
            });
            return this;
        }

        public Mapper build() {
            return new Mapper(builder.build());
        }
    }
}
