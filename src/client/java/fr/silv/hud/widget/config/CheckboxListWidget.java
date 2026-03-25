package fr.silv.hud.widget.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Consumer;

/**
 * Scrollable checkbox list widget used by config screens.
 */
public class CheckboxListWidget extends ElementListWidget<CheckboxListWidget.Entry> {
    private static final int SCROLLBAR_INTERACTION_MARGIN = 24;
    private static final int SCROLLBAR_WIDTH = 8;
    private final TextRenderer textRenderer;

    /**
     * Creates a new CheckboxListWidget instance.
     * 
     * @param client    value for client
     * @param x         value for x
     * @param y         value for y
     * @param width     value for width
     * @param height    value for height
     * @param rowHeight value for rowHeight
     */
    public CheckboxListWidget(MinecraftClient client, int x, int y, int width, int height, int rowHeight) {
        super(client, width, height, y, rowHeight);
        this.textRenderer = client.textRenderer;
        this.setDimensionsAndPosition(width, height, x, y);
    }

    /**
     * Adds option.
     * 
     * @param label    value for label
     * @param initial  value for initial
     * @param onChange value for onChange
     * @param icon     value for icon
     */
    public void addOption(Text label, boolean initial, Consumer<Boolean> onChange, Identifier icon) {
        this.addEntry(new Entry(label, initial, onChange, textRenderer, icon));
    }

    /**
     * Clears all current options from the list.
     */
    public void clearOptions() {
        this.clearEntries();
    }

    public static class Entry extends ElementListWidget.Entry<Entry> {
        private final CheckboxWidget checkbox;
        private final Identifier icon;

        /**
         * Executes the entry operation.
         * 
         * @param label    value for label
         * @param initial  value for initial
         * @param onChange value for onChange
         * @param tr       value for tr
         * @param icon     value for icon
         * @return the computed entry value
         */
        public Entry(Text label, boolean initial, Consumer<Boolean> onChange, TextRenderer tr, Identifier icon) {
            this.icon = icon;
            this.checkbox = CheckboxWidget.builder(label, tr)
                    .checked(initial)
                    .callback((cb, checked) -> onChange.accept(checked))
                    .pos(5, 5)
                    .build();
        }

        @Override
        /**
         * Executes the render operation.
         * 
         * @param ctx       value for ctx
         * @param index     value for index
         * @param top       value for top
         * @param left      value for left
         * @param rowWidth  value for rowWidth
         * @param rowHeight value for rowHeight
         * @param mouseX    value for mouseX
         * @param mouseY    value for mouseY
         * @param hovered   value for hovered
         * @param delta     value for delta
         */
        public void render(DrawContext ctx, int index, int top, int left, int rowWidth, int rowHeight,
                int mouseX, int mouseY, boolean hovered, float delta) {
            int x = left - 12;
            int y = top + (rowHeight - checkbox.getHeight()) / 2;
            checkbox.setPosition(x, y);
            checkbox.render(ctx, mouseX, mouseY, delta);

            int textWidth = checkbox.getMessage().getString().isEmpty()
                    ? 0
                    : MinecraftClient.getInstance().textRenderer.getWidth(checkbox.getMessage());

            int iconX = checkbox.getX() + 14 + textWidth + 10;
            int iconY = y;
            ctx.drawTexture(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, 0, 0, 16, 16, 16, 16);
        }

        @Override
        /**
         * Executes the children operation.
         * 
         * @return the computed children value
         */
        public List<? extends Element> children() {
            return List.of(checkbox);
        }

        @Override
        /**
         * Executes the selectable children operation.
         * 
         * @return the computed selectable children value
         */
        public List<? extends Selectable> selectableChildren() {
            return List.of(checkbox);
        }
    }

    @Override
    /**
     * Returns the row width.
     * 
     * @return the row width
     */
    public int getRowWidth() {
        return Math.max(0, this.width - SCROLLBAR_INTERACTION_MARGIN);
    }

    @Override
    /**
     * Returns the scrollbar x position.
     * 
     * @return the scrollbar x position
     */
    protected int getScrollbarX() {
        return this.getRight() - SCROLLBAR_WIDTH;
    }
}
