package fr.silv.model;

import java.util.List;

/**
 * Represents the minebox item component.
 */
public class MineboxItem {
    private String id;
    private List<String> location;
    private String condition;
    private String boost;

    /**
     * Creates a new MineboxItem instance.
     */
    public MineboxItem(){}

    /**
     * Creates a new MineboxItem instance.
        *
        * @param id item identifier
        * @param location locations where this item can be obtained
        * @param condition availability condition text
        * @param boost boost/stat summary text
     */
    public MineboxItem(String id, List<String> location, String condition, String boost) {
        this.id = id;
        this.location = location;
        this.condition = condition;
        this.boost = boost;
    }

    /**
        * Returns boost metadata text associated with the item.
        *
        * @return boost description
     */
    public String getBoost() {
        return boost;
    }
    /**
        * Returns configured source locations for this item.
        *
        * @return location list
     */
    public List<String> getLocation() {
        return location;
    }

    /**
        * Returns condition metadata text associated with the item.
        *
        * @return condition description
     */
    public String getCondition() {
        return condition;
    }

    /**
        * Returns the item identifier.
        *
        * @return item id
     */
    public String getId() {
        return id;
    }

    /**
        * Updates boost metadata text.
        *
        * @param boost new boost description
     */
    public void setBoost(String boost) {
        this.boost = boost;
    }
    /**
        * Updates condition metadata text.
        *
        * @param condition new condition description
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
        * Updates the item identifier.
        *
        * @param id new item id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
        * Updates source locations for this item.
        *
        * @param location new location list
     */
    public void setLocation(List<String> location) {
        this.location = location;
    }
}
