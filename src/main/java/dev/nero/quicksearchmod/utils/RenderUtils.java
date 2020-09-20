/*
 * Copyright (C) 2020 @N3ROO on Github (Lilian Gallon)
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; version 2. This program is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */

package dev.nero.quicksearchmod.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class RenderUtils {

    private static Minecraft MC = Minecraft.getInstance();

    /**
     * Renders some text with shadow. The position is relative to the current selected container. If none, it's relative
     * to the screen.
     * @param text the text that you want to write,
     * @param x the x position (from left to right),
     * @param y the y position (from top to bottom),
     * @param color the color in hex (00-FF), following this format: RRGGBB (R:red, G:green, B:blue). Ex: 0xFFFFFF
     */
    public static void renderText(String text, int x, int y, int color) {
        MC.fontRenderer.func_238405_a_(
                new MatrixStack(),
                text,
                x,
                y,
                color
        );
    }

    /**
     * Renders a rectangle. The position is relative to the current selected container. If none, it's relative to the
     * screen.
     * @param x the x position (from left to right),
     * @param y the y position (from top to bottom),
     * @param width the rectangle's width
     * @param height the rectangle's height
     * @param color its color in hex (0-FF), following this format: AARRGGBB (A:alpha R:red, G:green, B:blue)
     */
    public static void fillRect(int x, int y, int width, int height, int color) {
        AbstractGui.func_238467_a_(
                new MatrixStack(),
                x, y,
                x + width, y + height,
                color
        );
    }

    /**
     * It highlights the given slot
     * @param slotPos the slot pos
     * @param color the color (hex format: 0xAARRGGBB)
     */
    public static void highlightSlot(SlotPos slotPos, int color) {
        RenderUtils.fillRect(
                slotPos.getX(),
                slotPos.getY(),
                16, 16, // hard coded values. the source code of minecraft uses that same hard coded value
                color // white with some transparency
        );
    }
}
