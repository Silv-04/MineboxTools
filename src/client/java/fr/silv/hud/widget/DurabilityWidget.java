package fr.silv.hud.widget;

import fr.silv.items.DurabilityBarHandler;
import fr.silv.ModConfig;
import fr.silv.utils.MineboxItemDataUtils;
import fr.silv.utils.ModLog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * HUD widget that displays tool durability information.
 */
public class DurabilityWidget extends HudWidget {
    private static final Logger LOGGER = ModLog.getLogger(DurabilityWidget.class);

    /**
     * Creates a new DurabilityWidget instance.
     */
    public DurabilityWidget() {
        super("durability_widget",
                ModConfig.getWidgetPosition("durability_widget")[0],
                ModConfig.getWidgetPosition("durability_widget")[1],
                160, 30);
    }

    @Override
    /**
        * Renders durability values for main-hand and off-hand items when available.
        *
        * @param context draw context
        * @param client active client instance
     */
    public void render(DrawContext context, MinecraftClient client) {
        if (!ModConfig.isEnabled(ModConfig.FeatureFlag.HAND)) return;
        if (client.options.hudHidden) return;

        if (client.player == null) {
            ModLog.warnThrottled(LOGGER, "durability-widget:null-player", 10_000,
                    "DurabilityWidget render skipped because client state was incomplete");
            return;
        }

        ItemStack offHandStack = client.player.getOffHandStack();
        String offHandDurability = getOffHandDurability(offHandStack);
        ItemStack mainHandStack = client.player.getMainHandStack();
        String mainHandDurability = getMainHandDurability(mainHandStack);

        TextRenderer textRenderer = client.textRenderer;
        if (!mainHandDurability.isEmpty()) {
            context.drawItem(mainHandStack, this.x, this.y);
            context.drawTextWithShadow(textRenderer, Text.literal(mainHandDurability), this.x + 18, this.y + 4, Colors.WHITE);
            if (!offHandDurability.isEmpty()) {
                context.drawItem(offHandStack, this.x, this.y + 16);
                context.drawTextWithShadow(textRenderer, Text.literal(offHandDurability), this.x + 18, this.y + 20, Colors.WHITE);
            }
        } else if (!offHandDurability.isEmpty()) {
            context.drawItem(offHandStack, this.x, this.y);
            context.drawTextWithShadow(textRenderer, Text.literal(offHandDurability), this.x + 18, this.y + 4, Colors.WHITE);
        }
    }

    private String getMainHandDurability(ItemStack stack) {
        Optional<MineboxItemDataUtils.PersistentItemData> itemDataOptional = MineboxItemDataUtils.getPersistentItemData(stack);
        if (itemDataOptional.isEmpty()) return "";

        MineboxItemDataUtils.PersistentItemData itemData = itemDataOptional.get();
        Optional<Integer> currentOptional = MineboxItemDataUtils.getCurrentDurability(itemData.persistentData());
        Optional<Integer> maxOptional = MineboxItemDataUtils.getMaxDurability(itemData.itemId());
        if (currentOptional.isEmpty() || maxOptional.isEmpty()) return "";

        return currentOptional.get() + "/" + maxOptional.get();
    }

    private String getOffHandDurability(ItemStack stack) {
        Optional<String> idOptional = MineboxItemDataUtils.getItemId(stack);
        if (idOptional.isEmpty()) return "";
        String id = idOptional.get();

        if (id.contains("haversack")) {
            String[] amountInside = DurabilityBarHandler.getHaverackAmountInside(stack);
            if (amountInside != null) {
                try {
                    int current = Integer.parseInt(amountInside[0]);
                    int max = Integer.parseInt(amountInside[1]);
                    String text = current + "/" + max;
                    return text;
                } catch (NumberFormatException e) {
                    ModLog.warnThrottled(LOGGER, "durability-widget:invalid-haversack", 10_000,
                            "Invalid haversack amount {}", String.join("/", amountInside), e);
                }
            }
        }
        return "";
    }
}
