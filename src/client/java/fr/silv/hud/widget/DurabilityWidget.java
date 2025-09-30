package fr.silv.hud.widget;

import fr.silv.items.CustomItemDurabilityHandler;
import fr.silv.ModConfig;
import fr.silv.utils.ItemStatUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class DurabilityWidget extends HudWidget {
    private static final Logger DisplayAmountLogger = LogManager.getLogger(DurabilityWidget.class);

    public DurabilityWidget() {
        super("durability_widget",
                ModConfig.getWidgetPosition("durability_widget")[0],
                ModConfig.getWidgetPosition("durability_widget")[1],
                160, 30);
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        if (!ModConfig.handToggle) return;
        if (client.options.hudHidden) return;

        try {
            ItemStack offHandStack = client.player.getOffHandStack();
            String offHandDurability = getOffHandDurability(offHandStack);
            ItemStack mainHandStack = client.player.getMainHandStack();
            String mainHandDurability = getMainHandDurability(mainHandStack);

            TextRenderer textRenderer = client.textRenderer;
            if (!mainHandDurability.equals("")) {
                context.drawItem(mainHandStack, this.x, this.y);
                context.drawTextWithShadow(textRenderer, Text.literal(mainHandDurability), this.x+18, this.y+4, Colors.WHITE);
                if (!offHandDurability.equals("")) {
                    context.drawItem(offHandStack, this.x, this.y + 16);
                    context.drawTextWithShadow(textRenderer, Text.literal(offHandDurability), this.x+18, this.y + 20, Colors.WHITE);
                }
            }
            else {
                context.drawItem(offHandStack, this.x, this.y);
                context.drawTextWithShadow(textRenderer, Text.literal(offHandDurability), this.x+18, this.y+4, Colors.WHITE);
            }
        }
        catch (NullPointerException npe) {
            DisplayAmountLogger.error("Null pointer exception in DurabilityWidget: " + npe.getMessage());
            return;
        }
    }

    private String getMainHandDurability(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return "";

        NbtCompound nbtCompound = nbtComponent.copyNbt();

        if (nbtCompound.getString("mbitems:id").isEmpty()) return "";
        String id = nbtCompound.getString("mbitems:id").get();

        if (nbtCompound.getCompound("mbitems:persistent").isEmpty()) return "";
        NbtCompound persistent = nbtCompound.getCompound("mbitems:persistent").get();

        if (persistent.getInt("mbitems:durability").isEmpty()) return "";
        String mainHandCurrent = String.valueOf(persistent.getInt("mbitems:durability").get());
        String mainHandMax = String.valueOf(ItemStatUtils.getStatsFor(id).get("mbx.durability")[0]);
        return mainHandCurrent + "/" + mainHandMax;
    }

    private String getOffHandDurability(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return "";

        NbtCompound nbtCompound = nbtComponent.copyNbt();

        if (nbtCompound.getString("mbitems:id").isEmpty()) return "";
        String id = nbtCompound.getString("mbitems:id").get();

        if (id.contains("haversack")) {
            String[] amountInside = CustomItemDurabilityHandler.getHaverackAmountInside(stack);
            if (amountInside != null) {
                try {
                    int current = Integer.parseInt(amountInside[0]);
                    int max = Integer.parseInt(amountInside[1]);
                    String text = current + "/" + max;
                    return text;
                } catch (NumberFormatException e) {
                    DisplayAmountLogger.error("Invalid number format in haversack amount: " + e.getMessage());
                }
            }
        }
        return "";
    }
}
