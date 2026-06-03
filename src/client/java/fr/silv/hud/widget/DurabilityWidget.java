package fr.silv.hud.widget;

import fr.silv.ModConfig;
import fr.silv.items.DurabilityBarHandler;
import fr.silv.utils.MineboxItemDataUtils;
import fr.silv.utils.ModLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * HUD widget that displays tool durability information.
 */
public class DurabilityWidget extends HudWidget {
    private static final Logger LOGGER = ModLog.getLogger(DurabilityWidget.class);
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    /**
     * Creates a new DurabilityWidget with persisted screen position.
     */
    public DurabilityWidget() {
        super("durability_widget",
                ModConfig.getWidgetPosition("durability_widget")[0],
                ModConfig.getWidgetPosition("durability_widget")[1],
                160, 30);
    }

    /**
     * Renders durability values for main-hand and off-hand items when available.
     *
     * @param context draw context
     * @param client  active client instance
     */
    @Override
    public void render(GuiGraphicsExtractor context, Minecraft client) {
        if (!ModConfig.isEnabled(ModConfig.FeatureFlag.HAND)) return;
        if (client.options.hideGui) return;

        if (client.player == null) {
            ModLog.warnThrottled(LOGGER, "durability-widget:null-player", 10_000,
                    "DurabilityWidget render skipped because client state was incomplete");
            return;
        }

        ItemStack offHandStack = client.player.getOffhandItem();
        String offHandDurability = getOffHandDurability(offHandStack);
        ItemStack mainHandStack = client.player.getMainHandItem();
        String mainHandDurability = getMainHandDurability(mainHandStack);

        Font font = client.font;
        if (!mainHandDurability.isEmpty()) {
            context.item(mainHandStack, this.x, this.y);
            context.text(font, Component.literal(mainHandDurability), this.x + 18, this.y + 4, TEXT_COLOR);
            if (!offHandDurability.isEmpty()) {
                context.item(offHandStack, this.x, this.y + 16);
                context.text(font, Component.literal(offHandDurability), this.x + 18, this.y + 20, TEXT_COLOR);
            }
        } else if (!offHandDurability.isEmpty()) {
            context.item(offHandStack, this.x, this.y);
            context.text(font, Component.literal(offHandDurability), this.x + 18, this.y + 4, TEXT_COLOR);
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
                    return current + "/" + max;
                } catch (NumberFormatException e) {
                    ModLog.warnThrottled(LOGGER, "durability-widget:invalid-haversack", 10_000,
                            "Invalid haversack amount {}", String.join("/", amountInside), e);
                }
            }
        }
        return "";
    }
}
