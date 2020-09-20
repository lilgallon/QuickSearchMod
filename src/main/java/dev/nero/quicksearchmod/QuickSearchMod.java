/*
* Copyright (C) 2020 @N3ROO on Github (Lilian Gallon)
* This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation; version 2. This program is distributed in the hope that it will
* be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
* PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General
* Public License along with this program.
*/

package dev.nero.quicksearchmod;

import dev.nero.quicksearchmod.utils.RenderUtils;
import dev.nero.quicksearchmod.utils.SlotPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@Mod("quicksearchmod")
public class QuickSearchMod
{
    // If you need to log some stuff
    private static final Logger LOGGER = LogManager.getLogger();
    // If true, it will listen for key inputs and cancel their default behaviour
    private boolean isSearching = false;
    // Contains what the player is searching for (not search will be performed if it's empty)
    private String searchText = "";
    // Contains all the highlighted slots that match what the player is searching for
    private ArrayList<SlotPos> highlightedSlots = new ArrayList<>();
    // Prevents the toggle key to be written in the search box (issue when there is no modifiers)
    private boolean cancelFirstLetter = false;

    // Default key to focus the search: CTRL + F
    public KeyBinding focusKeybinding = new KeyBinding(
            "key.quicksearchmod.focus",
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "keys.category.quicksearchmod"
    );

    public QuickSearchMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.addListener(this::onKeyTypedEventPost); // feeds searchText
        MinecraftForge.EVENT_BUS.addListener(this::onKeyPressedEventPre); // handles backspace
        MinecraftForge.EVENT_BUS.addListener(this::onDrawForegroundEvent); // displays search field & highlights slots
        MinecraftForge.EVENT_BUS.addListener(this::onClientTickEvent); // container closed event handling
    }

    private void setup(final FMLClientSetupEvent event) {
        LOGGER.info("Registering focus key");
        ClientRegistry.registerKeyBinding(focusKeybinding);
    }

    /***************************************************************************************/
    /* EVENTS
    /***************************************************************************************/

    private void onKeyTypedEventPost(final GuiScreenEvent.KeyboardCharTypedEvent.Post event) {
        // Works with russian letters and all that fancy stuff. It also makes sure that the key typed is allowed
        if (this.isSearching && this.searchText.length() < 25) {
            if (SharedConstants.isAllowedCharacter(event.getCodePoint()) && !this.cancelFirstLetter) {
                this.searchText += Character.toString(event.getCodePoint());
                this.updateHighlightedSlots();
            } else if (this.cancelFirstLetter) {
                this.cancelFirstLetter = false;
            }
        }
    }

    private void onKeyPressedEventPre(final GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        // It removes the last char when the backspace key is pressed
        if (this.isSearching && this.searchText.length() > 0) {
            boolean cancelEvent = true;

            if (event.getKeyCode() == GLFW.GLFW_KEY_BACKSPACE) {
                this.searchText = this.searchText.substring(0, (this.searchText.length() - 1));
                this.updateHighlightedSlots();
                cancelEvent = false; // we want the repeat behaviour to work
            } else if (event.getKeyCode() == GLFW.GLFW_KEY_ESCAPE) {
                this.isSearching = false;
            }

            event.setCanceled(cancelEvent);
        } else if (!this.isSearching && this.keyBindingPressed(this.focusKeybinding, event)) {
            if (this.focusKeybinding.getKeyModifier() == KeyModifier.NONE) {
                // When there is no modifiers, the "key-typed event" is fired, and we want to prevent the first key
                // typed to be written as it was the key to focus the search bar
                this.cancelFirstLetter = true;
            }

            this.isSearching = true;
        }
    }

    private void onDrawForegroundEvent(final GuiContainerEvent.DrawForeground event) {
        // It draws the search box and it also highlights the found slots
        if (this.isSearchableContainer(event.getGuiContainer())) {
            // Render search text
            RenderUtils.renderText(
                    this.isSearching ?
                            "Search: " + searchText + "_" :
                            "Search disabled (" + keybindToString(this.focusKeybinding) + ")",
                    0,
                    - Minecraft.getInstance().fontRenderer.FONT_HEIGHT - 5,
                    this.isSearching ? 0xFFFFFF : 0x888888
            );

            // Highlight slots
            for (SlotPos slotPos : this.highlightedSlots) {
                RenderUtils.highlightSlot(slotPos, 0x772fb000); // green with transparency
            }
        }
    }

    private void onClientTickEvent(final TickEvent.ClientTickEvent event) {
        // There is no ContainedClosed event (as far as I know). So this function checks if the player has the container
        // closed. And if so, it stops the search (if it was enabled)


        if (Minecraft.getInstance().player != null) {
            if (Minecraft.getInstance().currentScreen == null && this.isSearching) {
                this.isSearching = false;
                // this.searchingFor = ""; it may be better to not reset the search field every time
                this.highlightedSlots = new ArrayList<>();
            }
        }
    }

    /***************************************************************************************/
    /* UTILITY FUNCTIONS
    /***************************************************************************************/

    private String keybindToString(KeyBinding keyBinding) {
        return keyBinding.func_238171_j_().getString();
    }

    // needed because keybinding#isDown or keybinding#isPressed won't work for some reason
    private boolean keyBindingPressed(KeyBinding keyBinding, GuiScreenEvent.KeyboardKeyPressedEvent event) {
        /*
        * @see GLFW#GLFW_MOD_SHIFT 1
        * @see GLFW#GLFW_MOD_CONTROL 2
        * @see GLFW#GLFW_MOD_ALT 4
        * @see GLFW#GLFW_MOD_SUPER 8
        * NONE: 0
        */
        int modifierEvent = event.getModifiers();

        /*
        * SHIFT: 1
        * CONTROL: 0
        * ALT: 2
        * SUPER: -
        * NONE: 3
         */
        int modifierKeybind = keyBinding.getKeyModifier().ordinal();

        boolean sameModifiers = (
                (modifierEvent == 1 && modifierKeybind == 1) || // shift
                (modifierEvent == 2 && modifierKeybind == 0) || // control
                (modifierEvent == 4 && modifierKeybind == 2) || // alt
                (modifierEvent == 0 && modifierKeybind == 3)    // none
        );

        return sameModifiers && keyBinding.getKey().getKeyCode() == event.getKeyCode();
    }

    /**
     * It searches for the slots that match with the search field. Then, it adds them into the highlightedSlots array
     */
    private void updateHighlightedSlots() {
        highlightedSlots.clear();

        if (this.searchText.length() > 0) {
            for (Slot slot : Minecraft.getInstance().player.openContainer.inventorySlots) {
                String name = slot.getStack().getItem().getName().getString().toLowerCase();
                if (name.startsWith(this.searchText.toLowerCase()) || name.contains(this.searchText.toLowerCase())) {
                    highlightedSlots.add(new SlotPos(slot));
                }
            }
        }
    }

    /**
     * @param container any container
     * @return true if the container is searchable (means that we will put the search field on it)
     */
    private boolean isSearchableContainer(ContainerScreen container) {
        return (container instanceof InventoryScreen
                || container instanceof ChestScreen
                || container instanceof ShulkerBoxScreen
                || container instanceof HorseInventoryScreen
                || container instanceof CraftingScreen
        );
    }
}
