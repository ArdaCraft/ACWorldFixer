package me.dags.massblockr.minecraft.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dags.massblockr.minecraft.block.Block;
import me.dags.massblockr.minecraft.block.BlockState;
import me.dags.massblockr.minecraft.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author dags <dags@dags.me>
 */
public class Loader {

    public static Registry load(InputStream inputStream) throws IOException {
        Registry registry = new Registry();

        try (Reader reader = new InputStreamReader(inputStream)) {
            JsonElement element = new JsonParser().parse(reader);
            if (element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                if (root.has("schema") && root.has("registry")) {
                    int version = root.get("schema").getAsInt();
                    JsonObject blocks = root.getAsJsonObject("registry");
                    BiFunction<String, JsonElement, Block> factory = getFactory(version);

                    for (Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                        Block block = factory.apply(entry.getKey(), entry.getValue());
                        registry.register(block);
                    }

                    return registry;
                }
            }
        }

        throw new IllegalArgumentException("Invalid registry!");
    }

    private static BiFunction<String, JsonElement, Block> getFactory(int version) {
        switch (version) {
            case World.LEGACY_SCHEMA:
                return (name, properties) -> {
                    List<BlockState.Builder> states = new LinkedList<>();
                    if (properties.getAsJsonObject().size() == 0) {
                        states.add(new BlockState.Builder());
                    } else {
                        for (Map.Entry<String, JsonElement> property : properties.getAsJsonObject().entrySet()) {
                            BlockState.Builder builder = BlockState.builder();
                            builder.meta(property.getValue().getAsInt());
                            BlockState.parse(builder, property.getKey());
                            states.add(builder);
                        }
                    }
                    return new Block(name, states);
                };
            case World.FUTURE_SCHEMA:
                return (name, properties) -> {
                    List<BlockState.Builder> states = new LinkedList<>();
                    if (properties.getAsJsonObject().size() == 0) {
                        states.add(new BlockState.Builder());
                    } else {
                        for (JsonElement property : properties.getAsJsonArray()) {
                            BlockState.Builder builder = BlockState.builder();
                            BlockState.parse(builder, property.getAsString());
                            states.add(builder);
                        }
                    }
                    return new Block(name, states);
                };
            default:
                    throw new IllegalArgumentException("INVALID SCHEMA VERSION " + version);
        }
    }
}
