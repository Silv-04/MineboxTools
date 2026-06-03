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
 */
public class IconWidget extends HudWidget {
    private static final int ICON_SPACING = 2;
    private static final ZoneId GAME_TIME_ZONE = ZoneId.of("UTC");

    /**
     * Creates a new IconWidget with persisted screen position and configured icon size.
     */
    public IconWidget() {
        this(initParams());
    }

    private IconWidget(int[] p) {
        super("icon_widget", p[0], p[1], p[2], p[3]);
    }

    private static int[] initParams() {
        int defaultSize = ModConfig.getHudIconSize().getPixels();
        int[] pos = ModConfig.getWidgetPosition("icon_widget");
        int[] size = ModConfig.getWidgetSavedSize("icon_widget", defaultSize, defaultSize);
        return new int[]{pos[0], pos[1], size[0], size[1]};
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
        if (level == null || client.player == null || client.options.hideGui) {
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

        int oldWidth = this.width;
        int oldHeight = this.height;
        setSize(layoutWidth, layoutHeight);

        if (!positiveDirection) {
            if (effectiveOrientation == ModConfig.IconOrientation.HORIZONTAL) {
                setPosition(this.x + (oldWidth - this.width), this.y);
            } else {
                setPosition(this.x, this.y + (oldHeight - this.height));
            }
        }

        keepInBounds(screenWidth, screenHeight);

        IconLayout layout = IconLayout.create(
                this.x,
                this.y,
                this.width,
                this.height,
                iconSize,
                effectiveOrientation,
                iconDirection,
                screenWidth,
                screenHeight
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
                                         ModConfig.IconDirection direction,
                                         int screenWidth,
                                         int screenHeight) {
            boolean positiveDirection = switch (direction) {
                case LEFT, UP -> false;
                case RIGHT, DOWN -> true;
                case AUTO -> orientation == ModConfig.IconOrientation.HORIZONTAL
                        ? x <= (screenWidth / 2)
                        : y <= (screenHeight / 2);
            };
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
