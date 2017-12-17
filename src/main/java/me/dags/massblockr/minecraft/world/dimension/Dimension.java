package me.dags.massblockr.minecraft.world.dimension;

import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.minecraft.world.region.Region;

import java.io.File;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public interface Dimension {

    String getName();

    World getWorld();

    int getRegionCount();

    boolean isRoot();

    Stream<Region> getRegions();

    Region createRegion(String name);

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
