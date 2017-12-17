package me.dags.massblockr.minecraft.world.dimension;

import me.dags.massblockr.minecraft.world.World;
import me.dags.massblockr.minecraft.world.region.Region;
import me.dags.massblockr.minecraft.world.region.RegionFile;
import me.dags.massblockr.util.FileUtils;

import java.io.File;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class WorldDimension implements Dimension {

    private final World world;
    private final boolean root;
    private final File dir;
    private final File regions;
    private final File[] files;

    public WorldDimension(World world) {
        this(world, world.getDirectory(), true);
    }

    public WorldDimension(World world, File dir) {
        this(world, dir, false);
    }

    public WorldDimension(World world, File dir, boolean root) {
        this.world = world;
        this.dir = dir;
        this.root = root;
        this.regions = FileUtils.mustDir(dir, "region");
        this.files = regions.listFiles();
    }

    @Override
    public String getName() {
        return dir.getName();
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public int getRegionCount() {
        return files.length;
    }

    @Override
    public boolean isRoot() {
        return root;
    }

    @Override
    public Stream<Region> getRegions() {
        return Stream.of(files).filter(RegionFile::isValid).map(file -> new RegionFile(world, file));
    }

    @Override
    public RegionFile createRegion(String name) {
        return new RegionFile(world, FileUtils.mustFile(regions, name));
    }
}
