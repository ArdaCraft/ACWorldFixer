package me.dags.massblockr.util;

import me.dags.massblockr.jnbt.NBTInputStream;
import me.dags.massblockr.jnbt.NBTOutputStream;
import me.dags.massblockr.jnbt.Tag;

import java.io.*;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class FileUtils {

    public static final int VERSION_GZIP = 1;
    public static final int VERSION_DEFLATE = 2;

    public static File mustDir(File dir) {
        dir.mkdirs();
        return dir;
    }

    public static File mustDir(File parent, String name) {
        return mustDir(new File(parent, name));
    }

    public static File mustFile(File parent, String name) {
        return new File(mustDir(parent), name);
    }

    public static InputStream bufferedIn(File file) throws IOException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    public static OutputStream bufferedOut(File file) throws IOException {
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    public static Tag readNBT(File file) throws IOException {
        return readNBT(bufferedIn(file), VERSION_GZIP);
    }

    public static void writeNBT(Tag tag, File file) throws IOException {
        writeNBT(tag, bufferedOut(file), VERSION_GZIP);
    }

    public static Tag readNBT(InputStream inputStream) throws IOException {
        try (NBTInputStream nbt = new NBTInputStream(inputStream)) {
            return nbt.readTag();
        }
    }

    public static Tag readNBT(InputStream inputStream, int type) throws IOException {
        if (type == VERSION_GZIP) {
            try (NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(inputStream))) {
                return nbt.readTag();
            }
        } else {
            try (NBTInputStream nbt = new NBTInputStream(new DeflaterInputStream(inputStream))) {
                return nbt.readTag();
            }
        }
    }

    public static void writeNBT(Tag tag, OutputStream outputStream, int type) throws IOException {
        if (type == VERSION_GZIP) {
            try (NBTOutputStream nbt = new NBTOutputStream(new GZIPOutputStream(outputStream))) {
                nbt.writeTag(tag);
            }
        } else if (type == VERSION_DEFLATE) {
            try (NBTOutputStream nbt = new NBTOutputStream(new DeflaterOutputStream(outputStream))) {
                nbt.writeTag(tag);
            }
        }
    }
}
