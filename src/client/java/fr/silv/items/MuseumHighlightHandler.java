package fr.silv.items;

import fr.silv.ModConfig;
import fr.silv.api.MuseumDonationCache;
import fr.silv.utils.MineboxItemDataUtils;
import fr.silv.utils.MuseumItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a static golden border on inventory slots holding items that can be donated
 * to the museum but haven't been yet. State is persisted via ModConfig.
 */
@SuppressWarnings("null") // client.player is checked above before use
public final class MuseumHighlightHandler {
    private static final int BORDER_TINT = 0xFFFFD700;

    private MuseumHighlightHandler() {
    }

    /**
     * Returns whether the museum highlight feature is currently enabled.
     *
     * @return {@code true} when un-donated museum items should be highlighted
     */
    public static boolean isEnabled() {
        return ModConfig.isEnabled(ModConfig.FeatureFlag.MUSEUM);
    }

    /**
     * Updates the enabled state and persists the change.
     *
     * @param value {@code true} to enable the museum glint overlay
     */
    public static void setEnabled(boolean value) {
        ModConfig.setEnabled(ModConfig.FeatureFlag.MUSEUM, value);
        ModConfig.save();
    }

    /**
     * Draws a golden border on hotbar slots holding un-donated museum items.
     *
     * @param client  active client instance
     * @param context draw context
     */
    public static void renderHotbar(Minecraft client, GuiGraphicsExtractor context) {
        if (!isEnabled() || client.player == null || client.gui.hud.isHidden()) {
            return;
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int center = screenWidth / 2;
        int hotbarLeft = center - 91;
        int itemY = screenHeight - 19;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (shouldHighlight(stack)) {
                context.outline(hotbarLeft + i * 20 + 3, itemY, 16, 16, BORDER_TINT);
            }
        }

        ItemStack offHandStack = client.player.getOffhandItem();
        if (shouldHighlight(offHandStack)) {
            // Mirrors vanilla's off-hand icon placement (Gui#renderHotbar): it sits just
            // outside the hotbar, on the side opposite the player's main hand.
            int offHandX = client.player.getMainArm() == HumanoidArm.LEFT
                    ? center + 91 + 10
                    : center - 91 - 26;
            context.outline(offHandX, itemY, 16, 16, BORDER_TINT);
        }
    }

    /**
     * Draws a golden border on all slots holding un-donated museum items.
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

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.hasItem()) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (shouldHighlight(stack)) {
                context.outline(leftPos + slot.x, topPos + slot.y, 16, 16, BORDER_TINT);
            }
        }
    }

    private static boolean shouldHighlight(ItemStack stack) {
        if (stack.isEmpty() || !MuseumDonationCache.isLoaded()) {
            return false;
        }
        return MineboxItemDataUtils.getItemId(stack)
                .filter(MuseumItemUtils::isMuseumItem)
                .filter(itemId -> !MuseumDonationCache.isDonated(itemId))
                .isPresent();
    }
}
