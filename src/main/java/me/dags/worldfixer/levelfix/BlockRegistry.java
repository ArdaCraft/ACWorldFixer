package me.dags.worldfixer.levelfix;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class BlockRegistry {

    private final Map<String, Integer> blockIds = new HashMap<>();

    public BlockRegistry() {
        blockIds.put("minecraft:air", 0);
    }

    public void register(String block, int id) {
        blockIds.put(block.trim(), id);
    }

    public Integer getId(String block) {
        return blockIds.get(block);
    }
}
