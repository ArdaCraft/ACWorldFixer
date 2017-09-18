package me.dags.blockr.replacer;

import org.pepsoft.minecraft.Extent;

/**
 * @author dags <dags@dags.me>
 */
public class RangeToRangeReplacer extends SimpleReplacer {

    private final int min;
    private final int max;

    RangeToRangeReplacer(int typeFrom, int typeTo, int minData, int maxData, int dataTo, BlockChangeApplier consumer) {
        super(typeFrom, typeTo, minData, dataTo, Replacers.matchTypeAndData, consumer);
        this.min = minData;
        this.max = maxData;
    }

    @Override
    public boolean dataMatches(int data) {
        return min <= data && max >= data;
    }

    @Override
    public boolean apply(Extent extent, int type, int x, int y, int z) {
        int dataFrom = extent.getDataValue(x, y, z);
        if (super.rule.test(this, type, dataFrom)) {
            int dataTo = remap(dataFrom);
            super.consumer.accept(extent, x, y, z, super.typeTo, dataTo);
            return true;
        }
        return false;
    }

    private int remap(int meta) {
        int offset = meta - min;
        return super.dataTo + offset;
    }
}
