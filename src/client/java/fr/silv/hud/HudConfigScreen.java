package fr.silv.hud;

import fr.silv.Lang;
import fr.silv.ModConfig;
import fr.silv.hud.widget.HudWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.List;

/**
 * Configuration screen for positioning HUD widgets.
 */
public class HudConfigScreen extends Screen {

    private final List<HudWidget> widgets;
    private HudWidget draggingWidget = null;
    private int dragOffsetX, dragOffsetY;

    /**
     * Creates a new HudConfigScreen instance.
     * @param widgets value for widgets
     */
    public HudConfigScreen(List<HudWidget> widgets) {
        super(Text.literal("HUD Config"));
        this.widgets = widgets;
    }

    @Override
    /**
     * Executes the init operation.
     */
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(Lang.get("mineboxtools.menu.close")),button -> {
            this.close();
        }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    /**
     * Executes the render operation.
     * @param context value for context
     * @param mouseX value for mouseX
     * @param mouseY value for mouseY
     * @param delta value for delta
     */
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (HudWidget widget : widgets) {
            widget.keepInBounds(this.width, this.height);
            if (widget.isMouseOver(mouseX, mouseY)) {
                context.drawBorder(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), 0xFFFFFF);
            }
            context.drawBorder(widget.getX(), widget.getY(),
                    widget.getWidth(), widget.getHeight(),
                    Colors.WHITE);
            String id = widget.getId().replace("_widget", "");
            context.drawTextWithShadow(this.textRenderer, Text.literal(id),
                    widget.getX() + (widget.getWidth() /2) - id.length()*2, widget.getY() + (widget.getHeight() /2) - 6, Colors.WHITE);
            if (widget.isMouseOver(mouseX, mouseY)) {
                context.drawBorder(widget.getX(), widget.getY(),
                        widget.getWidth(), widget.getHeight(),
                        Colors.RED);
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    /**
     * Executes the mouse clicked operation.
     * @param mouseX value for mouseX
     * @param mouseY value for mouseY
     * @param button value for button
     * @return true when the operation succeeds; otherwise false
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (HudWidget widget : widgets) {
                if (widget.isMouseOver((int) mouseX, (int) mouseY)) {
                    draggingWidget = widget;
                    dragOffsetX = (int) mouseX - widget.getX();
                    dragOffsetY = (int) mouseY - widget.getY();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    /**
     * Executes the mouse dragged operation.
     * @param mouseX value for mouseX
     * @param mouseY value for mouseY
     * @param button value for button
     * @param dx value for dx
     * @param dy value for dy
     * @return true when the operation succeeds; otherwise false
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingWidget != null && button == 0) {
            int nextX = (int) mouseX - dragOffsetX;
            int nextY = (int) mouseY - dragOffsetY;
            int maxX = Math.max(0, this.width - draggingWidget.getWidth());
            int maxY = Math.max(0, this.height - draggingWidget.getHeight());
            draggingWidget.setPosition(
                    Math.max(0, Math.min(nextX, maxX)),
                    Math.max(0, Math.min(nextY, maxY))
            );
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    /**
     * Executes the mouse released operation.
     * @param mouseX value for mouseX
     * @param mouseY value for mouseY
     * @param button value for button
     * @return true when the operation succeeds; otherwise false
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingWidget != null && button == 0) {
            ModConfig.setWidgetPosition(draggingWidget.getId(), draggingWidget.getX(), draggingWidget.getY());
            draggingWidget = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
