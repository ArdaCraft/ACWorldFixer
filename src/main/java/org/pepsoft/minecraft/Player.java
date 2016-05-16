/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.minecraft;

import static org.pepsoft.minecraft.Constants.ID_PLAYER;
import static org.pepsoft.minecraft.Constants.TAG_DIMENSION;
import static org.pepsoft.minecraft.Constants.TAG_INVENTORY;
import static org.pepsoft.minecraft.Constants.TAG_SCORE;

import java.util.Collections;

import org.jnbt.CompoundTag;
import org.jnbt.Tag;

/**
 *
 * @author pepijn
 */
public class Player extends Mob {
    public Player() {
        super(ID_PLAYER);
        setList(TAG_INVENTORY, CompoundTag.class, Collections.<Tag>emptyList());
        setInt(TAG_SCORE, 0);
        setInt(TAG_DIMENSION, 0);
    }

    public Player(CompoundTag tag) {
        super(tag);
    }

    private static final long serialVersionUID = 1L;
}