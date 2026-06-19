package fr.silv.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a guild profile returned by the Minebox API.
 */
public final class GuildProfile {
    /** Unique guild UUID. */
    public String id;
    /** Display name of the guild. */
    public String name;
    /** Banner identifier, or {@code null} when not set. */
    public String banner;
    /** Current guild level. */
    public int level;
    /** Accumulated guild XP. */
    public long xp;
    /** List of guild members. */
    public List<Member> members;

    /**
     * A single guild member entry.
     */
    public static final class Member {
        /** In-game username. */
        public String username;
        /** Whether the player is currently online. */
        public boolean online;
        /** Whether this member is the guild owner. */
        @SerializedName("is_owner")
        public boolean isOwner;
    }
}
