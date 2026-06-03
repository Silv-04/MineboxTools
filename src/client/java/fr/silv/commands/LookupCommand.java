package fr.silv.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.silv.Lang;
import fr.silv.ModConfig;
import fr.silv.api.MineboxApiClient;
import fr.silv.api.PlayerProfile;
import fr.silv.utils.SkillLevelUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registers the {@code /mbtlookup <username>} client command that fetches and
 * displays a player profile from the Minebox API in the chat.
 */
@SuppressWarnings("null") // Fabric/Minecraft APIs lack consistent @NonNull annotations
public final class LookupCommand {
    private static final String SEPARATOR = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final int COLOR_GOLD   = 0xFFAA00;
    private static final int COLOR_GRAY   = 0xAAAAAA;
    private static final int COLOR_WHITE  = 0xFFFFFF;
    private static final int COLOR_YELLOW = 0xFFFF55;
    private static final int COLOR_GREEN  = 0x55FF55;
    private static final int COLOR_RED    = 0xFF5555;

    private LookupCommand() {
    }

    /**
     * Registers the {@code /mbtlookup} command on the provided dispatcher.
     *
     * @param dispatcher Brigadier dispatcher used to register client commands
     */
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        ArgumentType<String> wordArg = StringArgumentType.word();
        dispatcher.register(ClientCommands.literal("mbtlookup")
                .then(ClientCommands.argument("username", wordArg)
                        .executes(context -> {
                            String username = StringArgumentType.getString(context, "username");
                            execute(context.getSource(), username);
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    /**
     * Validates the feature flag, sends a loading message, then triggers the async
     * API call and dispatches the result back to the main thread.
     *
     * @param source   command source used to send chat feedback
     * @param username player username to look up
     */
    private static void execute(FabricClientCommandSource source, String username) {
        send(source, colored(Lang.get("mineboxtools.command.lookup.loading"), COLOR_YELLOW));

        MineboxApiClient.fetchPlayerProfile(username)
                .thenAcceptAsync(result -> {
                    if (result.isOk()) {
                        displayProfile(source, result.getValue());
                    } else {
                        send(source, errorComponent(result.getError(), username));
                    }
                }, Minecraft.getInstance());
    }

    /**
     * Builds and sends all chat lines for a successfully retrieved player profile.
     *
     * @param source  command source used to send chat feedback
     * @param profile profile data returned by the API
     */
    private static void displayProfile(FabricClientCommandSource source, PlayerProfile profile) {
        Locale locale = ModConfig.getLanguage().equals("fr_fr") ? Locale.FRENCH : Locale.ENGLISH;

        send(source, separator());

        int dotColor = profile.online ? COLOR_GREEN : COLOR_RED;
        MutableComponent header = colored("● ", dotColor)
                .append(colored(profile.username + "  ", COLOR_WHITE))
                .append(colored("Lv." + profile.level, COLOR_YELLOW));
        send(source, header);

        send(source, separator());

        send(source, labelValue(Lang.get("mineboxtools.command.lookup.playtime"), formatPlaytime(profile.playtime)));
        send(source, labelValue(Lang.get("mineboxtools.command.lookup.first_seen"), formatDate(profile.firstConnection, locale)));
        send(source, labelValue(Lang.get("mineboxtools.command.lookup.last_seen"), formatDate(profile.lastConnection, locale)));

        if (profile.serverInstance != null && !profile.serverInstance.isEmpty()) {
            String islandId = extractIslandId(profile.serverInstance);
            String islandName = Lang.get("mineboxtools.island." + islandId);
            send(source, labelValue(
                    Lang.get("mineboxtools.command.lookup.instance"),
                    profile.serverInstance + " (" + islandName + ")"));
        }

        send(source, separator());
        send(source, colored(Lang.get("mineboxtools.command.lookup.skills"), COLOR_YELLOW));

        Map<String, Long> apiSkills = (profile.data != null && profile.data.skills != null)
                ? profile.data.skills.data : Map.of();
        List<String> skillIds = SkillLevelUtils.getSortedSkillIds();

        for (int i = 0; i < skillIds.size(); i += 2) {
            String id1 = skillIds.get(i);
            String name1 = Lang.get("mineboxtools.skill." + id1);
            int lvl1 = resolveLevel(id1, apiSkills);

            if (i + 1 < skillIds.size()) {
                String id2 = skillIds.get(i + 1);
                String name2 = Lang.get("mineboxtools.skill." + id2);
                int lvl2 = resolveLevel(id2, apiSkills);
                send(source, colored(name1 + " ", COLOR_GRAY)
                        .append(colored("Lv." + lvl1, COLOR_WHITE))
                        .append(colored("    " + name2 + " ", COLOR_GRAY))
                        .append(colored("Lv." + lvl2, COLOR_WHITE)));
            } else {
                send(source, colored(name1 + " ", COLOR_GRAY)
                        .append(colored("Lv." + lvl1, COLOR_WHITE)));
            }
        }

        Map<String, Object> relics = (profile.data != null
                && profile.data.objectives != null
                && profile.data.objectives.relics != null)
                ? profile.data.objectives.relics : Map.of();

        if (!relics.isEmpty()) {
            send(source, separator());
            send(source, colored(Lang.get("mineboxtools.command.lookup.relics"), COLOR_YELLOW));

            List<String> relicKeys = new ArrayList<>(relics.keySet());
            relicKeys.sort(String::compareTo);

            for (int i = 0; i < relicKeys.size(); i += 2) {
                String name1 = Lang.get("mineboxtools.relic." + relicKeys.get(i));
                if (i + 1 < relicKeys.size()) {
                    String name2 = Lang.get("mineboxtools.relic." + relicKeys.get(i + 1));
                    send(source, colored(name1 + "    " + name2, COLOR_GRAY));
                } else {
                    send(source, colored(name1, COLOR_GRAY));
                }
            }
        }

        send(source, separator());
    }

    /**
     * Builds a translated, red-coloured error component for the given lookup failure.
     *
     * @param error    the failure category
     * @param username username that was looked up, used in the not-found message
     * @return styled error component
     */
    private static Component errorComponent(MineboxApiClient.LookupError error, String username) {
        String key = switch (error) {
            case NOT_FOUND      -> "mineboxtools.command.lookup.error.not_found";
            case PROFILE_PRIVATE -> "mineboxtools.command.lookup.error.private";
            case RATE_LIMITED   -> "mineboxtools.command.lookup.error.rate_limited";
            case SERVER_ERROR   -> "mineboxtools.command.lookup.error.server";
            case NETWORK_ERROR  -> "mineboxtools.command.lookup.error.network";
        };
        return colored(Lang.get(key).replace("{0}", username), COLOR_RED);
    }

    /**
     * Resolves the level for a skill from the API skill map, defaulting to 1 when absent.
     *
     * @param skillId  lowercase skill identifier
     * @param apiSkills map of uppercase skill IDs to accumulated XP from the API
     * @return computed skill level, at least 1
     */
    private static int resolveLevel(String skillId, Map<String, Long> apiSkills) {
        Long xp = apiSkills.get(skillId.toUpperCase());
        if (xp == null) return 1;
        return SkillLevelUtils.expToLevel(skillId, xp);
    }

    /**
     * Extracts the island ID from a server instance string by removing the trailing
     * random code (e.g. {@code island_plain-7pgl9} → {@code island_plain}).
     *
     * @param serverInstance raw instance identifier from the API
     * @return island ID without the trailing code segment
     */
    private static String extractIslandId(String serverInstance) {
        int lastDash = serverInstance.lastIndexOf('-');
        if (lastDash <= 0) return serverInstance;
        return serverInstance.substring(0, lastDash);
    }

    /**
     * Formats a duration in seconds as a human-readable string (e.g. {@code 1d 15h 25m 8s}).
     *
     * @param seconds total duration in seconds
     * @return formatted duration string
     */
    private static String formatPlaytime(long seconds) {
        long days    = seconds / 86400;
        long hours   = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs    = seconds % 60;
        if (days > 0)    return days + "d " + hours + "h " + minutes + "m " + secs + "s";
        if (hours > 0)   return hours + "h " + minutes + "m " + secs + "s";
        if (minutes > 0) return minutes + "m " + secs + "s";
        return secs + "s";
    }

    /**
     * Parses an ISO-8601 timestamp and formats it as {@code d MMM yyyy} in the given locale.
     *
     * @param isoDate ISO-8601 date string from the API
     * @param locale  locale used for month name localisation
     * @return formatted date string, or {@code —} when the input is blank or unparseable
     */
    private static String formatDate(String isoDate, Locale locale) {
        if (isoDate == null || isoDate.isEmpty()) return "—";
        try {
            ZonedDateTime zdt = Instant.parse(isoDate).atZone(ZoneId.of("UTC"));
            return DateTimeFormatter.ofPattern("d MMM yyyy", locale).format(zdt);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private static MutableComponent separator() {
        return colored(SEPARATOR, COLOR_GOLD);
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
