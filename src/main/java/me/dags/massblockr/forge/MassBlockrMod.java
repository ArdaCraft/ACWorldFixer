package me.dags.massblockr.forge;

import com.google.gson.stream.JsonWriter;
import me.dags.massblockr.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dags <dags@dags.me>
 */
@Mod(modid = "massblockr")
public class MassBlockrMod {

    @Mod.EventHandler
    public void onLoad(FMLLoadCompleteEvent event) {
        File out = new File("config/registry.json").getAbsoluteFile();
        File parent = out.getParentFile();

        if (!parent.exists() && parent.mkdirs()) {
            System.out.println("Created mustDir: " + parent);
        }

        try (JsonWriter writer = new JsonWriter(new FileWriter(out))) {
            writer.setIndent("  ");
            writer.beginObject();
            writeSchema(writer);
            writeMods(writer);
            writeBlocks(writer);
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeSchema(JsonWriter writer) throws IOException {
        writer.name("schema").value(World.LEGACY_SCHEMA);
    }

    private static void writeMods(JsonWriter writer) throws IOException {
        writer.name("mods").beginObject();
        {
            for (ModContainer mod : Loader.instance().getActiveModList()) {
                if (mod.getModId().equalsIgnoreCase("massblockr")) {
                    continue;
                }
                writer.name(mod.getModId()).value(mod.getVersion());
            }
        }
        writer.endObject();
    }

    private static void writeBlocks(JsonWriter writer) throws IOException {
        writer.name("registry").beginObject();
        {
            for (Block block : Block.REGISTRY) {
                ResourceLocation name = block.getRegistryName();
                if (name == null) {
                    continue;
                }

                writer.name(name.toString()).beginObject();
                {
                    Pattern pattern = Pattern.compile(".+\\[(.*?)]");
                    List<IBlockState> variants = block.getBlockState().getValidStates();
                    for (IBlockState state : variants) {
                        Matcher matcher = pattern.matcher(state.toString());
                        if (matcher.find()) {
                            String properties = matcher.group(1);
                            writer.name(properties).value(block.getMetaFromState(state));
                        }
                    }
                }
                writer.endObject();
            }
        }
        writer.endObject();
    }
}
