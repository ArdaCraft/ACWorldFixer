package me.dags.massblockr.minecraft.world.dimension;

import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.util.FileUtils;

import java.io.File;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class SubDimension implements Dimension {

    private final World world;
    private final File root;
    private final File regions;

    public SubDimension(World world, File root) {
        this.world = world;
        this.root = root;
        this.regions = FileUtils.mustDir(root, "region");
    }

    @Override
    public String getName() {
        return root.getName();
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
