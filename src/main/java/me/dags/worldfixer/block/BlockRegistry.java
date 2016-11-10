package me.dags.worldfixer.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        System.out.printf("Registering %s:%s\n", block, id);
    }

    public Integer getId(String block) {
        return blockIds.get(block);
    }

    public String[] blockNames() {
        List<String> sorted = blockIds.entrySet().stream()
            .sorted((e1, e2) -> e1.getValue() >= e2.getValue() ? 1 : -1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        return sorted.toArray(new String[sorted.size()]);
    }
}
