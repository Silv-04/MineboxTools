package fr.silv;

import fr.silv.commands.MenuCommand;
import fr.silv.hud.DisplayAmount;
import fr.silv.hud.DisplayStats;
import fr.silv.utils.ModConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.silv.hud.DisplayIcons;
import fr.silv.items.CustomItemDurabilityHandler;
import fr.silv.items.CustomItemTooltipHandler;
import fr.silv.utils.ItemStatUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class MineboxToolsClient implements ClientModInitializer {
	private static final Logger MineboxToolsLogger = LogManager.getLogger(MineboxToolsClient.class);

	@Override
	public void onInitializeClient() {
		MineboxToolsLogger.info("[MineboxToolsClient] Initializing client...");

		ModConfig.load();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			MenuCommand.register(dispatcher);
		});
		ItemStatUtils.load();
		CustomItemDurabilityHandler.register();
		DisplayIcons.register();
		DisplayStats.register();
		DisplayAmount.register();
		ItemTooltipCallback.EVENT.register(CustomItemTooltipHandler::addStatRangesToTooltip);

	}
}