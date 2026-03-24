package fr.silv.hud.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Base class for movable HUD widgets.
 */
public abstract class HudWidget {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected String id;

    /**
     * Executes the hud widget operation.
     * @param id value for id
     * @param x value for x
     * @param y value for y
     * @param width value for width
     * @param height value for height
     * @return the computed hud widget value
     */
    public HudWidget(String id, int x, int y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Executes the render operation.
     * @param context value for context
     * @param client value for client
     * @return the computed render value
     */
    public abstract void render(DrawContext context, MinecraftClient client);

    /**
     * Checks whether mouse over.
     * @param mouseX value for mouseX
     * @param mouseY value for mouseY
     * @return true if the condition is met; otherwise false
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    /**
     * Returns the x.
     * @return the x
     */
    public int getX() { return x; }
    /**
     * Returns the y.
     * @return the y
     */
    public int getY() { return y; }
    /**
     * Returns the width.
     * @return the width
     */
    public int getWidth() { return width; }
    /**
     * Returns the height.
     * @return the height
     */
    public int getHeight() { return height; }

    /**
     * Updates the widget dimensions.
     * @param width the new widget width
     * @param height the new widget height
     */
    public void setSize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
    }

    /**
     * Keeps the widget inside current screen bounds.
     * @param screenWidth scaled screen width
     * @param screenHeight scaled screen height
     */
    public void keepInBounds(int screenWidth, int screenHeight) {
        int maxX = Math.max(0, screenWidth - width);
        int maxY = Math.max(0, screenHeight - height);
        x = Math.max(0, Math.min(x, maxX));
        y = Math.max(0, Math.min(y, maxY));
    }
    /**
     * Updates the position.
     * @param x value for x
     * @param y value for y
     */
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    /**
     * Returns the id.
     * @return the id
     */
    public String getId() { return id; }
}
