package fr.silv.model;

/**
 * Represents the minebox stat component.
 */
public class MineboxStat {
    private String stat;
    private int value;

    /**
     * Creates a new MineboxStat instance.
     * @param stat value for stat
     * @param value value for value
     */
    public MineboxStat(String stat, int value) {
        this.stat = stat;
        this.value = value;
    }
    /**
     * Returns the stat.
     * @return the stat
     */
    public String getStat() {
        return stat;
    }
    /**
     * Updates the stat.
     * @param stat value for stat
     */
    public void setStat(String stat) {
        this.stat = stat;
    }
    /**
     * Returns the value.
     * @return the value
     */
    public int getValue() {
        return value;
    }
    /**
     * Updates the value.
     * @param value value for value
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
