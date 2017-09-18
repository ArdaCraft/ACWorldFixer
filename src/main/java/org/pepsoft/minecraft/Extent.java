package org.pepsoft.minecraft;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public interface Extent {

    int getWidth();

    int getHeight();

    int getLength();

    List<Entity> getEntities();

    int getBlockType(int x, int y, int z);

    int getDataValue(int x, int y, int z);

    int getBiome(int x, int z);

    void setBlockType(int x, int y, int z, int type);

    void setDataValue(int x, int y, int z, int type);
}
