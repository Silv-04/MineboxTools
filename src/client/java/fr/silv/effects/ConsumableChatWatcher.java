package fr.silv.effects;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.Locale;

/**
 * Watches system chat for a consumable being <em>dispelled</em> and rescans when it sees one.
 *
 * <p>Dispelling removes an effect before it expires, and unlike eating it fires no client event, so
 * the widget would keep counting a gone effect down until the next scan. Expiry needs no such handling
 * - the local countdown drops those on its own - so only dispels are watched here, and since dispelling
 * is a rare manual action a rescan per hit costs no meaningful {@code /effect} traffic.
 *
 * <p>Matching is by word stem across languages ({@code dispel} / {@code dissip}) rather than a fixed
 * sentence, which is enough for a trigger: the rescan reads the authoritative menu, so a false match
 * only costs one scan and the exact wording never has to be known.
 */
public final class ConsumableChatWatcher {
    private ConsumableChatWatcher() {
    }

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) {
                return; // action-bar text, not a chat line
            }
            String text = message.getString().toLowerCase(Locale.ROOT);
            if (text.contains("dispel") || text.contains("dissip")) {
                EffectScanController.requestScan();
            }
        });
    }
}
