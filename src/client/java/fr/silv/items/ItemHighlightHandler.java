package fr.silv.items;

import fr.silv.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalInt;

/**
 * Renders a full-slot enchantment glint overlay on inventory slots whose item score
 * exceeds the configured threshold. State is persisted via ModConfig.
 *
 * <p>Vanilla masks the glint shader to the item's own pixel silhouette, which reads as a
 * thin, easy-to-miss sliver at 16px scale. Drawing the same texture/pipeline as a plain
 * full-slot quad instead keeps the "enchanted shimmer" look but makes it unmistakable.
 */
public final class ItemHighlightHandler {
    private static final Identifier GLINT_TEXTURE = Identifier.withDefaultNamespace("textures/misc/enchanted_glint_item.png");
    private static final int GLINT_TINT = 0xFFFFFFFF;
    private static final int GLINT_TEXTURE_SIZE = 128;
    /** Drawing the glint twice compounds the shader's additive blending for a stronger shimmer. */
    private static final int GLINT_PASSES = 2;

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
     * Draws a glint overlay on hotbar slots whose item score exceeds the threshold.
     *
     * @param client  active client instance
     * @param context draw context
     */
    public static void renderHotbar(Minecraft client, GuiGraphicsExtractor context) {
        if (!isEnabled() || client.player == null || client.gui.hud.isHidden()) {
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
            drawGlint(context, hotbarLeft + i * 20 + 3, itemY);
        }
    }

    /**
     * Draws a glint overlay on all slots whose item score exceeds the threshold.
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
            drawGlint(context, leftPos + slot.x, topPos + slot.y);
        }
    }

    private static void drawGlint(GuiGraphicsExtractor context, int x, int y) {
        for (int i = 0; i < GLINT_PASSES; i++) {
            context.blit(RenderPipelines.GLINT, GLINT_TEXTURE, x, y, 0f, 0f, 16, 16,
                    GLINT_TEXTURE_SIZE, GLINT_TEXTURE_SIZE, GLINT_TINT);
        }
    }
}
