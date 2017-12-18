package me.dags.massblockr.forge;

import com.google.gson.stream.JsonWriter;
import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.util.FileUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
        String version = "";
        for (String brand : FMLCommonHandler.instance().getBrandings(true)) {
            if (brand.startsWith("Minecraft")) {
                String ver = brand.substring("Minecraft ".length());
                int first = ver.indexOf('.') + 1;
                int second = ver.indexOf('.', first);
                version = second == -1 ? ver : ver.substring(0, second);
                break;
            }
        }

        if (version.isEmpty()) {
            System.out.println("Could not determine client version!");
            return;
        }

        File block = new File(String.format("config/massblockr/legacy/%s.json", version)).getAbsoluteFile();
        File state = new File(String.format("config/massblockr/registry/%s.json", version)).getAbsoluteFile();

        FileUtils.mustDir(block.getParentFile());
        FileUtils.mustDir(state.getParentFile());

        try (JsonWriter blocks = new JsonWriter(new FileWriter(block)); JsonWriter states = new JsonWriter(new FileWriter(state))) {
            blocks.setIndent("  ");
            states.setIndent("  ");

            blocks.beginObject();
            states.beginObject();

            writeSchema(states);
            writeMods(states);
            writeBlocks(blocks, states);

            blocks.endObject();
            states.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeSchema(JsonWriter writer) throws IOException {
        writer.name("schema").value(World.PRE_1_13_SCHEMA);
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

    private static void writeBlocks(JsonWriter blocks, JsonWriter states) throws IOException {
        states.name("registry").beginObject();
        {
            for (Block block : Block.REGISTRY) {
                ResourceLocation name = block.getRegistryName();
                if (name == null) {
                    continue;
                }

                blocks.name(name.toString()).value(Block.getIdFromBlock(block));

                states.name(name.toString()).beginObject();
                {
                    Pattern pattern = Pattern.compile(".+\\[(.*?)]"); // matches `a=0,b=1,c=2` in `domain:block[a=0,b=1,c=2]`
                    List<IBlockState> variants = block.getBlockState().getValidStates();
                    for (IBlockState state : variants) {
                        Matcher matcher = pattern.matcher(state.toString());
                        if (matcher.find()) {
                            String properties = matcher.group(1);
                            states.name(properties).value(block.getMetaFromState(state));
                        }
                    }
                }
                states.endObject();
            }
        }
        states.endObject();
    }
}
