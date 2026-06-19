package fr.silv.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.silv.Lang;
import fr.silv.api.MineboxApiClient;
import fr.silv.api.PlayerProfile;
import fr.silv.utils.SkillLevelUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Map;

/**
 * Registers the {@code /mbtlevel <username> <skill> <level>} client command.
 * Fetches the player's current skill XP from the API and prints how much XP
 * is still needed to reach the requested skill level.
 */
@SuppressWarnings("null")
public final class LevelCommand {
    private static final String SEPARATOR = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final int COLOR_GOLD   = 0xFFAA00;
    private static final int COLOR_GRAY   = 0xAAAAAA;
    private static final int COLOR_WHITE  = 0xFFFFFF;
    private static final int COLOR_YELLOW = 0xFFFF55;
    private static final int COLOR_GREEN  = 0x55FF55;
    private static final int COLOR_RED    = 0xFF5555;


    private LevelCommand() {
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("mbtlevel")
                .then(ClientCommands.argument("username", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            var connection = Minecraft.getInstance().getConnection();
                            if (connection == null) return builder.buildFuture();
                            return SharedSuggestionProvider.suggest(
                                    connection.getOnlinePlayers().stream()
                                            .map(info -> info.getProfile().name()),
                                    builder);
                        })
                        .then(ClientCommands.argument("skill", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (String id : SkillLevelUtils.getSortedSkillIds()) {
                                        builder.suggest(Lang.get("mineboxtools.skill." + id));
                                    }
                                    return builder.buildFuture();
                                })
                                .then(ClientCommands.argument("level", StringArgumentType.word())
                                        .executes(context -> {
                                            String username = StringArgumentType.getString(context, "username");
                                            String skillId  = StringArgumentType.getString(context, "skill");
                                            String levelStr = StringArgumentType.getString(context, "level");
                                            execute(context.getSource(), username, skillId, levelStr);
                                            return Command.SINGLE_SUCCESS;
                                        })))));
    }

    private static void execute(FabricClientCommandSource source, String username, String skillId, String levelStr) {
        if (!MineboxApiClient.isValidUsername(username)) {
            send(source, error(Lang.get("mineboxtools.command.lookup.error.invalid_username")));
            return;
        }

        int targetLevel;
        try {
            targetLevel = Integer.parseInt(levelStr);
        } catch (NumberFormatException e) {
            send(source, error(Lang.get("mineboxtools.command.level.error.invalid_level")));
            return;
        }

        String skillKey = SkillLevelUtils.resolveSkillId(skillId);
        if (skillKey == null) {
            send(source, error(Lang.get("mineboxtools.command.level.error.unknown_skill")
                    .replace("{0}", skillId)));
            return;
        }
        int maxLevel = SkillLevelUtils.getMaxLevel(skillKey);

        if (targetLevel < 2 || targetLevel > maxLevel) {
            send(source, error(Lang.get("mineboxtools.command.level.error.level_out_of_range")
                    .replace("{0}", String.valueOf(maxLevel))));
            return;
        }

        if (!MineboxApiClient.tryAcquireCooldown()) {
            send(source, error(Lang.get("mineboxtools.command.lookup.error.cooldown")));
            return;
        }

        send(source, colored(Lang.get("mineboxtools.command.lookup.loading"), COLOR_YELLOW));

        MineboxApiClient.fetchPlayerProfile(username)
                .thenAcceptAsync(result -> {
                    if (result.isOk()) {
                        displayResult(source, result.getValue(), skillKey, targetLevel);
                    } else {
                        send(source, apiError(result.getError(), username));
                    }
                }, Minecraft.getInstance());
    }

    private static void displayResult(FabricClientCommandSource source, PlayerProfile profile, String skillId, int targetLevel) {
        Map<String, Long> apiSkills = (profile.data != null && profile.data.skills != null && profile.data.skills.data != null)
                ? profile.data.skills.data : Map.of();

        Long rawXp = apiSkills.get(skillId.toUpperCase());
        long currentXp = rawXp != null ? rawXp : 0L;
        int currentLevel = SkillLevelUtils.expToLevel(skillId, currentXp);

        if (targetLevel <= currentLevel) {
            send(source, error(Lang.get("mineboxtools.command.level.error.already_reached")
                    .replace("{0}", String.valueOf(currentLevel))));
            return;
        }

        long requiredXp = SkillLevelUtils.levelToMinXp(skillId, targetLevel);
        long xpNeeded = requiredXp - currentXp;

        String skillName = Lang.get("mineboxtools.skill." + skillId);

        send(source, colored(SEPARATOR, COLOR_GOLD));

        MutableComponent header = colored(profile.username + "  ", COLOR_WHITE)
                .append(colored(skillName + "  ", COLOR_YELLOW))
                .append(colored("Lv." + currentLevel, COLOR_WHITE))
                .append(colored(" → ", COLOR_GRAY))
                .append(colored("Lv." + targetLevel, COLOR_GREEN));
        send(source, header);

        send(source, colored(SEPARATOR, COLOR_GOLD));

        send(source, labelValue(Lang.get("mineboxtools.command.level.current_xp"), formatXp(currentXp)));
        send(source, labelValue(Lang.get("mineboxtools.command.level.required_xp"), formatXp(requiredXp)));
        send(source, labelValue(Lang.get("mineboxtools.command.level.xp_needed"),   formatXp(xpNeeded)));

        send(source, colored(SEPARATOR, COLOR_GOLD));
    }

    private static Component apiError(MineboxApiClient.LookupError lookupError, String username) {
        String key = switch (lookupError) {
            case NOT_FOUND        -> "mineboxtools.command.lookup.error.not_found";
            case PROFILE_PRIVATE  -> "mineboxtools.command.lookup.error.private";
            case RATE_LIMITED     -> "mineboxtools.command.lookup.error.rate_limited";
            case SERVER_ERROR     -> "mineboxtools.command.lookup.error.server";
            case NETWORK_ERROR    -> "mineboxtools.command.lookup.error.network";
            case INVALID_USERNAME -> "mineboxtools.command.lookup.error.invalid_username";
        };
        return error(Lang.get(key).replace("{0}", username));
    }

    private static String formatXp(long xp) {
        if (xp >= 1_000_000) return String.format("%.2fM", xp / 1_000_000.0);
        if (xp >= 1_000)     return String.format("%,.0f", (double) xp);
        return String.valueOf(xp);
    }

    private static MutableComponent error(String text) {
        return colored(text, COLOR_RED);
    }

    private static MutableComponent labelValue(String label, String value) {
        return colored(label + "  ", COLOR_GRAY).append(colored(value, COLOR_WHITE));
    }

    private static MutableComponent colored(String text, int rgb) {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }

    private static void send(FabricClientCommandSource source, Component component) {
        source.sendFeedback(component);
    }
}
