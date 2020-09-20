package dev.nero.quicksearchmod;

import dev.nero.quicksearchmod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.*;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("quicksearchmod")
public class QuickSearchMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public QuickSearchMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::onDrawForegroundEvent);
    }

    private void setup(final FMLCommonSetupEvent event)
    {

    }

    private void onDrawForegroundEvent(final GuiContainerEvent.DrawForeground event) {
        if (this.isSearchableContainer(event.getGuiContainer())) {
            RenderUtils.renderText("Search _", 0, - Minecraft.getInstance().fontRenderer.FONT_HEIGHT - 5, 0xFFFFFF);
            RenderUtils.highlightSlot(event.getGuiContainer().getContainer().getSlot(0));
        }
    }

    private boolean isSearchableContainer(ContainerScreen container) {
        return (container instanceof InventoryScreen
                || container instanceof ChestScreen
                || container instanceof ShulkerBoxScreen
                || container instanceof HorseInventoryScreen
                || container instanceof CraftingScreen
        );
    }


}
