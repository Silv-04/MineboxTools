package fr.silv.hud.widget;

import fr.silv.ModConfig;
import fr.silv.effects.ActiveConsumable;
import fr.silv.effects.ActiveEffectsStore;
import fr.silv.effects.CatalogEffect;
import fr.silv.effects.EffectIconCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * HUD widget that shows the active Minebox consumables as a grid of icons, anchored at its top-left
 * and growing right then down. Cells are sorted by remaining time, soonest-to-expire first (top-left).
 *
 * <p>Each icon is the effect's catalog image, or the effects-menu item itself when the catalog ships
 * none. Around each icon sits a ring whose arc length is the remaining fraction of the effect's base
 * duration and whose colour runs a red-to-green gradient along that arc - so a short, mostly-red ring
 * reads as "about to expire". The fraction can exceed 1 (e.g. an enchanted variant lasts longer than
 * the base duration); for now that is clamped to a full ring.
 */
public class EffectsWidget extends HudWidget {
    private static final int COLUMNS = 6;
    private static final int SPRITE = 16;
    /** Ring centre-line radius: tight to the sprite, so the square's corners poke just outside it. */
    private static final int RING_RADIUS = 11;
    private static final float RING_THICKNESS = 1.4f;
    private static final int CELL_PADDING = 4;
    private static final int SEGMENTS = 64;
    /** Everything is authored full-size then scaled down once, for a crisp compact HUD. */
    private static final float WIDGET_SCALE = 0.75f;
    /** Padding between the panel edge and the icon grid, on every side. */
    private static final int PANEL_PADDING = 6;
    private static final int CORNER_RADIUS = 6;
    /** Faint dark panel for contrast - copied exactly from MineboxAdditions' HUD background. */
    private static final int PANEL_COLOR = 0x40000000;
    private static final int UNKNOWN_RING_COLOR = 0xFF888888;

    public EffectsWidget() {
        super("effects_widget",
                ModConfig.getWidgetPosition("effects_widget")[0],
                ModConfig.getWidgetPosition("effects_widget")[1],
                scaled(cellSize() + 2 * PANEL_PADDING), scaled(cellSize() + 2 * PANEL_PADDING));
    }

    private static int cellSize() {
        return 2 * (RING_RADIUS + 2) + CELL_PADDING;
    }

    /** Authored pixels converted to on-screen pixels at the widget's display scale. */
    private static int scaled(int authored) {
        return Math.round(authored * WIDGET_SCALE);
    }

    @Override
    public void render(GuiGraphicsExtractor context, Minecraft client) {
        if (!ModConfig.isEnabled(ModConfig.FeatureFlag.EFFECTS)) {
            return;
        }
        if (client.gui.hud.isHidden()) {
            return;
        }

        List<ActiveConsumable> active = new ArrayList<>(ActiveEffectsStore.getActive());
        active.sort(Comparator.comparingLong(ActiveConsumable::remainingMillis));

        int count = active.size();
        int cell = cellSize();
        int columns = Math.min(COLUMNS, Math.max(1, count));
        int rows = Math.max(1, (int) Math.ceil(count / (double) COLUMNS));
        int authoredWidth = columns * cell + 2 * PANEL_PADDING;
        int authoredHeight = rows * cell + 2 * PANEL_PADDING;

        // Bounds are the on-screen (scaled) size; a one-cell minimum keeps the widget grabbable in
        // the HUD config screen even when empty.
        setSize(scaled(authoredWidth), scaled(authoredHeight));
        keepInBounds(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());

        if (count == 0) {
            return;
        }

        // Author everything at full size in local coordinates, then scale the whole widget down once
        // so the geometry stays crisp instead of being rounded to half-size pixels.
        context.pose().pushMatrix();
        context.pose().translate(getX(), getY());
        context.pose().scale(WIDGET_SCALE, WIDGET_SCALE);

        roundedRect(context, 0, 0, authoredWidth, authoredHeight, CORNER_RADIUS, PANEL_COLOR);
        for (int i = 0; i < count; i++) {
            int cellX = PANEL_PADDING + (i % COLUMNS) * cell;
            int cellY = PANEL_PADDING + (i / COLUMNS) * cell;
            drawEffect(context, active.get(i), cellX + cell / 2, cellY + cell / 2);
        }

        context.pose().popMatrix();
    }

