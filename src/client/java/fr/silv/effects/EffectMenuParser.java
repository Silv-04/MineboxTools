package fr.silv.effects;

import fr.silv.utils.MineboxItemDataUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the open effects menu into a list of {@link ActiveConsumable}.
 *
 * <p>Every timed effect item shows its remaining time on a single lore line coloured Minebox gold;
 * that line is the only gold lore line carrying digits ("Stats" is gold but has none, stat lines are
 * green, flavour text is grey). The remaining time is read language-independently: the parenthetical
 * bonus (e.g. {@code (+10%)}) is stripped, then the integers are read right-to-left as seconds,
 * minutes, hours - so {@code 17 secondes}, {@code 29 minutes, 21 secondes} and
 * {@code 2 heures, 44 minutes, 32 secondes} all parse without knowing the language's unit words.
 *
 * <p>Items with no such line - filler panes, and permanent unlocks whose duration is instead the
 * word "permanent" - yield nothing and are skipped.
 */
public final class EffectMenuParser {
    /** The gold Minebox uses for headers and the remaining-time line ({@code #F9E97E}). */
    private static final int GOLD = 0xF9E97E;
    private static final Pattern PARENTHESISED = Pattern.compile("\\([^)]*\\)");
    private static final Pattern INTEGER = Pattern.compile("\\d+");

    private EffectMenuParser() {
    }

    /** Parses the container side of {@code menu} into the effects it lists. */
    public static List<ActiveConsumable> parse(AbstractContainerMenu menu, Inventory playerInventory) {
        List<ActiveConsumable> effects = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Slot slot : menu.slots) {
            // The menu shows the player's own inventory too; only the container side is effects.
            if (slot.container == playerInventory) {
                continue;
            }
            ItemStack stack = slot.getItem();
            // Beacon buffs (Balise) are shown in the menu but aren't consumables - blacklist them.
            if (stack.isEmpty() || stack.is(Items.BEACON)) {
                continue;
            }
            OptionalLong remaining = remainingSeconds(stack);
            if (remaining.isEmpty()) {
                continue;
            }
            String name = stack.getHoverName().getString();
            String sourceType = sourceType(stack).orElse(null);
            effects.add(new ActiveConsumable(sourceType, name,
                    now + remaining.getAsLong() * 1000L, stack.copy()));
        }
        return effects;
    }

    /** The remaining seconds read from the item's gold-coloured duration line, if it has one. */
    private static OptionalLong remainingSeconds(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) {
            return OptionalLong.empty();
        }
        for (Component line : lore.lines()) {
            TextColor color = line.getStyle().getColor();
            if (color == null || color.getValue() != GOLD) {
                continue;
            }
            String text = line.getString();
            if (INTEGER.matcher(text).find()) {
                return parseDuration(text);
            }
        }
        return OptionalLong.empty();
    }

    /** Reads "…: 2 heures, 44 minutes, 32 secondes (+10%)" into a second count, unit words ignored. */
    static OptionalLong parseDuration(String text) {
        String withoutBonus = PARENTHESISED.matcher(text).replaceAll(" ");
        List<Integer> numbers = new ArrayList<>();
        Matcher matcher = INTEGER.matcher(withoutBonus);
        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }
        if (numbers.isEmpty()) {
            return OptionalLong.empty();
        }
        // Units run high-to-low and seconds are always shown, so read from the right: s, m, h.
        long[] multipliers = {1L, 60L, 3600L};
        long total = 0L;
        for (int i = 0; i < numbers.size(); i++) {
            int fromRight = numbers.size() - 1 - i;
            long multiplier = fromRight < multipliers.length ? multipliers[fromRight] : 3600L;
            total += numbers.get(i) * multiplier;
        }
        return OptionalLong.of(total);
    }

    /** The {@code mythicmobs:type} the effects menu tags the item with, used to link to the catalog. */
    private static Optional<String> sourceType(ItemStack stack) {
        return MineboxItemDataUtils.getCustomData(stack)
                .flatMap(root -> root.getCompound("PublicBukkitValues"))
                .flatMap(values -> values.getString("mythicmobs:type"));
    }
}
