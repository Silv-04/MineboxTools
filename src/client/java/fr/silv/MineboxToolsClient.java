package fr.silv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.silv.hud.DailyShopTimer;
import fr.silv.items.CustomItemDurabilityHandler;
import fr.silv.items.CustomItemTooltipHandler;
import fr.silv.utils.ItemStatsRangeLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class MineboxToolsClient implements ClientModInitializer {
	private static final Logger MineboxToolsLogger = LogManager.getLogger(MineboxToolsClient.class);

	@Override
	public void onInitializeClient() {
		MineboxToolsLogger.info("[MineboxToolsClient] Initializing client...");
		ItemStatsRangeLoader.load();
		CustomItemDurabilityHandler.register();
		DailyShopTimer.register();
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			CustomItemTooltipHandler.addStatRangesToTooltip(stack, context, type, lines);
		});
	}
}