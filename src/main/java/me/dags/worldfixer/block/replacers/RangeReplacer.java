package me.dags.worldfixer.block.replacers;

/**
 * @author dags <dags@dags.me>
 */
public class RangeReplacer extends SimpleReplacer {

    private final int min;
    private final int max;

    RangeReplacer(int typeFrom, int typeTo, int minData, int maxData, int dataTo, BlockChangeApplier consumer) {
        super(typeFrom, typeTo, minData, dataTo, Replacers.matchTypeAndData, consumer);
        this.min = minData;
        this.max = maxData;
    }

    @Override
    boolean dataMatches(int data) {
        return min <= data && max >= data;
    }
}
