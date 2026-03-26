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
    private int lastScreenWidth = -1;
    private int lastScreenHeight = -1;

    /**
     * Creates a base HUD widget with initial position and bounds.
     *
     * @param id widget identifier used for persistence
     * @param x initial X coordinate in scaled screen space
     * @param y initial Y coordinate in scaled screen space
     * @param width initial widget width
     * @param height initial widget height
     */
    public HudWidget(String id, int x, int y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Renders the widget for the current HUD frame.
     *
     * @param context draw context used for rendering
     * @param client active client instance
     */
    public abstract void render(DrawContext context, MinecraftClient client);

    /**
     * Returns whether the mouse is currently inside widget bounds.
     *
     * @param mouseX current mouse X coordinate
     * @param mouseY current mouse Y coordinate
     * @return {@code true} when the pointer is over the widget
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    /**
     * Returns the current X coordinate.
     *
     * @return widget X position
     */
    public int getX() { return x; }

    /**
     * Returns the current Y coordinate.
     *
     * @return widget Y position
     */
    public int getY() { return y; }

    /**
     * Returns the current widget width.
     *
     * @return widget width in pixels
     */
    public int getWidth() { return width; }

    /**
     * Returns the current widget height.
     *
     * @return widget height in pixels
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
        if (lastScreenWidth > 0 && lastScreenHeight > 0
                && (lastScreenWidth != screenWidth || lastScreenHeight != screenHeight)) {
            int previousLeftMargin = x;
            int previousRightMargin = Math.max(0, lastScreenWidth - (x + width));
            int previousTopMargin = y;
            int previousBottomMargin = Math.max(0, lastScreenHeight - (y + height));

            boolean anchorRight = previousRightMargin < previousLeftMargin;
            boolean anchorBottom = previousBottomMargin < previousTopMargin;

            int deltaWidth = screenWidth - lastScreenWidth;
            int deltaHeight = screenHeight - lastScreenHeight;

            if (anchorRight) {
                x += deltaWidth;
            }
            if (anchorBottom) {
                y += deltaHeight;
            }
        }

        int maxX = Math.max(0, screenWidth - width);
        int maxY = Math.max(0, screenHeight - height);
        x = Math.max(0, Math.min(x, maxX));
        y = Math.max(0, Math.min(y, maxY));

        lastScreenWidth = screenWidth;
        lastScreenHeight = screenHeight;
    }
    /**
     * Sets the widget position directly.
     *
     * @param x new X coordinate
     * @param y new Y coordinate
     */
    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    /**
     * Returns the persistence identifier for this widget.
     *
     * @return widget id
     */
    public String getId() { return id; }
}
