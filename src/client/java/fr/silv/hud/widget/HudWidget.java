package fr.silv.hud.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Base class for movable HUD widgets.
 */
public abstract class HudWidget {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected String id;
    private double relX = -1;
    private double relY = -1;
    private int lastScreenWidth = -1;
    private int lastScreenHeight = -1;

    /**
     * Creates a base HUD widget with initial position and bounds.
     *
     * @param id     widget identifier used for persistence
     * @param x      initial X coordinate in scaled screen space
     * @param y      initial Y coordinate in scaled screen space
     * @param width  initial widget width
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
     * @param client  active client instance
     */
    public abstract void render(GuiGraphicsExtractor context, Minecraft client);

    /**
     * Returns whether the mouse is currently inside widget bounds.
     *
     * @param mouseX current mouse X coordinate
     * @param mouseY current mouse Y coordinate
     * @return {@code true} when the pointer is over the widget
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth()
                && mouseY >= getY() && mouseY <= getY() + getHeight();
    }

    /**
     * Returns the current X coordinate.
     *
     * @return widget X position
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the current Y coordinate.
     *
     * @return widget Y position
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the current widget width.
     *
     * @return widget width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the current widget height.
     *
     * @return widget height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Updates the widget dimensions.
     *
     * @param width  the new widget width
     * @param height the new widget height
     */
    public void setSize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
    }

    /**
     * Keeps the widget inside current screen bounds and anchors it on resize.
     *
     * @param screenWidth  scaled screen width
     * @param screenHeight scaled screen height
     */
    public void keepInBounds(int screenWidth, int screenHeight) {
        int curX = getX();
        int curY = getY();
        int boxWidth = getWidth();
        int boxHeight = getHeight();

        if (relX < 0) {
            relX = screenWidth > 0 ? (double) curX / screenWidth : 0;
            relY = screenHeight > 0 ? (double) curY / screenHeight : 0;
        } else if (lastScreenWidth > 0
                && (lastScreenWidth != screenWidth || lastScreenHeight != screenHeight)) {
            curX = (int) Math.round(relX * screenWidth);
            curY = (int) Math.round(relY * screenHeight);
        }

        int maxX = Math.max(0, screenWidth - boxWidth);
        int maxY = Math.max(0, screenHeight - boxHeight);
        curX = Math.max(0, Math.min(curX, maxX));
        curY = Math.max(0, Math.min(curY, maxY));

        lastScreenWidth = screenWidth;
        lastScreenHeight = screenHeight;

        // Adjust only the displayed position. relX/relY keep the user's intended
        // position so a clamp (screen too small) never permanently drags the widget
        // toward a corner — it returns to place when the screen grows back.
        applyDisplayPosition(curX, curY);
    }

    /**
     * Sets the widget position from its visual top-left corner, as chosen by the user.
     * Records it as the intended relative position used to anchor the widget on resize.
     *
     * @param visualX new X coordinate of the visual top-left corner
     * @param visualY new Y coordinate of the visual top-left corner
     */
    public void setPosition(int visualX, int visualY) {
        applyDisplayPosition(visualX, visualY);
        if (lastScreenWidth > 0) relX = (double) visualX / lastScreenWidth;
        if (lastScreenHeight > 0) relY = (double) visualY / lastScreenHeight;
    }

    /**
     * Applies a visual top-left position for display without changing the intended
     * relative anchor. Subclasses whose stored coordinate differs from the visual
     * corner override {@link #anchorXFromVisual}/{@link #anchorYFromVisual} to convert.
     *
     * @param visualX X coordinate of the visual top-left corner
     * @param visualY Y coordinate of the visual top-left corner
     */
    protected void applyDisplayPosition(int visualX, int visualY) {
        this.x = anchorXFromVisual(visualX);
        this.y = anchorYFromVisual(visualY);
    }

    /**
     * Converts a visual top-left X into the stored X coordinate. Identity by default.
     *
     * @param visualX visual top-left X coordinate
     * @return stored X coordinate
     */
    protected int anchorXFromVisual(int visualX) {
        return visualX;
    }

    /**
     * Converts a visual top-left Y into the stored Y coordinate. Identity by default.
     *
     * @param visualY visual top-left Y coordinate
     * @return stored Y coordinate
     */
    protected int anchorYFromVisual(int visualY) {
        return visualY;
    }

    /**
     * Returns the persistence identifier for this widget.
     *
     * @return widget id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the X coordinate to persist when saving widget position.
     * Subclasses may override to save a logical anchor instead of the raw top-left.
     *
     * @return X coordinate to store in config
     */
    public int getSaveX() {
        return x;
    }

    /**
     * Returns the Y coordinate to persist when saving widget position.
     * Subclasses may override to save a logical anchor instead of the raw top-left.
     *
     * @return Y coordinate to store in config
     */
    public int getSaveY() {
        return y;
    }
}
