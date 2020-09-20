/*
 * Copyright (C) 2020 @N3ROO on Github (Lilian Gallon)
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; version 2. This program is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */

package dev.nero.quicksearchmod.utils;

import net.minecraft.inventory.container.Slot;

public class SlotPos {

    private final int X;
    private final int Y;

    /**
     * Can be used in arrays to make sure that a slot is unique
     * @param slot the slot
     */
    public SlotPos(Slot slot) {
        this.X = slot.xPos;
        this.Y = slot.yPos;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SlotPos slotPos = (SlotPos) o;

        if (X != slotPos.X) return false;
        return Y == slotPos.Y;
    }

    @Override
    public int hashCode() {
        int result = X;
        result = 31 * result + Y;
        return result;
    }
}
