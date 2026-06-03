package fr.silv.items;

import fr.silv.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalInt;

/**
 * Renders a golden highlight border on inventory slots whose item score exceeds
 * the configured threshold. State is persisted via ModConfig.
 */
public final class ItemHighlightHandler {
    private static final int COLOR_GOLD = 0xFFFFD700;

    private ItemHighlightHandler() {
    }

    /**
     * Updates the minimum score threshold and persists the change.
     *
     * @param value new threshold value (clamped to [0, 100] by {@link ModConfig})
     */
    public static void setThreshold(int value) {
        ModConfig.setHighlightThreshold(value);
        ModConfig.save();
    }

    /**
     * Returns the configured minimum score threshold for highlight rendering.
     *
     * @return threshold in the range [0, 100]
     */
    public static int getThreshold() {
        return ModConfig.getHighlightThreshold();
    }

    /**
     * Updates the enabled state and persists the change.
     *
     * @param value {@code true} to enable highlight rendering
     */
    public static void setEnabled(boolean value) {
        ModConfig.setHighlightEnabled(value);
        ModConfig.save();
    }

    /**
     * Returns whether highlight rendering is currently enabled.
     *
     * @return {@code true} when golden borders should be drawn
     */
    public static boolean isEnabled() {
        return ModConfig.isHighlightEnabled();
    }

    /**
     * Draws gold borders on hotbar slots whose item score exceeds the threshold.
     *
     * @param client  active client instance
     * @param context draw context
     */
    public static void renderHotbar(Minecraft client, GuiGraphicsExtractor context) {
        if (!isEnabled() || client.player == null || client.options.hideGui) {
            return;
        }

        int threshold = getThreshold();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int hotbarLeft = screenWidth / 2 - 91;
        int itemY = screenHeight - 19;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            OptionalInt scoreOpt = TooltipHandler.computeItemScore(stack);
            if (scoreOpt.isEmpty() || scoreOpt.getAsInt() < threshold) {
                continue;
            }
            context.outline(hotbarLeft + i * 20 + 3, itemY, 16, 16, COLOR_GOLD);
        }
    }

    /**
     * Draws gold borders on all slots whose item score exceeds the threshold.
     *
     * @param screen  the open container screen
     * @param context draw context
     * @param leftPos screen X offset of the container GUI
     * @param topPos  screen Y offset of the container GUI
     */
    public static void render(AbstractContainerScreen<?> screen, GuiGraphicsExtractor context, int leftPos, int topPos) {
        if (!isEnabled()) {
            return;
        }

        int threshold = getThreshold();

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.hasItem()) {
                continue;
            }
            OptionalInt scoreOpt = TooltipHandler.computeItemScore(slot.getItem());
            if (scoreOpt.isEmpty() || scoreOpt.getAsInt() < threshold) {
                continue;
            }
            context.outline(leftPos + slot.x, topPos + slot.y, 16, 16, COLOR_GOLD);
        }
    }
}
