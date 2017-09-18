package me.dags.blockr.replacer;

import org.pepsoft.minecraft.Extent;

/**
 * @author dags <dags@dags.me>
 */
public interface Replacer extends Cloneable {

    boolean apply(Extent extent, int type, int x, int y, int z);

    boolean typeMatches(int type);

    boolean dataMatches(int data);

    Replacer clone();
}
