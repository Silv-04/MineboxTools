package fr.silv.model;

import java.util.List;

/**
 * Immutable Minebox item metadata loaded from resources.
 *
 * @param id        item identifier
 * @param location  zones where this item can be obtained
 * @param condition availability condition translation key (empty if none)
 * @param boost     boost translation key indicating better drop chances (empty if none)
 */
public record MineboxItem(String id, List<String> location, String condition, String boost) {

    /**
     * Returns the item identifier.
     *
     * @return item id
     */
    public String getId() {
        return id;
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
     * Returns boost metadata text associated with the item.
     *
     * @return boost description
     */
    public String getBoost() {
        return boost;
    }
}
