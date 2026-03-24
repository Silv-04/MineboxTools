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
     * @param id value for id
     * @param location value for location
     * @param condition value for condition
     * @param boost value for boost
     */
    public MineboxItem(String id, List<String> location, String condition, String boost) {
        this.id = id;
        this.location = location;
        this.condition = condition;
        this.boost = boost;
    }

    /**
     * Returns the boost.
     * @return the boost
     */
    public String getBoost() {
        return boost;
    }
    /**
     * Returns the location.
     * @return the location
     */
    public List<String> getLocation() {
        return location;
    }

    /**
     * Returns the condition.
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Returns the id.
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Updates the boost.
     * @param boost value for boost
     */
    public void setBoost(String boost) {
        this.boost = boost;
    }
    /**
     * Updates the condition.
     * @param condition value for condition
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Updates the id.
     * @param id value for id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Updates the location.
     * @param location value for location
     */
    public void setLocation(List<String> location) {
        this.location = location;
    }
}
