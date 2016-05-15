package me.dags.worldfixer;

import me.dags.worldfixer.levelfix.BlockRegistry;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class WorldData {

    private final File root;
    private final File level;
    private final File region;
    public final BlockRegistry blockRegistry = new BlockRegistry();

    public WorldData(File root) {
        this.root = root;
        this.level = new File(root, "level.dat");
        this.region = new File(root, "region");
    }

    public CompoundTag getLevelData() {
        try (NBTInputStream out = new NBTInputStream(new GZIPInputStream(new FileInputStream(level)))) {
            Tag tag = out.readTag();
            if (tag instanceof CompoundTag) {
                return (CompoundTag) tag;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public void writeLevelData(Tag tag) {
        try (NBTOutputStream out = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(level)))) {
            out.writeTag(tag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean validate() {
        return level.exists() && region.exists();
    }

    public String error() {
        if (!level.exists()) {
            return "level.dat is missing!";
        }
        return "region directory is missing!";
    }

    public List<File> getRegionFiles() {
        File[] files = region.listFiles();
        if (files != null && files.length > 0) {
            List<File> mca_files = new ArrayList<>();
            for (File f : files) {
                if (f.getName().endsWith(".mca")) {
                    mca_files.add(f);
                }
            }
            return mca_files;
        }
        return Collections.emptyList();
    }
}
