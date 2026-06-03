package fr.silv.api;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Represents a player profile returned by the Minebox API.
 */
public final class PlayerProfile {
    /** Unique player UUID. */
    public String id;
    /** In-game username. */
    public String username;
    /** Current player level. */
    public int level;
    /** Total playtime in seconds. */
    public long playtime;
    /** ISO-8601 timestamp of the first connection. */
    @SerializedName("first_connection")
    public String firstConnection;
    /** ISO-8601 timestamp of the last connection. */
    @SerializedName("last_connection")
    public String lastConnection;
    /** Whether the player is currently online. */
    public boolean online;
    /** Current server instance identifier (e.g. {@code island_plain-7pgl9}). */
    @SerializedName("server_instance")
    public String serverInstance;
    /** Player data containing skills and objectives. */
    public Data data;

    /**
     * Top-level player data section.
     */
    public static final class Data {
        /** Skill experience data. */
        @SerializedName("SKILLS")
        public Skills skills;
        /** Objective progress data. */
        @SerializedName("OBJECTIVES")
        public Objectives objectives;
    }

    /**
     * Maps skill IDs (uppercase, e.g. {@code MINER}) to accumulated experience points.
     */
    public static final class Skills {
        public Map<String, Long> data;
    }

    /**
     * Contains relic progress. Keys are relic type names; values are always empty objects.
     */
    public static final class Objectives {
        public Map<String, Object> relics;
    }
}
