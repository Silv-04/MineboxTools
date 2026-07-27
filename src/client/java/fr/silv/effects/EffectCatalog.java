package fr.silv.effects;

import java.util.List;

/**
 * Gson shape of the whole {@code https://api.minebox.co/effects} payload: a {@code total} count and
 * the list of {@link CatalogEffect} entries.
 */
public final class EffectCatalog {
    public Integer total;
    public List<CatalogEffect> effects;
}
