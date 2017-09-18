package me.dags.blockr.replacer;

/**
 * @author dags <dags@dags.me>
 */
public class Replacers {

    private static final BlockPredicate matchType = (replacer, type, data) -> replacer.typeMatches(type);
    private static final BlockPredicate matchData = (replacer, type, data) -> replacer.dataMatches(data);
    static final BlockPredicate matchTypeAndData = matchType.and(matchData);

    private static final BlockChangeApplier replaceType = ((chunk, x, y, z, type, data) -> chunk.setBlockType(x, y, z, type));
    private static final BlockChangeApplier replaceData = ((chunk, x, y, z, type, data) -> chunk.setDataValue(x, y, z, data));
    private static final BlockChangeApplier replaceTypeAndData = replaceType.and(replaceData);

    public static SimpleReplacer matchTypeReplaceType(int typeFrom, int typeTo) {
        return new SimpleReplacer(typeFrom, typeTo, -1, -1, matchType, replaceType);
    }

    public static SimpleReplacer printMatchTypeReplaceType(int typeFrom, int typeTo) {
        return new SimpleReplacer(typeFrom, typeTo, -1, -1, matchType, replaceType, true);
    }

    public static SimpleReplacer matchTypeReplaceData(int typeFrom, int dataTo) {
        return new SimpleReplacer(typeFrom, typeFrom, -1, dataTo, matchType, replaceData);
    }

    public static SimpleReplacer matchTypeReplaceTypeAndData(int typeFrom, int typeTo, int dataTo) {
        return new SimpleReplacer(typeFrom, typeTo, -1, dataTo, matchType, replaceTypeAndData);
    }

    public static SimpleReplacer matchTypeAndDataReplaceType(int typeFrom, int typeTo, int dataFrom) {
        return new SimpleReplacer(typeFrom, typeTo, dataFrom, dataFrom, matchTypeAndData, replaceType);
    }

    public static SimpleReplacer matchTypeAndDataReplaceData(int typeFrom, int dataFrom, int dataTo) {
        return new SimpleReplacer(typeFrom, typeFrom, dataFrom, dataTo, matchTypeAndData, replaceData);
    }

    public static SimpleReplacer matchTypeAndDataReplaceTypeAndData(int typeFrom, int typeTo, int dataFrom, int dataTo) {
        return new SimpleReplacer(typeFrom, typeTo, dataFrom, dataTo, matchTypeAndData, replaceTypeAndData);
    }

    public static RangeToOneReplacer rangeMatchTypeReplaceType(int typeFrom, int typeTo, int minData, int maxData) {
        return new RangeToOneReplacer(typeFrom, typeTo, minData, maxData, -1, replaceType);
    }

    public static RangeToOneReplacer rangeMatchTypeReplaceData(int typeFrom, int minData, int maxData, int dataTo) {
        return new RangeToOneReplacer(typeFrom, typeFrom, minData, maxData, dataTo, replaceData);
    }

    public static RangeToOneReplacer rangeMatchTypeReplaceTypeAndData(int typeFrom, int typeTo, int minData, int maxData, int dataTo) {
        return new RangeToOneReplacer(typeFrom, typeTo, minData, maxData, dataTo, replaceTypeAndData);
    }

    public static RangeToRangeReplacer rangeMatchTypeReplaceDataRange(int typeFrom, int minData, int maxData, int toMinData) {
        return new RangeToRangeReplacer(typeFrom, typeFrom, minData, maxData, toMinData, replaceData);
    }

    public static RangeToRangeReplacer rangeMatchTypeReplaceTypeAndDataRange(int typeFrom, int typeTo, int minData, int maxData, int toMinData) {
        return new RangeToRangeReplacer(typeFrom, typeTo, minData, maxData, toMinData, replaceData);
    }

    public static BiomeReplacer matchBiomeWithReplacer(int biomeId, Replacer replacer) {
        return new BiomeReplacer(biomeId, replacer);
    }

    public static CopyBelowReplacer matchTypeAndDataReplaceWithBelow(int fromId, int minData, int maxData) {
        return new CopyBelowReplacer(fromId, minData, maxData, matchTypeAndData);
    }
}
