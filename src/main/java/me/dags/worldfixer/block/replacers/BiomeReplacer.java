package me.dags.worldfixer.block.replacers;

import org.pepsoft.minecraft.Chunk;

/**
 * @author dags <dags@dags.me>
 */
public class BiomeReplacer implements Replacer {

    private final int biomeId;
    private final Replacer replacer;

    BiomeReplacer(int biomeId, Replacer replacer) {
        this.biomeId = biomeId;
        this.replacer = replacer;
    }

    @Override
    public boolean apply(Chunk chunk, int type, int x, int y, int z) {
        // check biome at (x,z) and pass to the replacer if it's the correct biome
        return chunk.getBiome(x, z) == biomeId && replacer.apply(chunk, type, x, y, z);
    }

    @Override
    public BiomeReplacer clone() {
        return new BiomeReplacer(biomeId, replacer.clone());
    }
}
