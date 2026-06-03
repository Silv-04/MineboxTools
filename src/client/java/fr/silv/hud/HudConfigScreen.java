package fr.silv.hud;

import fr.silv.Lang;
import fr.silv.ModConfig;
import fr.silv.hud.widget.HudWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Configuration screen for positioning HUD widgets.
 */
public class HudConfigScreen extends Screen {
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_RED = 0xFFFF0000;

    private final List<HudWidget> widgets;
    private HudWidget draggingWidget = null;
    private int dragOffsetX;
    private int dragOffsetY;

    /**
     * Creates a new HudConfigScreen.
     *
     * @param widgets draggable HUD widgets to display and edit
     */
    public HudConfigScreen(List<HudWidget> widgets) {
        super(Component.literal("HUD Config"));
        this.widgets = widgets;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.literal(Lang.get("mineboxtools.menu.close")), button -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        for (HudWidget widget : widgets) {
            widget.keepInBounds(this.width, this.height);
            boolean hovered = widget.isMouseOver(mouseX, mouseY);
            int borderColor = hovered ? COLOR_RED : COLOR_WHITE;
            context.outline(widget.getX(), widget.getY(),
                    widget.getWidth(), widget.getHeight(),
                    borderColor);
            String id = widget.getId().replace("_widget", "");
            int labelX = widget.getX() + (widget.getWidth() / 2) - this.font.width(id) / 2;
            int labelY = widget.getY() - this.font.lineHeight - 2;
            context.text(this.font, Component.literal(id), labelX, labelY, borderColor);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mouseX = event.x();
            double mouseY = event.y();
            for (HudWidget widget : widgets) {
                if (widget.isMouseOver(mouseX, mouseY)) {
                    draggingWidget = widget;
                    dragOffsetX = (int) mouseX - widget.getX();
                    dragOffsetY = (int) mouseY - widget.getY();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingWidget != null && event.button() == 0) {
            int nextX = (int) event.x() - dragOffsetX;
            int nextY = (int) event.y() - dragOffsetY;
            int maxX = Math.max(0, this.width - draggingWidget.getWidth());
            int maxY = Math.max(0, this.height - draggingWidget.getHeight());
            draggingWidget.setPosition(
                    Math.max(0, Math.min(nextX, maxX)),
                    Math.max(0, Math.min(nextY, maxY)));
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingWidget != null && event.button() == 0) {
            ModConfig.setWidgetPosition(draggingWidget.getId(),
                    draggingWidget.getX(), draggingWidget.getY(),
                    draggingWidget.getWidth(), draggingWidget.getHeight());
            draggingWidget = null;
            return true;
        }
        return super.mouseReleased(event);
    }
}
