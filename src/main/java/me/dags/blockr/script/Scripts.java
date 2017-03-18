package me.dags.blockr.script;

import me.dags.blockr.Config;
import me.dags.blockr.block.BlockInfo;
import me.dags.data.NodeAdapter;
import me.dags.data.node.Node;
import me.dags.data.node.NodeTypeAdapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author dags_ <dags@dags.me>
 */
public class Scripts {

    public static void main(String[] args) {
        NodeTypeAdapters.register(BlockInfo.class, new BlockInfo.Adapter());
        NodeTypeAdapters.register(Config.class, new Config.Adapter());

        File root = new File("").getAbsoluteFile();

        File ardaDump = new File(root, "from_mappings.txt");
        File crDump = new File(root, "to_mappings.txt");
        File script = new File(root, "script.sour");
        File output = new File(root, "mappings.json");

        Map<Integer, String> arda = loadMappings(ardaDump);
        Map<Integer, String> cr = loadMappings(crDump);

        Config config = convertScript(script, arda, cr);
        config.copyBelow.add(new BlockInfo("minecraft:double_plant", -1, 8, 15, BlockInfo.EMPTY));

        Node node = NodeTypeAdapters.serialize(config);
        NodeAdapter.json().to(node, output);
    }

    private static Map<Integer, String> loadMappings(File file) {
        Map<Integer, String> map = new HashMap<>();

        for (String line : Scripts.readLines(file)) {
            String[] split = line.split(",");
            String block = split[0];
            String blockId = split[1].trim().replace("id=", "");

            int id = parseInt(blockId);
            if (id > -1) {
                map.put(id, block);
            }
        }

        return map;
    }

    private static Config convertScript(File script, Map<Integer, String> from, Map<Integer, String> to) {
        Config config = new Config();
        for (String line : readLines(script)) {
            if (!line.startsWith("REPLACE")) {
                continue;
            }

            String[] split = line.split(" ");
            if (split.length < 5) {
                continue;
            }

            int fromId = parseInt(split[1]);
            int fromMeta = parseInt(split[2]);
            int toId = parseInt(split[3]);
            int toMeta = parseInt(split[4]);

            if (fromId > -1 && toId > -1 && fromMeta > -1 && toMeta > -1) {
                String fromBlock = from.get(fromId);
                String toBlock = to.get(toId);
                if (fromBlock != null && toBlock != null) {
                    BlockInfo toInfo = new BlockInfo(toBlock, -1, toMeta, toMeta, BlockInfo.EMPTY);
                    BlockInfo fromInfo = new BlockInfo(fromBlock, -1, fromMeta, fromMeta, toInfo);
                    config.blocks.add(fromInfo);
                }
            }
        }

        return config;
    }

    private static int parseInt(String in) {
        try {
            return Integer.parseInt(in.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static List<String> readLines(File file) {
        List<String> lines = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            try (Scanner scanner = new Scanner(inputStream)) {
                while (scanner.hasNext()) {
                    lines.add(scanner.nextLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
