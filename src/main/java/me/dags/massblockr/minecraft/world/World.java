package me.dags.massblockr.minecraft.world;

import com.google.common.collect.ImmutableList;
import me.dags.massblockr.minecraft.registry.Registry;
import me.dags.massblockr.minecraft.world.dimension.Dimension;
import me.dags.massblockr.minecraft.world.dimension.RootDimension;
import me.dags.massblockr.minecraft.world.dimension.SubDimension;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public interface World {

    int LEGACY_SCHEMA = 0;
    int FUTURE_SCHEMA = 1;

    int getSchema();

    File getDirectory();

    Registry getRegistry();

    List<Dimension> getDimensions();

    Dimension createDimension(String name);

    static List<Dimension> findDimensions(World world) {
        File[] files = world.getDirectory().listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        ImmutableList.Builder<Dimension> builder = ImmutableList.builder();
        builder.add(new RootDimension(world));

        for (File file : files) {
            if (!Dimension.isValidDimension(file)) {
                continue;
            }
            builder.add(new SubDimension(world, file));
        }
        return builder.build();
    }
}
