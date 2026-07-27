package fr.silv.effects;

import com.mojang.blaze3d.platform.NativeImage;
import fr.silv.utils.ModLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Decodes the base64 PNG icons carried by {@link CatalogEffect} into GPU textures, once each.
 *
 * <p>The catalog ships each effect's icon as a base64 string; this turns it into a registered
 * {@link DynamicTexture} the HUD can blit, caching the result by effect id and remembering failures
 * so a bad payload isn't retried every frame. Not every effect has an icon (many food buffs don't) -
 * those return empty and the caller falls back to rendering the item itself.
 *
 * <p>Must be called from the render thread, since it creates and registers a texture.
 */
public final class EffectIconCache {
    private static final Logger LOGGER = ModLog.getLogger(EffectIconCache.class);
    private static final String NAMESPACE = "mineboxtools";

    private static final Map<String, Icon> CACHE = new HashMap<>();
    private static final Set<String> FAILED = new HashSet<>();

    private EffectIconCache() {
    }

    /** A decoded, registered icon texture and its pixel dimensions. */
    public record Icon(Identifier textureId, int width, int height) {
    }

    /** The effect's icon as a texture, decoding and registering it on first use; empty when it has none. */
    public static Optional<Icon> get(CatalogEffect effect) {
        if (effect == null || effect.icon == null || effect.icon.isBlank()) {
            return Optional.empty();
        }
        String key = effect.id != null ? effect.id : Integer.toHexString(effect.icon.hashCode());

        Icon cached = CACHE.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        if (FAILED.contains(key)) {
            return Optional.empty();
        }

        try {
            byte[] png = Base64.getDecoder().decode(effect.icon);
            NativeImage image = NativeImage.read(png);
            Identifier textureId = Identifier.fromNamespaceAndPath(NAMESPACE, "effect_icon/" + sanitize(key));
            DynamicTexture texture = new DynamicTexture(() -> "mbt-effect-" + key, image);
            Minecraft.getInstance().getTextureManager().register(textureId, texture);
            Icon icon = new Icon(textureId, image.getWidth(), image.getHeight());
            CACHE.put(key, icon);
            return Optional.of(icon);
        } catch (Exception e) {
            FAILED.add(key);
            LOGGER.warn("Failed to decode effect icon for '{}'", key, e);
            return Optional.empty();
        }
    }

    private static String sanitize(String key) {
        return key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
    }
}
