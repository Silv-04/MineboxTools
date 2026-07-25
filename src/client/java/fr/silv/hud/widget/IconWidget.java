package fr.silv.hud.widget;

import fr.silv.ModConfig;
import fr.silv.availability.AvailabilityEntry;
import fr.silv.availability.AvailabilityRegistry;
import fr.silv.availability.AvailabilitySlot;
import fr.silv.constants.DaylightCycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * HUD widget that displays contextual status icons.
 *
 * <p>{@code x}/{@code y} hold the <b>anchor</b>: the top-left corner of the first icon.
 * The anchor is the persisted base position and is never derived from the widget size,
 * so changing the icon count never moves it. The visual bounding box (used for dragging,
 * hover detection and bounds clamping) is computed from the anchor each frame.</p>
 */
public class IconWidget extends HudWidget {
    private static final int ICON_SPACING = 2;
    private static final ZoneId GAME_TIME_ZONE = ZoneId.of("UTC");

    private ModConfig.IconOrientation lastOrientation = ModConfig.IconOrientation.HORIZONTAL;
    private boolean lastPositive = true;
    private int lastIconSize = ModConfig.IconSize.NORMAL.getPixels();

    /**
     * Creates a new IconWidget with persisted anchor position and configured icon size.
     */
    public IconWidget() {
        this(initParams());
    }

    private IconWidget(int[] p) {
        super("icon_widget", p[0], p[1], p[2], p[3]);
    }

    private static int[] initParams() {
        int iconSize = ModConfig.getHudIconSize().getPixels();
        int[] pos = ModConfig.getWidgetPosition("icon_widget");
        // x/y = anchor; size starts at one icon and is updated by setSize() in render().
        return new int[]{pos[0], pos[1], iconSize, iconSize};
    }

    /**
     * Returns the visual top-left X of the icon block, derived from the anchor.
     */
    @Override
    public int getX() {
        if (lastOrientation == ModConfig.IconOrientation.HORIZONTAL && !lastPositive) {
            return x - (width - lastIconSize);
        }
        return x;
    }

    /**
     * Returns the visual top-left Y of the icon block, derived from the anchor.
     */
    @Override
    public int getY() {
        if (lastOrientation == ModConfig.IconOrientation.VERTICAL && !lastPositive) {
            return y - (height - lastIconSize);
        }
        return y;
    }

    /**
     * Converts a visual top-left X into the stored anchor X (top-left of the first icon).
     *
     * @param visualX visual top-left X coordinate
     * @return anchor X coordinate
     */
    @Override
    protected int anchorXFromVisual(int visualX) {
        if (lastOrientation == ModConfig.IconOrientation.HORIZONTAL && !lastPositive) {
            return visualX + (width - lastIconSize);
        }
        return visualX;
    }

    /**
     * Converts a visual top-left Y into the stored anchor Y (top-left of the first icon).
     *
     * @param visualY visual top-left Y coordinate
     * @return anchor Y coordinate
     */
    @Override
    protected int anchorYFromVisual(int visualY) {
        if (lastOrientation == ModConfig.IconOrientation.VERTICAL && !lastPositive) {
            return visualY + (height - lastIconSize);
        }
        return visualY;
    }

