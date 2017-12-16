package me.dags.massblockr.minecraft.world;

/**
 * @author dags <dags@dags.me>
 */
public interface LegacyBlockHandler {

    default int getStateId(int id, int meta) {
        return id + (meta << 12);
    }

    default int getBlockId(int stateId) {
        return stateId & 4095;
    }

    default int getMetaData(int stateId) {
        return (stateId >> 12) & 15;
    }

    default int getShort(int index, byte[] blocks, byte[] adds) {
        if ((index >> 1) >= adds.length) {
            return blocks[index] & 0xFF;
        } else {
            return getNibble(index, adds) + (blocks[index] & 0xFF);
        }
    }

    default int getNibble(int index, byte[] values) {
        if ((index & 1) == 0) {
            return (values[index >> 1] & 0x0F) << 8;
        }
        return (values[index >> 1] & 0xF0) << 4;
    }

    default byte[] setShort(int index, int id, byte[] blocks, byte[] adds) {
        if (id > 255) {
            if (adds.length == 0) {
                adds = new byte[(blocks.length >> 1) + 1];
            }
            if ((index & 1) == 0) {
                adds[index >> 1] = (byte) (adds[index >> 1] & 0xF0 | (id >> 8) & 0xF);
            } else {
                adds[index >> 1] = (byte) (adds[index >> 1] & 0xF | ((id >> 8) & 0xF) << 4);
            }
        }
        blocks[index] = (byte) id;
        return adds;
    }

    default void setNibble(int index, int meta, byte[] data) {
        int i = index >> 1;
        byte value = data[i];
        if ((i & 1) == 0) {
            value &= 0xF0;
            value |= (meta & 0x0F);
        } else {
            value &= 0x0F;
            value |= ((meta & 0x0F) << 4);
        }
        data[i] = value;
    }
}
