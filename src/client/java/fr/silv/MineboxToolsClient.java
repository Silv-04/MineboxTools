package fr.silv;

import fr.silv.api.MuseumDonationCache;
import fr.silv.api.MuseumScreenRegistry;
import fr.silv.commands.EffectsCommand;
import fr.silv.commands.GuildCommand;
import fr.silv.commands.LevelCommand;
import fr.silv.commands.LookupCommand;
import fr.silv.commands.MenuCommand;
import fr.silv.effects.EffectCatalogService;
import fr.silv.effects.EffectScanController;
import fr.silv.hud.widget.HudWidgetManager;
import fr.silv.items.DurabilityBarHandler;
import fr.silv.items.TooltipHandler;
import fr.silv.utils.MineboxItemStatUtils;
import fr.silv.utils.MineboxItemUtils;
import fr.silv.utils.MuseumItemUtils;
import fr.silv.utils.SkillLevelUtils;
import fr.silv.utils.ModLog;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
            GuildCommand.register(dispatcher);
            LevelCommand.register(dispatcher);
            EffectsCommand.register(dispatcher);
        });

        MineboxItemStatUtils.load();
        MineboxItemUtils.load();
        MuseumItemUtils.load();
        SkillLevelUtils.load();
        DurabilityBarHandler.register();

        EffectCatalogService.init();
        ClientTickEvents.END_CLIENT_TICK.register(EffectScanController::onClientTick);

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            TooltipHandler.addStatRangesToTooltip(stack, context, type, lines);
            TooltipHandler.addInfoToTooltip(stack, context, type, lines);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                MuseumDonationCache.refresh());

        // Seed the effects widget on join: effects already running (from before login) fire no
        // consume event, so scan once the world is up.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                EffectScanController.requestScan());

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                ScreenEvents.remove(screen).register(closedScreen -> {
                    int containerId = containerScreen.getMenu().containerId;
                    if (MuseumScreenRegistry.consumeAndCheck(containerId)) {
                        // Donations can take a moment to land server-side, so refresh is delayed
                        // rather than fired immediately on close.
                        CompletableFuture.runAsync(MuseumDonationCache::refresh,
                                CompletableFuture.delayedExecutor(60, TimeUnit.SECONDS));
                    }
                });
            }
        });

        HudWidgetManager.init();
    }
}
