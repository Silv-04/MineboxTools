package fr.silv;

import fr.silv.commands.MenuCommand;
import fr.silv.hud.widget.HudWidgetManager;
import fr.silv.utils.MineboxItemUtils;
import fr.silv.utils.ModLog;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import fr.silv.items.DurabilityBarHandler;
import fr.silv.items.TooltipHandler;
import fr.silv.utils.MineboxItemStatUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

/**
 * Client entry point for MineboxTools initialization and registrations.
 */
public class MineboxToolsClient implements ClientModInitializer {
	private static final Logger MineboxToolsLogger = ModLog.getLogger(MineboxToolsClient.class);

	@Override
	/**
	 * Initializes this mod component.
	 */
	public void onInitializeClient() {
		MineboxToolsLogger.info("[MineboxToolsClient] Initializing client...");

		ModConfig.load();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			MenuCommand.register(dispatcher);
		});
		MineboxItemStatUtils.load();
		MineboxItemUtils.load();
		DurabilityBarHandler.register();

		ItemTooltipCallback.EVENT.register(TooltipHandler::addStatRangesToTooltip);
		ItemTooltipCallback.EVENT.register(TooltipHandler::addInfoToTooltip);

		HudWidgetManager.init();
		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, Identifier.of("mineboxtools", "widgets"), (context, tickCounter) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			int screenWidth = client.getWindow().getScaledWidth();
			int screenHeight = client.getWindow().getScaledHeight();
			for (var widget : HudWidgetManager.getWidgets()) {
				widget.keepInBounds(screenWidth, screenHeight);
				widget.render(context, client);
			}
		});
	}
}
