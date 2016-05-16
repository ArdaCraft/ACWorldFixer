package me.dags.worldfixer.block.replacers;

import org.pepsoft.minecraft.Chunk;

/**
 * @author dags <dags@dags.me>
 */
public interface Replacer extends Cloneable {

    int getType();

    boolean apply(Chunk chunk, int type, int x, int y, int z);

    Replacer clone();
}
