package me.dags.blockr.extra;

/**
 * @author dags <dags@dags.me>
 */
public enum LegacyArt {

    A1x1_0(16, 16, 0, 0),
    A1x1_1(16, 16, 16, 0),
    A1x1_2(16, 16, 32, 0),
    A1x1_3(16, 16, 48, 0),
    A1x1_4(16, 16, 64, 0),
    A1x1_5(16, 16, 80, 0),
    A1x1_6(16, 16, 96, 0),
    A1x1_7(16, 16, 112, 0),
    A1x1_8(16, 16, 128, 0),
    A1x1_9(16, 16, 144, 0),
    A1x1_10(16, 16, 160, 0),
    A1x1_11(16, 16, 176, 0),

    A1x1_12(16, 16, 0, 16),
    A1x1_13(16, 16, 16, 16),
    A1x1_14(16, 16, 32, 16),
    A1x1_15(16, 16, 48, 16),
    A1x1_16(16, 16, 64, 16),
    A1x1_17(16, 16, 80, 16),
    A1x1_18(16, 16, 96, 16),
    A1x1_19(16, 16, 112, 16),
    A1x1_20(16, 16, 128, 16),
    A1x1_21(16, 16, 144, 16),
    A1x1_22(16, 16, 160, 16),
    A1x1_23(16, 16, 176, 16),

    A2x1_24(32, 16, 0, 32),
    A2x1_25(32, 16, 32, 32),
    A2x1_26(32, 16, 64, 32),
    A2x1_27(32, 16, 96, 32),
    A2x1_28(32, 16, 128, 32),
    A2x1_29(32, 16, 160, 32),

    A2x1_30(32, 16, 0, 48),
    A2x1_31(32, 16, 32, 48),
    A2x1_32(32, 16, 64, 48),
    A2x1_33(32, 16, 96, 48),
    A2x1_34(32, 16, 128, 48),
    A2x1_35(32, 16, 160, 48),

    A1x2_36(16, 32, 0, 64),
    A1x2_37(16, 32, 16, 64),
    A1x2_38(16, 32, 32, 64),
    A1x2_39(16, 32, 48, 64),
    A1x2_40(16, 32, 64, 64),
    A1x2_41(16, 32, 80, 64),
    A1x2_42(16, 32, 96, 64),
    A1x2_43(16, 32, 112, 64),
    A1x2_44(16, 32, 128, 64),
    A1x2_45(16, 32, 144, 64),
    A1x2_46(16, 32, 160, 64),
    A1x2_47(16, 32, 176, 64),

    A4x2_48(64, 32, 0, 96),
    A4x2_49(64, 32, 64, 96),
    A4x2_50(64, 32, 128, 96),

    A2x2_51(32, 32, 0, 128),
    A2x2_52(32, 32, 32, 128),
    A2x2_53(32, 32, 64, 128),
    A2x2_54(32, 32, 96, 128),
    A2x2_55(32, 32, 128, 128),
    A2x2_56(32, 32, 160, 128),

    A2x2_57(32, 32, 0, 160),
    A2x2_58(32, 32, 32, 160),
    A2x2_59(32, 32, 64, 160),
    A2x2_60(32, 32, 96, 160),
    A2x2_61(32, 32, 128, 160),
    A2x2_62(32, 32, 160, 160),
    A2x2_63(32, 32, 192, 160),
    A2x2_64(32, 32, 224, 160),

    A4x4_65(64, 64, 0, 192),
    A4x4_66(64, 64, 64, 192),
    A4x4_67(64, 64, 128, 192),
    A4x4_68(64, 64, 192, 192),

    A4x3_69(64, 48, 192, 64),
    A4x3_70(64, 48, 192, 112),;

    public final int sizeX;
    public final int sizeY;
    public final int offsetX;
    public final int offsetY;
    public final String shape;

    LegacyArt(int x, int y, int offsetX, int offsetY) {
        this.sizeX = x;
        this.sizeY = y;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.shape = sizeX % 16 + "x" + sizeY % 16;
    }

    public static LegacyArt forName(String name) {
        for (LegacyArt art : LegacyArt.values()) {
            if (art.name().equals(name)) {
                return art;
            }
        }
        return LegacyArt.A1x1_0;
    }

    public Art toArt() {
        int index = ordinal();
        Art[] arts = Art.values();

        if (index < arts.length) {
            return arts[index];
        }

        throw new UnsupportedOperationException(name());
    }
}
