package me.dags.massblockr.util;

import me.dags.massblockr.jnbt.NBTInputStream;
import me.dags.massblockr.jnbt.NBTOutputStream;
import me.dags.massblockr.jnbt.Tag;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class FileUtils {

    public static File mustDir(File parent, String name) {
        File file = new File(parent, name);
        file.mkdirs();
        return file;
    }

    public static File mustFile(File parent, String name) {
        parent.mkdirs();
        return new File(parent, name);
    }

    public static InputStream bufferedIn(File file) throws IOException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    public static OutputStream bufferedOut(File file) throws IOException {
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    public static Tag readNBT(File file) throws IOException {
        return readNBT(bufferedIn(file));
    }

    public static void writeNBT(Tag tag, File file) throws IOException {
        writeNBT(tag, bufferedOut(file));
    }

    public static Tag readNBT(InputStream inputStream) throws IOException {
        try (NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(inputStream))) {
            return nbt.readTag();
        }
    }

    public static void writeNBT(Tag tag, OutputStream outputStream) throws IOException {
        try (NBTOutputStream nbt = new NBTOutputStream(new GZIPOutputStream(outputStream))) {
            nbt.writeTag(tag);
        }
    }
}
