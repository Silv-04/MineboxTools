package fr.silv;

import fr.silv.commands.MenuCommand;
import fr.silv.hud.widget.DurabilityWidget;
import fr.silv.hud.widget.HudWidgetManager;
import fr.silv.hud.widget.StatWidget;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.silv.hud.widget.IconWidget;
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

		ItemTooltipCallback.EVENT.register(CustomItemTooltipHandler::addStatRangesToTooltip);

		HudWidgetManager.init();
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			for (var widget : HudWidgetManager.getWidgets()) {
				widget.render(context, client);
			}
		});
	}
}