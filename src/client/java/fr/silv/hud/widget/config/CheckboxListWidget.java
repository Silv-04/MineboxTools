package fr.silv.hud.widget.config;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;

/**
 * Scrollable checkbox list widget used by config screens.
 */
public class CheckboxListWidget extends ContainerObjectSelectionList<CheckboxListWidget.Entry> {
    private static final int SCROLLBAR_INTERACTION_MARGIN = 24;
    private static final int SCROLLBAR_WIDTH = 8;
    private final Font font;

    /**
     * Creates a new CheckboxListWidget.
     *
     * @param client    active client instance
     * @param x         left position
     * @param y         top position
     * @param width     list width
     * @param height    list height
     * @param rowHeight row height for each checkbox entry
     */
    public CheckboxListWidget(Minecraft client, int x, int y, int width, int height, int rowHeight) {
        super(client, width, height, y, rowHeight);
        this.font = client.font;
        this.setRectangle(width, height, x, y);
    }

    /**
     * Adds a new checkbox option row.
     *
     * @param label    label text for the checkbox
     * @param initial  initial checked state
     * @param onChange callback invoked when state changes
     * @param icon     icon drawn after the label
     */
    public void addOption(Component label, boolean initial, Consumer<Boolean> onChange, Identifier icon) {
        this.addEntry(new Entry(label, initial, onChange, font, icon));
    }

    /**
     * Clears all current options from the list.
     */
    public void clearOptions() {
        this.clearEntries();
    }

    @Override
    public int getRowWidth() {
        return Math.max(0, this.width - SCROLLBAR_INTERACTION_MARGIN);
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - SCROLLBAR_WIDTH;
    }

    /**
     * Row entry rendering a checkbox followed by an optional icon.
     */
    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private static final RenderPipeline ICON_PIPELINE = RenderPipelines.GUI_TEXTURED;
        private final Checkbox checkbox;
        private final Identifier icon;

        /**
         * Creates one checkbox row entry with an optional icon.
         *
         * @param label    label text for the checkbox
         * @param initial  initial checked state
         * @param onChange callback invoked on state updates
         * @param font     font used by the checkbox widget
         * @param icon     icon texture for this row
         */
        public Entry(Component label, boolean initial, Consumer<Boolean> onChange, Font font, Identifier icon) {
            this.icon = icon;
            this.checkbox = Checkbox.builder(label, font)
                    .selected(initial)
                    .onValueChange((cb, checked) -> onChange.accept(checked))
                    .pos(0, 0)
                    .build();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, boolean hovered, float delta) {
            int rowX = getContentX();
            int rowY = getContentY();
            int rowHeight = getContentHeight();
            int checkboxY = rowY + (rowHeight - checkbox.getHeight()) / 2;
            checkbox.setPosition(rowX, checkboxY);
            checkbox.extractRenderState(ctx, mouseX, mouseY, delta);

            int textWidth = checkbox.getMessage().getString().isEmpty()
                    ? 0
                    : Minecraft.getInstance().font.width(checkbox.getMessage());

            int iconX = checkbox.getX() + 14 + textWidth + 10;
            ctx.blit(ICON_PIPELINE, icon, iconX, checkboxY, 0f, 0f, 16, 16, 16, 16);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(checkbox);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(checkbox);
        }
    }
}
