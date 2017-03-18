package me.dags.blockr.block.replacers;

import org.pepsoft.minecraft.Chunk;

/**
 * @author dags <dags@dags.me>
 */
public class SimpleReplacer implements Replacer {

    final int typeFrom;
    final int typeTo;
    final int dataFrom;
    final int dataTo;
    final BlockPredicate rule;
    final BlockChangeApplier consumer;
    final boolean log;

    SimpleReplacer(int typeFrom, int typeTo, int dataFrom, int dataTo, BlockPredicate blockPredicate, BlockChangeApplier consumer) {
        this.typeFrom = typeFrom;
        this.typeTo = typeTo;
        this.dataFrom = dataFrom;
        this.dataTo = dataTo;
        this.rule = blockPredicate;
        this.consumer = consumer;
        this.log = false;
    }

    SimpleReplacer(int typeFrom, int typeTo, int dataFrom, int dataTo, BlockPredicate blockPredicate, BlockChangeApplier consumer, boolean log) {
        this.typeFrom = typeFrom;
        this.typeTo = typeTo;
        this.dataFrom = dataFrom;
        this.dataTo = dataTo;
        this.rule = blockPredicate;
        this.consumer = consumer;
        this.log = log;
    }

    @Override
    public boolean apply(Chunk chunk, int type, int x, int y, int z) {
        if (rule.test(this, type, chunk.getDataValue(x, y, z))) {
            consumer.accept(chunk, x, y, z, typeTo, dataTo);
            return true;
        }
        return false;
    }

    @Override
    public boolean typeMatches(int type) {
        return typeFrom == type;
    }

    @Override
    public boolean dataMatches(int data) {
        return dataFrom == data;
    }

    @Override
    public SimpleReplacer clone() {
        return new SimpleReplacer(typeFrom, typeTo, dataFrom, dataTo, rule, consumer);
    }

    @Override
    public String toString() {
        return String.format("%s:%s => %s:%s", typeFrom, dataFrom, typeTo, dataTo);
    }
}
