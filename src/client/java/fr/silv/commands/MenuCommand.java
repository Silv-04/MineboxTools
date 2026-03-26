package fr.silv.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import fr.silv.hud.HudMenuScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

/**
 * Registers the client command that opens the MineboxTools menu.
 */
public class MenuCommand {
    /**
     * Registers command handlers. -> /mbt
        *
        * @param dispatcher Brigadier dispatcher used to register client commands
     */
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("mbt")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.player != null) {
                        client.send(() -> {
                            client.setScreenAndRender(new HudMenuScreen());
                        });
                    }
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
