package fr.silv;

import fr.silv.commands.LookupCommand;
import fr.silv.commands.MenuCommand;
import fr.silv.hud.widget.HudWidgetManager;
import fr.silv.items.DurabilityBarHandler;
import fr.silv.items.ItemHighlightHandler;
import fr.silv.items.TooltipHandler;
import fr.silv.utils.MineboxItemStatUtils;
import fr.silv.utils.MineboxItemUtils;
import fr.silv.utils.SkillLevelUtils;
import fr.silv.utils.ModLog;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * Client entry point for MineboxTools initialization and registrations.
 */
public class MineboxToolsClient implements ClientModInitializer {
    private static final Logger LOGGER = ModLog.getLogger(MineboxToolsClient.class);

    /**
     * Performs client-side mod initialization: loads data, registers commands,
     * tooltip callbacks, and HUD widgets.
     */
    @Override
    public void onInitializeClient() {
        LOGGER.info("[MineboxToolsClient] Initializing client...");

        ModConfig.load();

        ClientLifecycleEvents.CLIENT_STARTED.register(client ->
                Lang.load(ModConfig.getLanguage()));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            MenuCommand.register(dispatcher);
            LookupCommand.register(dispatcher);
        });

        MineboxItemStatUtils.load();
        MineboxItemUtils.load();
        SkillLevelUtils.load();
        DurabilityBarHandler.register();

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            TooltipHandler.addStatRangesToTooltip(stack, context, type, lines);
            TooltipHandler.addInfoToTooltip(stack, context, type, lines);
        });

        HudWidgetManager.init();
        HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES,
                Identifier.fromNamespaceAndPath("mineboxtools", "widgets"),
                (context, tickCounter) -> {
                    Minecraft client = Minecraft.getInstance();
                    int screenWidth = client.getWindow().getGuiScaledWidth();
                    int screenHeight = client.getWindow().getGuiScaledHeight();
                    for (var widget : HudWidgetManager.getWidgets()) {
                        widget.keepInBounds(screenWidth, screenHeight);
                        widget.render(context, client);
                    }
                    ItemHighlightHandler.renderHotbar(client, context);
                });
    }
}
