package fr.silv.model;

/**
 * Represents the minebox stat component.
 */
public class MineboxStat {
    private String stat;
    private int value;

    /**
     * Creates a new MineboxStat instance.
        *
        * @param stat stat key identifier
        * @param value numeric stat value
     */
    public MineboxStat(String stat, int value) {
        this.stat = stat;
        this.value = value;
    }
    /**
        * Returns the stat key.
        *
        * @return stat key identifier
     */
    public String getStat() {
        return stat;
    }
    /**
        * Updates the stat key.
        *
        * @param stat new stat key identifier
     */
    public void setStat(String stat) {
        this.stat = stat;
    }
    /**
        * Returns the numeric stat value.
        *
        * @return stat value
     */
    public int getValue() {
        return value;
    }
    /**
        * Updates the numeric stat value.
        *
        * @param value new stat value
     */
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    /**
     * Builds the string representation of this object.
     * @return the string representation of this object
     */
    public String toString() {
        return "MineboxStat{" +
                "stat='" + stat + '\'' +
                ", value=" + value +
                '}';
    }
}
