package me.dags.worldfixer.blockfix.replacers;

import org.pepsoft.minecraft.Chunk;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockChangeApplier
{

    void accept(Chunk chunk, int x, int y, int z, int toType, int toData);

    default BlockChangeApplier and(BlockChangeApplier consumer) {
        return ((chunk, x, y, z, toType, toData) -> {
            accept(chunk, x, y, z, toType, toData);
            consumer.accept(chunk, x, y, z, toType, toData);
        });
    }
}
