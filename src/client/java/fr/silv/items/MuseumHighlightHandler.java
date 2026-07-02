package fr.silv.items;

import fr.silv.ModConfig;
import fr.silv.api.MuseumDonationCache;
import fr.silv.utils.MineboxItemDataUtils;
import fr.silv.utils.MuseumItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Overlays an unmasked, full-slot enchantment glint on inventory slots holding items that
 * can be donated to the museum but haven't been yet. State is persisted via ModConfig.
 *
 * <p>Vanilla masks the glint shader to the item's own pixel silhouette, which reads as a
 * thin, easy-to-miss sliver at 16px scale. Drawing the same texture/pipeline as a plain
 * full-slot quad instead keeps the "enchanted shimmer" look but makes it unmistakable.
 */
@SuppressWarnings("null") // client.player is checked above before use
public final class MuseumHighlightHandler {
    private static final Identifier GLINT_TEXTURE = Identifier.withDefaultNamespace("textures/misc/enchanted_glint_item.png");
    private static final long PULSE_PERIOD_MS = 900L;
    private static final float PULSE_MIN_ALPHA = 0.75f;
    private static final float PULSE_MAX_ALPHA = 1.0f;
    /** Vivid gold, more saturated than a pale tint so the glow reads clearly against any item. */
    private static final int GLINT_RGB = 0xFFD700;
    /**
     * The glint texture is a 128x128 diagonal-stripe pattern; minifying the whole thing into
     * a 16px slot is what makes the stripes read as distinct "bars". Passing the real texture
     * size here instead samples just its native-resolution top-left 16x16 corner, showing a
     * small soft patch of the pattern instead of the full repeating stripes.
     */
    private static final int GLINT_TEXTURE_SIZE = 128;

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
     * Draws a full-slot glint overlay on hotbar slots holding un-donated museum items.
     *
     * @param client  active client instance
     * @param context draw context
     */
    public static void renderHotbar(Minecraft client, GuiGraphicsExtractor context) {
        if (!isEnabled() || client.player == null || client.options.hideGui) {
            return;
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int hotbarLeft = screenWidth / 2 - 91;
        int itemY = screenHeight - 19;
        int tint = currentPulseTint();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (shouldHighlight(stack)) {
                drawGlint(context, hotbarLeft + i * 20 + 3, itemY, tint);
            }
        }
    }

    /**
     * Draws a full-slot glint overlay on all slots holding un-donated museum items.
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

        int tint = currentPulseTint();

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.hasItem()) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (shouldHighlight(stack)) {
                drawGlint(context, leftPos + slot.x, topPos + slot.y, tint);
            }
        }
    }

    private static void drawGlint(GuiGraphicsExtractor context, int x, int y, int tint) {
        context.blit(RenderPipelines.GLINT, GLINT_TEXTURE, x, y, 0f, 0f, 16, 16,
                GLINT_TEXTURE_SIZE, GLINT_TEXTURE_SIZE, tint);
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

    /**
     * Computes a pale gold tint whose alpha oscillates on a sine wave, so the glint intensity
     * pulses rather than staying static. Kept low-alpha overall so it reads as a shimmer
     * rather than obscuring the item icon underneath.
     *
     * @return ARGB tint for this frame's pulse phase
     */
    private static int currentPulseTint() {
        double phase = (System.currentTimeMillis() % PULSE_PERIOD_MS) / (double) PULSE_PERIOD_MS;
        float t = (float) ((Math.sin(phase * 2 * Math.PI) + 1.0) / 2.0);
        float alpha = PULSE_MIN_ALPHA + t * (PULSE_MAX_ALPHA - PULSE_MIN_ALPHA);
        int alphaByte = Math.round(alpha * 255f) & 0xFF;
        return (alphaByte << 24) | GLINT_RGB;
    }
}
