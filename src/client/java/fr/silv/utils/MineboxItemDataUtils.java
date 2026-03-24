package fr.silv.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Parses and caches item data and availability metadata.
 */
public final class MineboxItemDataUtils {
    private static final String STATS_KEY = "mbitems:stats";
    private static final String ITEM_ID_KEY = "mbitems:id";
    private static final String DISPLAY_KEY = "mbitems:display";
    private static final String PERSISTENT_KEY = "mbitems:persistent";
    private static final String DURABILITY_KEY = "mbitems:durability";
    private static final String DURABILITY_STAT_KEY = "mbx.durability";

    private static final CachedItemData EMPTY_ITEM_DATA = new CachedItemData(
            Optional.empty(),
            Optional.empty(),
            false,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );
    private static final Map<NbtComponent, CachedItemData> ITEM_DATA_CACHE = new WeakHashMap<>();
    private static final Map<String, String[]> KEY_PATH_CACHE = new HashMap<>();

    private MineboxItemDataUtils() {
    }

    /**
     * Returns the custom data.
     * @param stack value for stack
     * @return an optional nbtcompound value when present
     */
    public static Optional<NbtCompound> getCustomData(ItemStack stack) {
        return getCachedItemData(stack).customData();
    }

    /**
     * Returns the item id.
     * @param stack value for stack
     * @return an optional string value when present
     */
    public static Optional<String> getItemId(ItemStack stack) {
        return getCachedItemData(stack).itemId();
    }

    /**
     * Returns the item id.
     * @param nbt value for nbt
     * @return an optional string value when present
     */
    public static Optional<String> getItemId(NbtCompound nbt) {
        return nbt.getString(ITEM_ID_KEY);
    }

    /**
     * Checks whether display only item.
     * @param stack value for stack
     * @return true if the condition is met; otherwise false
     */
    public static boolean isDisplayOnlyItem(ItemStack stack) {
        return getCachedItemData(stack).displayOnly();
    }

    /**
     * Checks whether display only item.
     * @param nbt value for nbt
     * @return true if the condition is met; otherwise false
     */
    public static boolean isDisplayOnlyItem(NbtCompound nbt) {
        return nbt.getInt(DISPLAY_KEY).orElse(0) == 1;
    }

    /**
     * Returns the persistent data.
     * @param nbt value for nbt
     * @return an optional nbtcompound value when present
     */
    public static Optional<NbtCompound> getPersistentData(NbtCompound nbt) {
        return nbt.getCompound(PERSISTENT_KEY)
                .filter(persistent -> !persistent.isEmpty());
    }

    /**
     * Returns the persistent data.
     * @param stack value for stack
     * @return an optional nbtcompound value when present
     */
    public static Optional<NbtCompound> getPersistentData(ItemStack stack) {
        return getCachedItemData(stack).persistentData();
    }

    /**
     * Returns the persistent item data.
     * @param stack value for stack
     * @return an optional persistentitemdata value when present
     */
    public static Optional<PersistentItemData> getPersistentItemData(ItemStack stack) {
        return getCachedItemData(stack).persistentItemData();
    }

    /**
     * Returns the persistent item data.
     * @param nbt value for nbt
     * @return an optional persistentitemdata value when present
     */
    public static Optional<PersistentItemData> getPersistentItemData(NbtCompound nbt) {
        Optional<String> itemId = getItemId(nbt);
        Optional<NbtCompound> persistent = getPersistentData(nbt);
        if (itemId.isEmpty() || persistent.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new PersistentItemData(nbt, itemId.get(), persistent.get()));
    }

    /**
     * Checks whether value is available.
     * @param nbt value for nbt
     * @param keyPath value for keyPath
     * @return true if the condition is met; otherwise false
     */
    public static boolean hasValue(NbtCompound nbt, String keyPath) {
        return getValue(nbt, keyPath).isPresent();
    }

    /**
     * Checks whether value is available.
     * @param stack value for stack
     * @param keyPath value for keyPath
     * @return true if the condition is met; otherwise false
     */
    public static boolean hasValue(ItemStack stack, String keyPath) {
        return getValue(stack, keyPath).isPresent();
    }

    /**
     * Returns the value.
     * @param stack value for stack
     * @param keyPath value for keyPath
     * @return an optional nbtelement value when present
     */
    public static Optional<NbtElement> getValue(ItemStack stack, String keyPath) {
        return getCustomData(stack).flatMap(nbt -> getValue(nbt, keyPath));
    }

