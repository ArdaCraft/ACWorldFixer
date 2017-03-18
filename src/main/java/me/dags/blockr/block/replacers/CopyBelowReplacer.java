package me.dags.blockr.block.replacers;

import org.pepsoft.minecraft.Chunk;

/**
 * @author dags <dags@dags.me>
 */
public class CopyBelowReplacer implements Replacer {

    private final BlockPredicate rule;
    private final int type;
    private final int minData;
    private final int maxData;

    CopyBelowReplacer(int type, int minData, int maxData, BlockPredicate blockPredicate) {
        this.type = type;
        this.minData = minData;
        this.maxData = maxData;
        this.rule = blockPredicate;
    }

    @Override
    public boolean apply(Chunk chunk, int type, int x, int y, int z) {
        // is block below
        if (y > 0) {
            int data = chunk.getDataValue(x, y, z);

            // type and data within target range
            if (type == this.type && data >= minData) {
                int typeBelow = chunk.getBlockType(x, y - 1, z);

                // block below is a different type
                if (typeBelow != type) {
                    int dataBelow = chunk.getDataValue(x, y - 1, z);
                    chunk.setBlockType(x, y, z, typeBelow);
                    chunk.setDataValue(x, y, z, dataBelow);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean typeMatches(int type) {
        return type == this.type;
    }

    @Override
    public boolean dataMatches(int data) {
        return data >= minData && data <= maxData;
    }

    @Override
    public Replacer clone() {
        return new CopyBelowReplacer(type, minData, maxData, rule);
    }
}
