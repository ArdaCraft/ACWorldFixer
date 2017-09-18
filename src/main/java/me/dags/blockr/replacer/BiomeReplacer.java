package me.dags.blockr.replacer;

import org.pepsoft.minecraft.Extent;

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
    public boolean apply(Extent extent, int type, int x, int y, int z) {
        // check biome at (x,z) and pass to the replacer if it's the correct biome
        return extent.getBiome(x, z) == biomeId && replacer.apply(extent, type, x, y, z);
    }

    @Override
    public boolean typeMatches(int type) {
        return replacer.typeMatches(type);
    }

    @Override
    public boolean dataMatches(int data) {
        return replacer.dataMatches(data);
    }

    @Override
    public BiomeReplacer clone() {
        return new BiomeReplacer(biomeId, replacer.clone());
    }
}
