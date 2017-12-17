package me.dags.massblockr.util;

import me.dags.massblockr.minecraft.block.Mapper;
import me.dags.massblockr.minecraft.world.World;

import java.io.*;

/**
 * @author dags <dags@dags.me>
 */
public class Mappings {

    public static Mapper load(World worldIn, World worldOut, File file) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            return load(worldIn, worldOut, inputStream);
        }
    }

    public static Mapper load(World worldIn, World worldOut, InputStream inputStream) {
        Mapper.Builder builder = Mapper.build(worldIn, worldOut);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                readLine(builder, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.build();
    }

    private static void readLine(Mapper.Builder builder, String line) {
        int i = 0;
        while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
            i++;
        }

        int inStart = i;
        while (i < line.length() && !Character.isWhitespace(line.charAt(i))) {
            i++;
        }
        int inEnd = i;

        while (i < line.length()) {
            char c = line.charAt(i);
            if (Character.isAlphabetic(c)) {
                break;
            }
            i++;
        }

        int outStart = i;
        while (i < line.length() && !Character.isWhitespace(line.charAt(i))) {
            i++;
        }
        int outEnd = i;

        String input = line.substring(inStart, inEnd);
        String output = line.substring(outStart, outEnd);
        builder.map(input, output);
    }
}
