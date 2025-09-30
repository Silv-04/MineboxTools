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

public class CheckboxListWidget extends ElementListWidget<CheckboxListWidget.Entry> {
    private final TextRenderer textRenderer;

    public CheckboxListWidget(MinecraftClient client, int x, int y, int width, int height, int rowHeight) {
        super(client, width, height, y, rowHeight);
        this.textRenderer = client.textRenderer;
        this.setDimensionsAndPosition(width, height, x, y);
    }

    public void addOption(Text label, boolean initial, Consumer<Boolean> onChange, Identifier icon) {
        this.addEntry(new Entry(label, initial, onChange, textRenderer, icon));
    }

    public static class Entry extends ElementListWidget.Entry<Entry> {
        private final CheckboxWidget checkbox;
        private final Identifier icon;

        public Entry(Text label, boolean initial, Consumer<Boolean> onChange, TextRenderer tr, Identifier icon) {
            this.icon = icon;
            this.checkbox = CheckboxWidget.builder(label, tr)
                    .checked(initial)
                    .callback((cb, checked) -> onChange.accept(checked))
                    .pos(5, 5)
                    .build();
        }

        @Override
        public void render(DrawContext ctx, int index, int top, int left, int rowWidth, int rowHeight,
                           int mouseX, int mouseY, boolean hovered, float delta) {
            int x = left + 2;
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
        public List<? extends Element> children() {
            return List.of(checkbox);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(checkbox);
        }
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }
}
