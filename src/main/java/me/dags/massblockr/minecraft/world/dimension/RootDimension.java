package me.dags.massblockr.minecraft.world.dimension;

import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.util.FileUtils;

import java.io.File;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class RootDimension implements Dimension {

    private final World world;
    private final File regions;

    public RootDimension(World world) {
        this.world = world;
        this.regions = FileUtils.mustDir(world.getDirectory(), "region");
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Stream<RegionFile> getRegions() {
        File[] files = regions.listFiles();
        if (files == null) {
            return Stream.empty();
        }
        return Stream.of(files).filter(RegionFile::isValid).map(file -> new RegionFile(world, file));
    }

    @Override
    public RegionFile createRegion(String name) {
        return new RegionFile(world, FileUtils.mustFile(regions, name));
    }
}
