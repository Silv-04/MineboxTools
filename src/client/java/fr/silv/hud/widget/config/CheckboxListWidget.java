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
        * @param client active client instance
        * @param x left position
        * @param y top position
        * @param width list width
        * @param height list height
        * @param rowHeight row height for each checkbox entry
     */
    public CheckboxListWidget(MinecraftClient client, int x, int y, int width, int height, int rowHeight) {
        super(client, width, height, y, rowHeight);
        this.textRenderer = client.textRenderer;
        this.setDimensionsAndPosition(width, height, x, y);
    }

    /**
        * Adds a new checkbox option row.
        *
        * @param label label text for the checkbox
        * @param initial initial checked state
        * @param onChange callback invoked when state changes
        * @param icon icon drawn after the label
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
         * Creates one checkbox row entry with an optional icon.
         *
         * @param label label text for the checkbox
         * @param initial initial checked state
         * @param onChange callback invoked on state updates
         * @param tr text renderer used by the checkbox widget
         * @param icon icon texture for this row
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
         * Renders the checkbox row and trailing icon.
         *
         * @param ctx draw context
         * @param index row index
         * @param top top row position
         * @param left left row position
         * @param rowWidth available row width
         * @param rowHeight row height
         * @param mouseX mouse X position
         * @param mouseY mouse Y position
         * @param hovered whether this row is hovered
         * @param delta frame interpolation delta
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
         * Returns interactive child elements for this row.
         *
         * @return row child elements
         */
        public List<? extends Element> children() {
            return List.of(checkbox);
        }

        @Override
        /**
         * Returns selectable elements used for focus/navigation.
         *
         * @return selectable row children
         */
        public List<? extends Selectable> selectableChildren() {
            return List.of(checkbox);
        }
    }

    @Override
    /**
        * Returns the usable row width while reserving scrollbar interaction space.
        *
        * @return row content width
     */
    public int getRowWidth() {
        return Math.max(0, this.width - SCROLLBAR_INTERACTION_MARGIN);
    }

    @Override
    /**
        * Returns the right-side X position where the scrollbar should be drawn.
        *
        * @return scrollbar X coordinate
     */
    protected int getScrollbarX() {
        return this.getRight() - SCROLLBAR_WIDTH;
    }
}
