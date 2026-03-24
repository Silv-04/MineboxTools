package fr.silv.hud.widget;

import fr.silv.ModConfig;
import fr.silv.availability.AvailabilityEntry;
import fr.silv.availability.AvailabilityRegistry;
import fr.silv.availability.AvailabilitySlot;
import fr.silv.constants.DaylightCycle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

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
     * Creates a new IconWidget instance.
     */
    public IconWidget() {
        super("icon_widget",
                ModConfig.getWidgetPosition("icon_widget")[0],
                ModConfig.getWidgetPosition("icon_widget")[1],
                ModConfig.getHudIconSize().getPixels(),
                ModConfig.getHudIconSize().getPixels());
    }

    @Override
    /**
     * Executes the render operation.
     * @param drawContext value for drawContext
     * @param client value for client
     */
    public void render(DrawContext drawContext, MinecraftClient client) {
        World world = client.world;
        if (world == null || client.player == null || client.options.hudHidden) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int iconSize = ModConfig.getHudIconSize().getPixels();
        ModConfig.IconOrientation configuredOrientation = ModConfig.getHudIconOrientation();
        ModConfig.IconDirection iconDirection = ModConfig.getHudIconDirection();
        ModConfig.IconOrientation effectiveOrientation = switch (iconDirection) {
            case LEFT, RIGHT -> ModConfig.IconOrientation.HORIZONTAL;
            case UP, DOWN -> ModConfig.IconOrientation.VERTICAL;
            case AUTO -> configuredOrientation;
        };

        LocalTime now = LocalTime.now(GAME_TIME_ZONE);
        List<AvailabilityEntry> entries = collectEntries(world, now);
        int iconCount = Math.max(1, entries.size());
        int delta = iconSize + ICON_SPACING;

        int layoutWidth = effectiveOrientation == ModConfig.IconOrientation.HORIZONTAL
                ? iconSize + ((iconCount - 1) * delta)
                : iconSize;
        int layoutHeight = effectiveOrientation == ModConfig.IconOrientation.VERTICAL
                ? iconSize + ((iconCount - 1) * delta)
                : iconSize;

        setSize(layoutWidth, layoutHeight);
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

        drawEntries(drawContext, layout, entries);
    }

    private static List<AvailabilityEntry> collectEntries(World world, LocalTime now) {
        List<AvailabilityEntry> entries = new ArrayList<>();
        entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.WEATHER, world, now));
        entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.ALL_DAY, world, now));

        if (world.isRaining() || world.isThundering()) {
            entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.BAD_WEATHER, world, now));
        } else {
            entries.addAll(AvailabilityRegistry.entriesForSlot(slotFor(now), world, now));
        }

        entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.SPECIAL, world, now));
        entries.addAll(AvailabilityRegistry.entriesForSlot(AvailabilitySlot.SHOP, world, now));
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

    private static void drawEntries(DrawContext context, IconLayout layout, List<AvailabilityEntry> entries) {
        for (AvailabilityEntry entry : entries) {
            layout.draw(context, entry.icon());
        }
    }

    private static void drawIcon(DrawContext context, Identifier icon, int x, int y, int iconSize) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, icon, x, y, 0f, 0f, iconSize, iconSize, iconSize, iconSize);
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

        private void draw(DrawContext context, Identifier icon) {
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
