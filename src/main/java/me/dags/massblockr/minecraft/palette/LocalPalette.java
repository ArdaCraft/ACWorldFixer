package me.dags.massblockr.minecraft.palette;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.ListTag;
import me.dags.massblockr.jnbt.Tag;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.registry.Registry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dags <dags@dags.me>
 */
public class LocalPalette extends AbstractPalette {

    private final AtomicInteger idCounter = new AtomicInteger(-1);

    public LocalPalette(Registry registry, ListTag palette) {
        for (Tag tag : palette.getValue()) {
            CompoundTag entry = (CompoundTag) tag;
            BlockState state = registry.parseState(entry);
            register(createID(), state);
        }
    }

    @Override
    protected int createID() {
        return idCounter.addAndGet(1);
    }
}
