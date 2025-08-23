package fr.silv.hud;

import fr.silv.items.CustomItemDurabilityHandler;
import fr.silv.utils.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DisplayAmount {
    private static final Logger DisplayAmountLogger = LogManager.getLogger(DisplayAmount.class);

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.offHandToggle) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden) return;
            ItemStack stack = client.player.getOffHandStack();
            if (stack == null || stack.isEmpty()) return;

            NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (nbtComponent == null) return;

            NbtCompound nbtCompound = nbtComponent.copyNbt();

            if (nbtCompound.getInt("mbitems:display").isPresent() && nbtCompound.getInt("mbitems:display").get() == 1)
                return;

            if (nbtCompound.getString("mbitems:id").isEmpty()) return;
            String id = nbtCompound.getString("mbitems:id").get();

            if (id.contains("haversack")) {
                String[] amountInside = CustomItemDurabilityHandler.getHaverackAmountInside(stack);
                if (amountInside != null) {
                    try {
                        int current = Integer.parseInt(amountInside[0]);
                        int max = Integer.parseInt(amountInside[1]);
                        String text = "Amount inside: " + current + "/" + max;
                        int x = 2;
                        int y = client.getWindow().getScaledHeight() - 12;
                        drawContext.drawTextWithShadow(client.textRenderer, Text.literal(text), x, y, Colors.WHITE);
                    } catch (NumberFormatException e) {
                        DisplayAmountLogger.error("Invalid number format in haversack amount: " + e.getMessage());
                    }
                }
            }
        });
    }
}
