package me.dags.massblockr.minecraft.palette;

import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.ListTag;
import me.dags.massblockr.jnbt.NBTConstants;
import me.dags.massblockr.jnbt.Tag;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dags <dags@dags.me>
 */
public class LocalPalette extends AbstractPalette {

    private final ListTag tag;
    private final AtomicInteger idCounter = new AtomicInteger(-1);
    private volatile boolean modified = false;

    public LocalPalette(Registry registry, ListTag palette) {
        this.tag = palette;
        for (Tag tag : palette.getValue()) {
            CompoundTag entry = (CompoundTag) tag;
            BlockState state = registry.parseState(entry);
            register(idCounter.addAndGet(1), state);
        }
    }

    public ListTag toNBT() {
        if (modified) {
            List<Tag> list = new ArrayList<>();
            int max = idCounter.get();
            for (int i = 0; i < max; i++) {
                BlockState state = getState(i);
                if (state != null) {
                    list.add(state.toNBT());
                }
            }
            return new ListTag("Palette", NBTConstants.TYPE_COMPOUND, list);
        }
        return tag;
    }

    @Override
    protected int createID() {
        modified = true;
        return idCounter.addAndGet(1);
    }
}
