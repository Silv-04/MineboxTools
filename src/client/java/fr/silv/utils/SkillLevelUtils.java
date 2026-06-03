package fr.silv.utils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads skill XP-per-level tables from {@code skills.json} and converts
 * accumulated experience points into a skill level.
 */
public final class SkillLevelUtils {
    private static final Logger LOGGER = ModLog.getLogger(SkillLevelUtils.class);
    private static final Gson GSON = new Gson();
    private static final Map<String, int[]> SKILL_TABLES = new HashMap<>();
    private static final List<String> SORTED_SKILL_IDS = new ArrayList<>();

    private SkillLevelUtils() {
    }

    /**
     * Loads skill XP tables from the bundled {@code skills.json} resource.
     * Must be called once during client initialisation before any level lookups.
     */
    public static void load() {
        try (InputStream is = SkillLevelUtils.class.getResourceAsStream("/assets/mineboxtools/skills.json")) {
            if (is == null) {
                LOGGER.error("skills.json not found in classpath");
                return;
            }
            SkillsJson data = parseSkillsJson(is);
            if (data == null || data.skills == null) {
                LOGGER.error("skills.json is empty or malformed");
                return;
            }
            SKILL_TABLES.clear();
            SORTED_SKILL_IDS.clear();
            for (SkillEntry entry : data.skills) {
                SKILL_TABLES.put(entry.id.toLowerCase(), entry.experiencePerLevel);
                SORTED_SKILL_IDS.add(entry.id.toLowerCase());
            }
            SORTED_SKILL_IDS.sort(String::compareTo);
            LOGGER.info("Loaded {} skill XP tables", SKILL_TABLES.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load skills.json", e);
        }
    }

    /**
     * Converts accumulated experience points into a skill level.
     * Returns 1 if the skill is unknown or the XP is below the first threshold.
     *
     * @param skillId  lowercase skill identifier (e.g. {@code miner})
     * @param totalXp  total accumulated experience points
     * @return computed skill level, at least 1
     */
    public static int expToLevel(String skillId, long totalXp) {
        int[] table = SKILL_TABLES.get(skillId.toLowerCase());
        if (table == null) return 1;
        int level = 1;
        long cumulative = 0;
        for (int i = 1; i < table.length; i++) {
            cumulative += table[i];
            if (cumulative > totalXp) break;
            level++;
        }
        return level;
    }

    /**
     * Returns all known skill IDs sorted alphabetically.
     *
     * @return immutable list of skill IDs
     */
    public static List<String> getSortedSkillIds() {
        return List.copyOf(SORTED_SKILL_IDS);
    }

    private static SkillsJson parseSkillsJson(InputStream is) {
        return GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), SkillsJson.class);
    }

    private static final class SkillsJson {
        List<SkillEntry> skills;
    }

    private static final class SkillEntry {
        String id;
        @SerializedName("experience_per_level")
        int[] experiencePerLevel;
    }
}
