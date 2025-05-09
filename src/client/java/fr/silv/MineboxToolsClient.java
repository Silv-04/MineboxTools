package fr.silv;

import fr.silv.items.CustomItemDurabilityHandler;
import fr.silv.items.CustomItemTooltipHandler;
import fr.silv.utils.ItemStatsRangeLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class MineboxToolsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		System.out.println("[MineboxToolsClient] Initializing client...");
		ItemStatsRangeLoader.load();
		CustomItemDurabilityHandler.register();
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			CustomItemTooltipHandler.addStatRangesToTooltip(stack, context, type, lines);
		});
	}
}