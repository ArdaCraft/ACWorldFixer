package me.dags.blockr.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author dags <dags@dags.me>
 */
public class WorldDataFile extends WorldData {

    private final File level;

    public WorldDataFile(File levelIn) {
        this.level = levelIn;
    }

    @Override
    public boolean validate() {
        return level.exists();
    }

    @Override
    public String error() {
        if (!level.exists()) {
            return "level.dat is missing!";
        }
        return "region directory is missing!";
    }

    @Override
    InputStream getInputStream() throws IOException {
        return new FileInputStream(level);
    }
}
