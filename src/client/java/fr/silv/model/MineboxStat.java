package fr.silv.model;

/**
 * Immutable pair of stat key and numeric value parsed from item lore.
 *
 * @param stat  stat key identifier (e.g. {@code mbx.stats.fortune})
 * @param value numeric stat value
 */
public record MineboxStat(String stat, int value) {

    /**
     * Returns the stat key.
     *
     * @return stat key identifier
     */
    public String getStat() {
        return stat;
    }

    /**
     * Returns the numeric stat value.
     *
     * @return stat value
     */
    public int getValue() {
        return value;
    }
}
