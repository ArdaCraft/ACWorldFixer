package me.dags.massblockr.jnbt;

/**
 * @author dags <dags@dags.me>
 */
public class LongArrayTag extends Tag {

    private long[] value;

    public LongArrayTag(String name, long[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public int getTypeCode() {
        return NBTConstants.TYPE_LONG_ARRAY;
    }

    @Override
    public String getTypeName() {
        return NBTConstants.NAME_LONG_ARRAY;
    }

    @Override
    public long[] getValue() {
        return value;
    }

    @Override
    public LongArrayTag clone() {
        LongArrayTag clone = (LongArrayTag) super.clone();
        clone.value = value.clone();
        return clone;
    }
}