    /** Filled rounded rectangle drawn from {@code fill} spans - no texture, no extra dependency. */
    private static void roundedRect(GuiGraphicsExtractor context, int x, int y, int width, int height,
                                    int radius, int color) {
        int x2 = x + width;
        int y2 = y + height;
        int r = Math.min(radius, Math.min(width, height) / 2);
        context.fill(x, y + r, x2, y2 - r, color); // middle band, full width
        for (int i = 0; i < r; i++) {
            int dist = r - i; // distance in rows from the corner's centre
            int inset = r - (int) Math.round(Math.sqrt((double) r * r - (double) dist * dist));
            context.fill(x + inset, y + i, x2 - inset, y + i + 1, color);       // top edge
            context.fill(x + inset, y2 - 1 - i, x2 - inset, y2 - i, color);     // bottom edge
        }
    }

    private void drawEffect(GuiGraphicsExtractor context, ActiveConsumable effect, int centerX, int centerY) {
        Optional<CatalogEffect> catalog = effect.catalogEntry();
        Optional<EffectIconCache.Icon> icon = catalog.flatMap(EffectIconCache::get);
        if (icon.isPresent()) {
            drawIconTexture(context, icon.get(), centerX, centerY);
        } else {
            context.item(effect.menuStack(), centerX - 8, centerY - 8);
        }
        drawRing(context, centerX, centerY, proportion(effect, catalog));
    }

    private static void drawIconTexture(GuiGraphicsExtractor context, EffectIconCache.Icon icon, int centerX, int centerY) {
        float scale = SPRITE / (float) Math.max(icon.width(), icon.height());
        context.pose().pushMatrix();
        context.pose().translate(centerX - (icon.width() * scale) / 2f, centerY - (icon.height() * scale) / 2f);
        context.pose().scale(scale, scale);
        context.blit(RenderPipelines.GUI_TEXTURED, icon.textureId(), 0, 0, 0f, 0f,
                icon.width(), icon.height(), icon.width(), icon.height());
        context.pose().popMatrix();
    }

    private static void drawRing(GuiGraphicsExtractor context, int centerX, int centerY, OptionalDouble proportion) {
        boolean unknown = proportion.isEmpty();
        double clamped = Math.max(0.0, Math.min(1.0, proportion.orElse(1.0)));
        int drawn = (int) Math.round(clamped * SEGMENTS);

        double step = (2 * Math.PI) / SEGMENTS;
        // Overlap neighbours slightly so the ticks read as a continuous ring rather than dots.
        int halfLen = Math.max(2, (int) Math.ceil(step * RING_RADIUS / 2.0) + 1);

        // One solid colour for the whole ring, from the remaining fraction: green when full, red
        // when nearly gone, shifting as it counts down.
        int color = unknown ? UNKNOWN_RING_COLOR : gradientColor(clamped);
        for (int i = 0; i < drawn; i++) {
            // Grow anticlockwise from 12 o'clock so the arc depletes clockwise as time runs down.
            double angle = -Math.PI / 2.0 - i * step;
            context.pose().pushMatrix();
            context.pose().translate(centerX, centerY);
            context.pose().rotate((float) angle);
            // Draw a unit-wide radial band and stretch it to the (fractional) thickness via the matrix,
            // since fill() itself only takes integer widths.
            context.pose().translate(RING_RADIUS, 0f);
            context.pose().scale(RING_THICKNESS, 1f);
            context.fill(0, -halfLen, 1, halfLen, color);
            context.pose().popMatrix();
        }
    }

    /** Remaining time as a fraction of the effect's base duration; empty when the duration is unknown. */
    private static OptionalDouble proportion(ActiveConsumable effect, Optional<CatalogEffect> catalog) {
        if (catalog.isPresent() && catalog.get().duration != null && catalog.get().duration > 0) {
            return OptionalDouble.of(effect.remainingSeconds() / (double) catalog.get().duration);
        }
        return OptionalDouble.empty();
    }

    /** Red (fraction 0) through to green (fraction 1), muted rather than vivid. */
    private static int gradientColor(double fraction) {
        float hue = (float) (Math.max(0.0, Math.min(1.0, fraction)) * (120.0 / 360.0));
        return 0xFF000000 | (Color.HSBtoRGB(hue, 0.5f, 0.85f) & 0xFFFFFF);
    }
}
