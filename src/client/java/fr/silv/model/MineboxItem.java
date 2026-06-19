package fr.silv.model;

import java.util.List;

/**
 * Immutable Minebox item metadata loaded from resources.
 *
 * @param id         item identifier
 * @param location   zones where this item can be obtained
 * @param conditions availability condition translation keys (empty if none)
 * @param boost      boost translation key indicating better drop chances (empty if none)
 */
public record MineboxItem(String id, List<String> location, List<String> conditions, String boost) {

    public String getId() {
        return id;
    }

    public List<String> getLocation() {
        return location;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public String getBoost() {
        return boost;
    }
}
