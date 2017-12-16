package me.dags.massblockr.minecraft.world.volume;

import me.dags.massblockr.jnbt.CompoundTag;

/**
 * @author dags <dags@dags.me>
 */
public interface VolumeOutput extends MutableBlockVolume {

    CompoundTag getTag();
}
