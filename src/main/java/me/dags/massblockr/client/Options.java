package me.dags.massblockr.client;

import java.io.File;

/**
 * @author dags <dags@dags.me>
 */
public class Options {

    public File world = null;
    public File level = null;
    public int threads = 1;
    public boolean remap = true;
    public boolean remapOnly = false;
    public boolean schemsOnly = false;

    @Override
    public String toString() {
        return "Options{" +
                "world=" + world +
                ", level=" + level +
                ", threads=" + threads +
                ", remap=" + remap +
                ", remapOnly=" + remapOnly +
                ", schemsOnly=" + schemsOnly +
                '}';
    }
}