    /**
     * Returns the value from persistent.
     * @param stack value for stack
     * @param keyPath value for keyPath
     * @return an optional nbtelement value when present
     */
    public static Optional<NbtElement> getValueFromPersistent(ItemStack stack, String keyPath) {
        return getPersistentData(stack).flatMap(nbt -> getValue(nbt, keyPath));
    }

    /**
     * Returns the value.
     * @param nbt value for nbt
     * @param keyPath value for keyPath
     * @return an optional nbtelement value when present
     */
    public static Optional<NbtElement> getValue(NbtCompound nbt, String keyPath) {
        if (nbt == null || keyPath == null || keyPath.isBlank()) {
            return Optional.empty();
        }

        String[] parts = splitKeyPath(keyPath);
        NbtCompound current = nbt;

        for (int i = 0; i < parts.length; i++) {
            NbtElement value = current.get(parts[i]);
            if (value == null) {
                return Optional.empty();
            }

            if (i == parts.length - 1) {
                return Optional.of(value);
            }

            if (!(value instanceof NbtCompound compound)) {
                return Optional.empty();
            }
            current = compound;
        }

        return Optional.empty();
    }

    /**
     * Returns the string value.
     * @param stack value for stack
     * @param keyPath value for keyPath
     * @return an optional string value when present
     */
    public static Optional<String> getStringValue(ItemStack stack, String keyPath) {
        return getCustomData(stack).flatMap(nbt -> getStringValue(nbt, keyPath));
    }

    /**
     * Returns the string value from persistent.
     * @param stack value for stack
     * @param keyPath value for keyPath
     * @return an optional string value when present
     */
    public static Optional<String> getStringValueFromPersistent(ItemStack stack, String keyPath) {
        return getPersistentData(stack).flatMap(nbt -> getStringValue(nbt, keyPath));
    }

    /**
     * Returns the string value.
     * @param nbt value for nbt
     * @param keyPath value for keyPath
     * @return an optional string value when present
     */
    public static Optional<String> getStringValue(NbtCompound nbt, String keyPath) {
        if (nbt == null || keyPath == null || keyPath.isBlank()) {
            return Optional.empty();
        }

        String[] parts = splitKeyPath(keyPath);
        NbtCompound parent = getParentCompound(nbt, parts);
        if (parent == null) {
            return Optional.empty();
        }
        return parent.getString(parts[parts.length - 1]);
    }

    /**
     * Returns the int value.
     * @param stack value for stack
     * @param keyPath value for keyPath
     * @return an optional integer value when present
     */
    public static Optional<Integer> getIntValue(ItemStack stack, String keyPath) {
        return getCustomData(stack).flatMap(nbt -> getIntValue(nbt, keyPath));
    }

    /**
     * Returns the int value from persistent.
     * @param stack value for stack
     * @param keyPath value for keyPath
     * @return an optional integer value when present
     */
    public static Optional<Integer> getIntValueFromPersistent(ItemStack stack, String keyPath) {
        return getPersistentData(stack).flatMap(nbt -> getIntValue(nbt, keyPath));
    }

    /**
     * Returns the int value.
     * @param nbt value for nbt
     * @param keyPath value for keyPath
     * @return an optional integer value when present
     */
    public static Optional<Integer> getIntValue(NbtCompound nbt, String keyPath) {
        if (nbt == null || keyPath == null || keyPath.isBlank()) {
            return Optional.empty();
        }

        String[] parts = splitKeyPath(keyPath);
        NbtCompound parent = getParentCompound(nbt, parts);
        if (parent == null) {
            return Optional.empty();
        }
        return parent.getInt(parts[parts.length - 1]);
    }

    /**
     * Returns the compound value.
     * @param stack value for stack
     * @param keyPath value for keyPath
     * @return an optional nbtcompound value when present
     */
    public static Optional<NbtCompound> getCompoundValue(ItemStack stack, String keyPath) {
        return getCustomData(stack).flatMap(nbt -> getCompoundValue(nbt, keyPath));
    }

    /**
     * Returns the compound value from persistent.
     * @param stack value for stack
     * @param keyPath value for keyPath
     * @return an optional nbtcompound value when present
     */
    public static Optional<NbtCompound> getCompoundValueFromPersistent(ItemStack stack, String keyPath) {
        return getPersistentData(stack).flatMap(nbt -> getCompoundValue(nbt, keyPath));
    }

    /**
     * Returns the compound value.
     * @param nbt value for nbt
     * @param keyPath value for keyPath
     * @return an optional nbtcompound value when present
     */
    public static Optional<NbtCompound> getCompoundValue(NbtCompound nbt, String keyPath) {
        if (nbt == null || keyPath == null || keyPath.isBlank()) {
            return Optional.empty();
        }

        String[] parts = splitKeyPath(keyPath);
        NbtCompound parent = getParentCompound(nbt, parts);
        if (parent == null) {
            return Optional.empty();
        }
        return parent.getCompound(parts[parts.length - 1]);
    }

