package fr.silv;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer for MineboxTools.
 * <p>
 * This class runs on both client and server logical sides. All client-only
 * registrations live in {@link fr.silv.MineboxToolsClient}.
 */
public class MineboxTools implements ModInitializer {
    public static final String MOD_ID = "mineboxtools";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Runs once when Fabric finishes loading the mod.
     */
    @Override
    public void onInitialize() {
        LOGGER.info("MineboxTools initialized.");
    }
}
