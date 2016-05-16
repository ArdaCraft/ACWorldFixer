package me.dags.worldfixer.block.replacers;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockPredicate
{

    boolean test(SimpleReplacer replacer, int type, int data);

    default BlockPredicate and(BlockPredicate rule) {
        return (replacer, type, data) -> test(replacer, type, data) && rule.test(replacer, type, data);
    }
}