    /**
     * Returns the current durability.
     * @param persistent value for persistent
     * @return an optional integer value when present
     */
    public static Optional<Integer> getCurrentDurability(NbtCompound persistent) {
        return persistent.getInt(DURABILITY_KEY);
    }

    /**
     * Returns the current durability.
     * @param stack value for stack
     * @return an optional integer value when present
     */
    public static Optional<Integer> getCurrentDurability(ItemStack stack) {
        return getCachedItemData(stack).currentDurability();
    }

    /**
     * Returns the stats data.
     * @param persistent value for persistent
     * @return an optional nbtcompound value when present
     */
    public static Optional<NbtCompound> getStatsData(NbtCompound persistent) {
        return persistent.getCompound(STATS_KEY)
                .filter(stats -> !stats.isEmpty());
    }

    /**
     * Returns the stats data.
     * @param stack value for stack
     * @return an optional nbtcompound value when present
     */
    public static Optional<NbtCompound> getStatsData(ItemStack stack) {
        return getCachedItemData(stack).statsData();
    }

    /**
     * Returns the max durability.
     * @param itemId value for itemId
     * @return an optional integer value when present
     */
    public static Optional<Integer> getMaxDurability(String itemId) {
        Map<String, int[]> stats = MineboxItemStatUtils.getStatsFor(itemId);
        int[] range = stats.get(DURABILITY_STAT_KEY);
        if (range == null || range.length == 0) {
            return Optional.empty();
        }
        return Optional.of(range[0]);
    }

    /**
     * Checks whether equipment or accessory.
     * @param itemId value for itemId
     * @return true if the condition is met; otherwise false
     */
    public static boolean isEquipmentOrAccessory(String itemId) {
        return itemId.contains("helmet")
                || itemId.contains("chestplate")
                || itemId.contains("leggings")
                || itemId.contains("boots")
                || itemId.contains("ring")
                || itemId.contains("belt")
                || itemId.contains("back")
                || itemId.contains("necklace")
                || itemId.contains("pet");
    }

    private static String[] splitKeyPath(String keyPath) {
        return KEY_PATH_CACHE.computeIfAbsent(keyPath, path -> path.split("\\."));
    }

    private static NbtCompound getParentCompound(NbtCompound root, String[] parts) {
        NbtCompound current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Optional<NbtCompound> child = current.getCompound(parts[i]);
            if (child.isEmpty()) {
                return null;
            }
            current = child.get();
        }
        return current;
    }

    private static CachedItemData getCachedItemData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return EMPTY_ITEM_DATA;
        }

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null || nbtComponent.isEmpty()) {
            return EMPTY_ITEM_DATA;
        }

        CachedItemData cachedData = ITEM_DATA_CACHE.get(nbtComponent);
        if (cachedData != null) {
            return cachedData;
        }

        NbtCompound customData = nbtComponent.copyNbt();
        Optional<String> itemId = getItemId(customData);
        boolean displayOnly = isDisplayOnlyItem(customData);
        Optional<NbtCompound> persistentData = getPersistentData(customData);
        Optional<PersistentItemData> persistentItemData = itemId.isPresent() && persistentData.isPresent()
                ? Optional.of(new PersistentItemData(customData, itemId.get(), persistentData.get()))
                : Optional.empty();
        Optional<Integer> currentDurability = persistentData.flatMap(MineboxItemDataUtils::getCurrentDurability);
        Optional<NbtCompound> statsData = persistentData.flatMap(MineboxItemDataUtils::getStatsData);

        CachedItemData itemData = new CachedItemData(
                Optional.of(customData),
                itemId,
                displayOnly,
                persistentData,
                persistentItemData,
                currentDurability,
                statsData
        );
        ITEM_DATA_CACHE.put(nbtComponent, itemData);
        return itemData;
    }

    private record CachedItemData(
            Optional<NbtCompound> customData,
            Optional<String> itemId,
            boolean displayOnly,
            Optional<NbtCompound> persistentData,
            Optional<PersistentItemData> persistentItemData,
            Optional<Integer> currentDurability,
            Optional<NbtCompound> statsData
    ) {
    }

    /**
     * Groups persistent Minebox item metadata extracted from custom data.
     * @param customData full custom NBT payload attached to the item
     * @param itemId resolved Minebox item identifier
     * @param persistentData persistent NBT subtree for gameplay metadata
     */
    public record PersistentItemData(NbtCompound customData, String itemId, NbtCompound persistentData) {
    }
}
