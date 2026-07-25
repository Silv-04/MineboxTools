package fr.silv.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.silv.Lang;
import fr.silv.api.GuildProfile;
import fr.silv.api.MineboxApiClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Comparator;
import java.util.List;

/**
 * Registers the {@code /mbtguild <guild_name>} client command that fetches and
 * displays a guild profile from the Minebox API in the chat.
 */
@SuppressWarnings("null")
public final class GuildCommand {
    private static final String SEPARATOR = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final int COLOR_GOLD = 0xFFAA00;
    private static final int COLOR_GRAY = 0xAAAAAA;
    private static final int COLOR_WHITE = 0xFFFFFF;
    private static final int COLOR_YELLOW = 0xFFFF55;
    private static final int COLOR_GREEN = 0x55FF55;
    private static final int COLOR_RED = 0xFF5555;

    private GuildCommand() {
    }

    /**
     * Registers the {@code /mbtguild} command on the provided dispatcher.
     *
     * @param dispatcher Brigadier dispatcher used to register client commands
     */
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("mbtguild")
                .then(ClientCommands.argument("guild_name", StringArgumentType.greedyString())
                        .executes(context -> {
                            String guildName = StringArgumentType.getString(context, "guild_name");
                            execute(context.getSource(), guildName.trim());
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    private static void execute(FabricClientCommandSource source, String guildName) {
        if (!MineboxApiClient.isValidGuildName(guildName)) {
            send(source, colored(Lang.get("mineboxtools.command.guild.error.invalid_name"), COLOR_RED));
            return;
        }

        if (!MineboxApiClient.tryAcquireCooldown()) {
            send(source, colored(Lang.get("mineboxtools.command.lookup.error.cooldown"), COLOR_RED));
            return;
        }

        send(source, colored(Lang.get("mineboxtools.command.guild.loading"), COLOR_YELLOW));

        MineboxApiClient.fetchGuildProfile(guildName)
                .thenAcceptAsync(result -> {
                    if (result.isOk()) {
                        displayGuild(source, result.getValue());
                    } else {
                        send(source, errorComponent(result.getError(), guildName));
                    }
                }, Minecraft.getInstance());
    }

    private static void displayGuild(FabricClientCommandSource source, GuildProfile guild) {
        send(source, separator());

        MutableComponent header = colored(guild.name + "  ", COLOR_WHITE)
                .append(colored("Lv. " + guild.level + "  ", COLOR_YELLOW))
                .append(colored("Xp. " + formatNumber(guild.xp), COLOR_YELLOW));
        send(source, header);

        send(source, separator());

        List<GuildProfile.Member> members = guild.members != null ? guild.members : List.of();

        long onlineCount = members.stream().filter(m -> m.online).count();
        String membersLabel = Lang.get("mineboxtools.command.guild.members")
                + " (" + onlineCount + "/" + members.size() + ")";
        send(source, colored(membersLabel, COLOR_YELLOW));

        List<GuildProfile.Member> sorted = members.stream()
                .sorted(Comparator
                        .comparingInt((GuildProfile.Member m) -> m.isOwner ? 0 : 1)
                        .thenComparing(m -> m.online ? 0 : 1)
                        .thenComparing(m -> m.username))
                .toList();

        for (int i = 0; i < sorted.size(); i += 2) {
            MutableComponent left = memberComponent(sorted.get(i));
            if (i + 1 < sorted.size()) {
                send(source, left.append(colored("    ", COLOR_WHITE)).append(memberComponent(sorted.get(i + 1))));
            } else {
                send(source, left);
            }
        }

        send(source, separator());
    }

    private static MutableComponent memberComponent(GuildProfile.Member member) {
        int dotColor = member.online ? COLOR_GREEN : COLOR_RED;
        MutableComponent component = colored("● ", dotColor)
                .append(colored(member.username, COLOR_GRAY));
        if (member.isOwner) {
            component.append(colored(" (" + Lang.get("mineboxtools.command.guild.owner") + ")", COLOR_GOLD));
        }
        return component;
    }

    private static Component errorComponent(MineboxApiClient.LookupError error, String guildName) {
        String key = switch (error) {
            case NOT_FOUND -> "mineboxtools.command.guild.error.not_found";
            // The guild endpoint never returns 401; kept only to satisfy the shared enum's exhaustive switch.
            case API_ACCESS_DISABLED -> "mineboxtools.command.lookup.error.server";
            case RATE_LIMITED -> "mineboxtools.command.lookup.error.rate_limited";
            case SERVER_ERROR -> "mineboxtools.command.lookup.error.server";
            case NETWORK_ERROR -> "mineboxtools.command.lookup.error.network";
            case INVALID_USERNAME -> "mineboxtools.command.guild.error.invalid_name";
        };
        return colored(Lang.get(key).replace("{0}", guildName), COLOR_RED);
    }

    private static MutableComponent separator() {
        return colored(SEPARATOR, COLOR_GOLD);
    }

    private static MutableComponent colored(String text, int rgb) {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }

    private static void send(FabricClientCommandSource source, Component component) {
        source.sendFeedback(component);
    }

    private static String formatNumber(long n) {
        if (n >= 1_000_000)
            return String.format("%.1fm", n / 1_000_000.0);
        if (n >= 1_000)
            return String.format("%.1fk", n / 1_000.0);
        return String.valueOf(n);
    }
}
