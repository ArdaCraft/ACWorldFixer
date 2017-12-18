package me.dags.massblockr.minecraft.world;

import com.google.common.base.Preconditions;
import me.dags.massblockr.jnbt.CompoundTag;
import me.dags.massblockr.jnbt.IntTag;
import me.dags.massblockr.jnbt.StringTag;
import me.dags.massblockr.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author dags <dags@dags.me>
 */
public class Level {

    private final int id;
    private final String version;
    private final CompoundTag root;

    public Level(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public Level(InputStream inputStream) throws IOException {
        CompoundTag root = (CompoundTag) FileUtils.readNBT(inputStream);
        inputStream.close();

        IntTag id = (IntTag) CompoundTag.getTag(root, "Data", "Version", "Id");
        StringTag name = (StringTag) CompoundTag.getTag(root, "Data", "Version", "Name");
        Preconditions.checkNotNull(id, "Level version ID is not found!");
        Preconditions.checkNotNull(name, "Level version Name is not found!");

        this.root = root;
        this.id = id.getValue();
        this.version = name.getValue();
    }

    public int getSchema() {
        if (getVersionId() < 1400) {
            return World.PRE_1_13_SCHEMA;
        }
        if (getVersionId() > 1400) {
            return World.POST_1_13_SCHEMA;
        }
        return World.UNKNOWN_SCHEMA;
    }

    public int getVersionId() {
        return id;
    }

    public String getVersionName() {
        return version;
    }

    public CompoundTag getRoot() {
        return root;
    }
}
