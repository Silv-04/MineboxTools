package fr.silv.hud.widget.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * Scrollable list holding one or more arbitrary widgets per row.
 *
 * <p>Used to keep screen widgets from ever being positioned at a fixed pixel
 * offset that can slide off-screen on small windows/high GUI Scale (which can
 * crash Minecraft 26.2's renderer if an off-screen widget's label needs to
 * scroll). Each row repositions its widgets relative to the list's own
 * properly-clamped scroll position every frame, the same way {@link CheckboxListWidget}
 * repositions its checkbox.</p>
 */
public class WidgetRowListWidget extends ContainerObjectSelectionList<WidgetRowListWidget.Entry> {
    private final Font font;

    /**
     * Creates a new WidgetRowListWidget.
     *
     * @param client    active client instance
     * @param x         left position
     * @param y         top position
     * @param width     list width
     * @param height    list height
     * @param rowHeight default row height for single-widget rows
     */
    public WidgetRowListWidget(Minecraft client, int x, int y, int width, int height, int rowHeight) {
        super(client, width, height, y, rowHeight);
        this.font = client.font;
        this.setRectangle(width, height, x, y);
    }

    /**
     * Adds a row containing one or more widgets, laid out left-to-right using
     * each widget's current x as its offset from the row's left edge.
     *
     * @param widgets widgets to place in this row
     */
    public void addRow(AbstractWidget... widgets) {
        addEntry(new Entry(null, 0, 0, 0, widgets));
    }

    /**
     * Adds a row like {@link #addRow(AbstractWidget...)}, plus a text label drawn
     * at a fixed offset from the row's top-left corner. The label supplier is
     * re-evaluated every frame, and drawing is skipped entirely when it returns
     * {@code null} (for conditionally-shown hints).
     *
     * @param label   supplies the label text each frame, or {@code null} to hide it that frame
     * @param labelX  label x offset from the row's left edge
     * @param labelY  label y offset from the row's top edge
     * @param color   label text color
     * @param widgets widgets to place in this row
     */
    public void addRow(Supplier<Component> label, int labelX, int labelY, int color, AbstractWidget... widgets) {
        addEntry(new Entry(label, labelX, labelY, color, widgets));
    }

    /**
     * Adds a blank spacer row purely for vertical spacing between groups of rows.
     *
     * @param height spacer height in pixels
     */
    public void addSpacer(int height) {
        addEntry(new Entry(null, 0, 0, 0), height);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - scrollbarWidth();
    }

    /**
     * Row entry rendering one or more widgets repositioned relative to the row,
     * plus an optional accompanying text label.
     */
    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final List<AbstractWidget> widgets;
        private final int[] relativeX;
        private final Supplier<Component> label;
        private final int labelX;
        private final int labelY;
        private final int labelColor;

        /**
         * Creates one row entry.
         *
         * @param label    supplies the accompanying label text each frame, or {@code null} for none
         * @param labelX   label x offset from the row's left edge
         * @param labelY   label y offset from the row's top edge
         * @param color    label text color
         * @param widgets  widgets to place in this row, positioned by their current x as a relative offset
         */
        public Entry(Supplier<Component> label, int labelX, int labelY, int color, AbstractWidget... widgets) {
            this.widgets = List.of(widgets);
            this.relativeX = new int[widgets.length];
            for (int i = 0; i < widgets.length; i++) {
                relativeX[i] = widgets[i].getX();
            }
            this.label = label;
            this.labelX = labelX;
            this.labelY = labelY;
            this.labelColor = color;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, boolean hovered, float delta) {
            int rowX = getContentX();
            int rowY = getContentY();

            for (int i = 0; i < widgets.size(); i++) {
                AbstractWidget widget = widgets.get(i);
                widget.setPosition(rowX + relativeX[i], rowY);
                widget.extractRenderState(ctx, mouseX, mouseY, delta);
            }

            if (label != null) {
                Component text = label.get();
                if (text != null) {
                    ctx.text(font, text, rowX + labelX, rowY + labelY, labelColor, false);
                }
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return widgets;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return widgets;
        }
    }
}
