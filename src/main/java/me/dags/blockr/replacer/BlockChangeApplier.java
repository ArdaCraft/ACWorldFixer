package me.dags.blockr.replacer;

import org.pepsoft.minecraft.Extent;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockChangeApplier
{

    void accept(Extent extent, int x, int y, int z, int toType, int toData);

    default BlockChangeApplier and(BlockChangeApplier consumer) {
        return ((extent, x, y, z, toType, toData) -> {
            accept(extent, x, y, z, toType, toData);
            consumer.accept(extent, x, y, z, toType, toData);
        });
    }
}
