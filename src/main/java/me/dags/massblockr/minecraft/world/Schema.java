package me.dags.massblockr.minecraft.world;

/**
 * @author dags <dags@dags.me>
 */
public class Schema {

    public static final int PRE113_SCHEMA = 0;
    public static final int POST113_SCHEMA = 1;

    public static int getSchema(Level level) {
        if (level.getVersionId() < 1400) {
            return PRE113_SCHEMA;
        }
        return POST113_SCHEMA;
    }
}