    /**
     * Renders active availability icons using current orientation and direction settings.
     *
     * @param drawContext draw context
     * @param client      active client instance
     */
    @Override
    public void render(GuiGraphicsExtractor drawContext, Minecraft client) {
        Level level = client.level;
        if (level == null || client.player == null || client.gui.hud.isHidden()) {
            return;
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int iconSize = ModConfig.getHudIconSize().getPixels();
        ModConfig.IconOrientation configuredOrientation = ModConfig.getHudIconOrientation();
        ModConfig.IconDirection iconDirection = ModConfig.getHudIconDirection();
        ModConfig.IconOrientation effectiveOrientation = switch (iconDirection) {
            case LEFT, RIGHT -> ModConfig.IconOrientation.HORIZONTAL;
            case UP, DOWN -> ModConfig.IconOrientation.VERTICAL;
            case AUTO -> configuredOrientation;
        };

        // AUTO direction is decided from the stable anchor, never from the moving block edge.
        boolean positiveDirection = switch (iconDirection) {
            case LEFT, UP -> false;
            case RIGHT, DOWN -> true;
            case AUTO -> effectiveOrientation == ModConfig.IconOrientation.HORIZONTAL
                    ? this.x <= (screenWidth / 2)
                    : this.y <= (screenHeight / 2);
        };

        LocalTime now = LocalTime.now(GAME_TIME_ZONE);
        List<AvailabilityEntry> entries = collectEntries(level, now);
        int iconCount = Math.max(1, entries.size());
        int delta = iconSize + ICON_SPACING;

        int layoutWidth = effectiveOrientation == ModConfig.IconOrientation.HORIZONTAL
                ? iconSize + ((iconCount - 1) * delta)
                : iconSize;
        int layoutHeight = effectiveOrientation == ModConfig.IconOrientation.VERTICAL
                ? iconSize + ((iconCount - 1) * delta)
                : iconSize;

        lastOrientation = effectiveOrientation;
        lastPositive = positiveDirection;
        lastIconSize = iconSize;

        setSize(layoutWidth, layoutHeight);
        keepInBounds(screenWidth, screenHeight);

        IconLayout layout = IconLayout.create(
                getX(),
                getY(),
                this.width,
                this.height,
                iconSize,
                effectiveOrientation,
                positiveDirection
        );

        for (AvailabilityEntry entry : entries) {
            layout.draw(drawContext, entry.icon());
        }
    }

    private static List<AvailabilityEntry> collectEntries(Level level, LocalTime now) {
        List<AvailabilityEntry> entries = new ArrayList<>();
        entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.WEATHER, level, now));
        entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.ALL_DAY, level, now));

        if (level.isRaining() || level.isThundering()) {
            entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.BAD_WEATHER, level, now));
        } else {
            entries.addAll(AvailabilityRegistry.entriesForSlot(slotFor(now), level, now));
        }

        entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.SPECIAL, level, now));
        entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.SHOP, level, now));
        return entries;
    }

    private static AvailabilitySlot slotFor(LocalTime now) {
        if (DaylightCycle.isMorning(now)) {
            return AvailabilitySlot.MORNING;
        }
        if (DaylightCycle.isAfternoon(now)) {
            return AvailabilitySlot.AFTERNOON;
        }
        if (DaylightCycle.isEvening(now)) {
            return AvailabilitySlot.EVENING;
        }
        return AvailabilitySlot.NIGHT;
    }

    private static void drawIcon(GuiGraphicsExtractor context, Identifier icon, int x, int y, int iconSize) {
        context.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0f, 0f, iconSize, iconSize, iconSize, iconSize);
    }

    private static final class IconLayout {
        private final int baseX;
        private final int baseY;
        private final int width;
        private final int height;
        private final int iconSize;
        private final ModConfig.IconOrientation orientation;
        private final boolean positiveDirection;
        private int offsetIndex;

        private IconLayout(int baseX,
                           int baseY,
                           int width,
                           int height,
                           int iconSize,
                           ModConfig.IconOrientation orientation,
                           boolean positiveDirection) {
            this.baseX = baseX;
            this.baseY = baseY;
            this.width = width;
            this.height = height;
            this.iconSize = iconSize;
            this.orientation = orientation;
            this.positiveDirection = positiveDirection;
        }

        private static IconLayout create(int x,
                                         int y,
                                         int width,
                                         int height,
                                         int iconSize,
                                         ModConfig.IconOrientation orientation,
                                         boolean positiveDirection) {
            return new IconLayout(x, y, width, height, iconSize, orientation, positiveDirection);
        }

        private void draw(GuiGraphicsExtractor context, Identifier icon) {
            int delta = iconSize + ICON_SPACING;
            int drawX;
            int drawY;

            if (orientation == ModConfig.IconOrientation.HORIZONTAL) {
                int originX = positiveDirection ? baseX : baseX + width - iconSize;
                int sign = positiveDirection ? 1 : -1;
                drawX = originX + (offsetIndex * sign * delta);
                drawY = baseY;
            } else {
                int originY = positiveDirection ? baseY : baseY + height - iconSize;
                int sign = positiveDirection ? 1 : -1;
                drawX = baseX;
                drawY = originY + (offsetIndex * sign * delta);
            }

            IconWidget.drawIcon(context, icon, drawX, drawY, iconSize);
            offsetIndex++;
        }
    }
}
