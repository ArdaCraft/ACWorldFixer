package me.dags.massblockr.minecraft.world.dimension;

import java.io.File;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public interface Dimension {

    String getName();

    Stream<RegionFile> getRegions();

    RegionFile createRegion(String name);

    static boolean isValidDimension(File file) {
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }

        if (file.getName().equalsIgnoreCase("data")) {
            return false;
        }

        if (file.getName().equalsIgnoreCase("region")) {
            return false;
        }

        if (file.getName().equalsIgnoreCase("stats")) {
            return false;
        }

        if (file.getName().equalsIgnoreCase("playerdata")) {
            return false;
        }

        return true;
    }
}
