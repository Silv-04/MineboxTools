package fr.silv.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import fr.silv.constants.StatDefinition;
import fr.silv.effects.ActiveConsumable;
import fr.silv.effects.ActiveEffectsStore;
import fr.silv.effects.CatalogEffect;
import fr.silv.effects.EffectCatalogService;
import fr.silv.effects.EffectScanController;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Development command for the consumable-effects tracking: {@code /mbteffects} prints the effects
 * currently tracked (name, remaining time, and the permanent stats pulled from the catalog), and
 * {@code /mbteffects scan} forces a silent effects scan right away. This is test scaffolding for the
 * eventual HUD, not a user-facing feature yet.
 */
public final class EffectsCommand {
    private static final int COLOR_GOLD = 0xFFAA00;
    private static final int COLOR_GRAY = 0xAAAAAA;
    private static final int COLOR_WHITE = 0xFFFFFF;
    private static final int COLOR_GREEN = 0x55FF55;
    private static final int COLOR_YELLOW = 0xFFFF55;

    private EffectsCommand() {
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("mbteffects")
                .executes(context -> {
                    list(context.getSource());
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommands.literal("scan")
                        .executes(context -> {
                            EffectScanController.forceScan();
                            context.getSource().sendFeedback(colored("Scanning active effects…", COLOR_YELLOW));
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    private static void list(FabricClientCommandSource source) {
        if (!EffectCatalogService.isLoaded()) {
            source.sendFeedback(colored("Effects catalog not loaded yet.", COLOR_YELLOW));
        }

        List<ActiveConsumable> active = ActiveEffectsStore.getActive();
        if (active.isEmpty()) {
            long last = ActiveEffectsStore.lastScanEpochMillis();
            String suffix = last == 0
                    ? " (no scan yet — eat something or run /mbteffects scan)"
                    : " (last scan " + (System.currentTimeMillis() - last) / 1000 + "s ago)";
            source.sendFeedback(colored("No active effects" + suffix, COLOR_GRAY));
            return;
        }

        source.sendFeedback(colored("Active effects (" + active.size() + "):", COLOR_GOLD));
        for (ActiveConsumable effect : active) {
            MutableComponent header = colored("• ", COLOR_GRAY)
                    .append(colored(effect.displayName(), COLOR_GOLD))
                    .append(colored("  " + formatDuration(effect.remainingSeconds()), COLOR_GREEN));
            source.sendFeedback(header);

            Optional<CatalogEffect> catalog = effect.catalogEntry();
            if (catalog.isPresent()) {
                for (Component statLine : statLines(catalog.get())) {
                    source.sendFeedback(colored("    ", COLOR_GRAY).append(statLine));
                }
            } else {
                String key = effect.sourceType() == null ? "?" : effect.sourceType();
                source.sendFeedback(colored("    (no catalog match for '" + key + "')", COLOR_GRAY));
            }
        }
    }

    private static List<Component> statLines(CatalogEffect catalog) {
        if (catalog.effects == null) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>();
        for (CatalogEffect.EffectComponent component : catalog.effects) {
            if (component.stats != null) {
                component.stats.forEach((key, value) -> lines.add(statLine(key, value, false)));
            }
            if (component.percents != null) {
                component.percents.forEach((key, value) -> lines.add(statLine(key, value, true)));
            }
        }
        return lines;
    }

    private static Component statLine(String statKey, double value, boolean percent) {
        String amount = (value >= 0 ? "+" : "") + trim(value) + (percent ? "% " : " ");
        Optional<StatDefinition> definition = statDefinition(statKey);
        String label = definition.map(StatDefinition::advancedLabel).orElse(statKey);
        MutableComponent line = colored(amount, COLOR_GREEN);
        return definition.map(def -> line.append(def.styleText(label)))
                .orElseGet(() -> line.append(colored(label, COLOR_WHITE)));
    }

    private static Optional<StatDefinition> statDefinition(String catalogStatKey) {
        try {
            return Optional.of(StatDefinition.valueOf(catalogStatKey.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static String trim(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (hours > 0 || minutes > 0) {
            builder.append(minutes).append("m ");
        }
        builder.append(secs).append("s");
        return builder.toString();
    }

    private static MutableComponent colored(String text, int rgb) {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }
}
