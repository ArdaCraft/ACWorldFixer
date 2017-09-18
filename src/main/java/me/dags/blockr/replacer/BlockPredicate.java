package me.dags.blockr.replacer;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockPredicate
{

    boolean test(Replacer replacer, int type, int data);

    default BlockPredicate and(BlockPredicate rule) {
        return (replacer, type, data) -> test(replacer, type, data) && rule.test(replacer, type, data);
    }
}
