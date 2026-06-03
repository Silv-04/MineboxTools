package fr.silv.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import fr.silv.hud.HudMenuScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

/**
 * Registers the client command that opens the MineboxTools menu.
 */
public final class MenuCommand {

    private MenuCommand() {
    }

    /**
     * Registers the {@code /mbt} client command on the provided dispatcher.
     *
     * @param dispatcher Brigadier dispatcher used to register client commands
     */
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("mbt")
                .executes(context -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client != null && client.player != null) {
                        client.execute(() -> client.setScreen(new HudMenuScreen()));
                    }
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
