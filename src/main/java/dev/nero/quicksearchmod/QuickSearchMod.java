package dev.nero.quicksearchmod;

import dev.nero.quicksearchmod.utils.RenderUtils;
import dev.nero.quicksearchmod.utils.SlotPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.*;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("quicksearchmod")
public class QuickSearchMod
{
    // creative inventory search: CreativeScreen#func_231160_c_()

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private boolean isSearching = false;
    private String searchText = "";
    private ArrayList<SlotPos> highlightedSlots = new ArrayList<>();

    private final int CHARS_LIMIT = 25;

    public QuickSearchMod() {
        MinecraftForge.EVENT_BUS.addListener(this::onKeyTypedEvent); // feeds searchText
        MinecraftForge.EVENT_BUS.addListener(this::onKeyPressedEvent); // handles backspace
        MinecraftForge.EVENT_BUS.addListener(this::onDrawForegroundEvent); // displays search field & highlights slots
        MinecraftForge.EVENT_BUS.addListener(this::onClientTickEvent); // container closed event handling
    }

    private void onKeyTypedEvent(final GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        // Works with russian letters and all that fancy stuff. It also makes sure that the key typed is allowed
        if (this.isSearching && this.searchText.length() < this.CHARS_LIMIT) {
            if (SharedConstants.isAllowedCharacter(event.getCodePoint())) {
                event.setCanceled(true);
                this.searchText += Character.toString(event.getCodePoint());
                this.updateHighlightedSlots();
            }
        }
    }

    private void onKeyPressedEvent(final GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        // It removes the last char when the backspace key is pressed
        if (this.isSearching && this.searchText.length() > 0) {
            if (event.getKeyCode() == 259) { // backspace
                event.setCanceled(true);
                this.searchText = this.searchText.substring(0, (this.searchText.length() - 1));
                this.updateHighlightedSlots();
            }
        }
    }

    private void onDrawForegroundEvent(final GuiContainerEvent.DrawForeground event) {
        // It draws the search box and it also highlights the found slots
        if (this.isSearchableContainer(event.getGuiContainer())) {
            this.isSearching = true;

            // Render search text
            RenderUtils.renderText(
                    "Search: " + searchText + "_",
                    0,
                    - Minecraft.getInstance().fontRenderer.FONT_HEIGHT - 5,
                    0xFFFFFF
            );

            // Highlight slots
            for (SlotPos slotPos : this.highlightedSlots) {
                RenderUtils.highlightSlot(slotPos);
            }
        }
    }

    private void onClientTickEvent(final TickEvent.ClientTickEvent event) {
        // There is no ContainedClosed event (as far as I know). So this function checks if the player has the container
        // closed. And if so, it stops the search (if it was enabled)

        if (Minecraft.getInstance().player != null) {
            if (Minecraft.getInstance().player.openContainer instanceof PlayerContainer && this.isSearching) {
                this.isSearching = false;
                // this.searchingFor = ""; it may be better to not reset the search field every time
                this.highlightedSlots = new ArrayList<>();
            }
        }
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
