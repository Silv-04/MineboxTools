package fr.silv.effects;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * One entry of the Minebox effects catalog (see {@code https://api.minebox.co/effects}).
 *
 * <p>Holds the permanent, server-defined description of an effect - its stats, base duration, icon
 * and the source items that grant it. The live remaining time of an <em>active</em> effect is not
 * here; that is read from the in-game effects menu and kept in {@link ActiveConsumable}.
 *
 * <p>Fields are public and loosely typed because this is a Gson data-transfer object; only the parts
 * the mod consumes are declared, and any others in the payload are ignored.
 */
public final class CatalogEffect {
    /** Stable effect id, e.g. {@code clownfish_curry} or {@code candy_strength}. */
    public String id;

    /** Display name, e.g. {@code Clownfish Curry}; absent for some effects (beacons). */
    public String name;

    /** Whether this effect is a beneficial buff. */
    public boolean buff;

    /** Base duration in seconds; absent for effects that only define per-item durations. */
    public Integer duration;

    /** Groups variants of the same effect, e.g. {@code candy_strength}. */
    @SerializedName("group_id")
    public String groupId;

    /** Per-source-item durations, e.g. {@code candy_strength} 3600s vs {@code candy_enchanted_strength} 43200s. */
    public List<DurationEntry> durations;

    /** The concrete stat/potion/attribute changes this effect applies. */
    public List<EffectComponent> effects;

    /** Base64-encoded PNG icon, when the effect ships one. */
    public String icon;

    /** Maps a source item id to the duration it grants. */
    public static final class DurationEntry {
        public Integer duration;
        public String item;
    }

    /** One change applied by an effect; the populated fields depend on {@link #type}. */
    public static final class EffectComponent {
        /** {@code stats}, {@code stat_percent}, {@code potion}, {@code attribute}, {@code sound}, {@code skill}, {@code bleed}. */
        public String type;

        /** Flat stat deltas keyed by upper-case stat id (e.g. {@code STRENGTH}); set when {@code type == "stats"}. */
        public Map<String, Double> stats;

        /** Percentage stat deltas keyed by lower-case stat id (e.g. {@code fortune}); set when {@code type == "stat_percent"}. */
        public Map<String, Double> percents;

        /** Vanilla potion id; set when {@code type == "potion"}. */
        @SerializedName("potion_type")
        public String potionType;

        /** Vanilla attribute id; set when {@code type == "attribute"}. */
        public String attribute;

        /** Attribute amount; set when {@code type == "attribute"}. */
        public Double value;
    }
}
