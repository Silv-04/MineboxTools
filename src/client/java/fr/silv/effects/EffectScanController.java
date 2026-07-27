package fr.silv.effects;

import fr.silv.utils.ModLog;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Runs a silent {@code /effect} scan by intercepting the menu at the packet level: the effects menu
 * is recognised by its title in the open-screen packet, cancelled there so no screen or menu is ever
 * created, and its items are read straight from the follow-up content packet.
 *
 * <p>Because the effects menu is identified by title - not by "the next menu after we asked" - a menu
 * the player opens themselves is never touched no matter how long a scan takes, so the open/content
 * waits can be generous enough to survive a laggy world-join without ever flashing a menu on screen.
 *
 * <p>The tick drives the request side (send {@code /effect}, wait, time out); the interception hooks
 * ({@link #shouldInterceptOpen}/{@link #onOpenIntercepted} and {@link #shouldInterceptContent}/
 * {@link #onContentIntercepted}) are called from the client-packet mixin.
 */
public final class EffectScanController {
    private static final Logger LOGGER = ModLog.getLogger(EffectScanController.class);

    private static final String EFFECT_COMMAND = "effect";
    /** ~2.5s: lets a burst of uses collapse into one scan and the server apply the new effect first. */
    private static final int SCAN_DELAY_TICKS = 50;
    // Generous, so a laggy server's menu is still caught; safe to wait this long because only the
    // effects menu (by title) is ever intercepted.
    private static final int OPEN_TIMEOUT_TICKS = 300;
    private static final int CONTENT_TIMEOUT_TICKS = 100;
    /** While pending, wait out loading screens / open menus and retry rather than giving up. */
    private static final int READY_RETRY_TICKS = 20;
    private static final int MAX_READY_RETRIES = 40;
    /** Player-inventory slots a menu appends after its own (27 main + 9 hotbar). */
    private static final int PLAYER_INVENTORY_SLOTS = 36;
    /** Localized effects-menu titles, matched as a normalized substring (past the icon glyphs). */
    private static final Set<String> EFFECT_MENU_TITLES = Set.of("effets actifs", "active effects");

    private enum State { IDLE, PENDING, AWAITING_OPEN, AWAITING_CONTENT }

    private static State state = State.IDLE;
    private static int timer = 0;
    private static int capturedContainerId = -1;
    private static int readyRetries = 0;

    private EffectScanController() {
    }

    /** Requests a scan after the normal debounce delay; called when a Minebox consumable is used. */
    public static void requestScan() {
        if (state == State.IDLE || state == State.PENDING) {
            state = State.PENDING;
            timer = SCAN_DELAY_TICKS;
            readyRetries = MAX_READY_RETRIES;
        }
    }

    /** Requests a scan on the next tick, skipping the debounce; used by the test command. */
    public static void forceScan() {
        state = State.PENDING;
        timer = 1;
        readyRetries = MAX_READY_RETRIES;
    }

    /** Whether the open-screen packet for {@code title} is the effects menu we're waiting on. */
    public static boolean shouldInterceptOpen(Component title, int containerId) {
        return state == State.AWAITING_OPEN && titleMatches(title);
    }

    /** Records the intercepted menu's id and waits for its contents; no screen or menu is created. */
    public static void onOpenIntercepted(int containerId) {
        capturedContainerId = containerId;
        state = State.AWAITING_CONTENT;
        timer = CONTENT_TIMEOUT_TICKS;
    }

    /** Whether this content packet belongs to the effects menu we intercepted. */
    public static boolean shouldInterceptContent(int containerId) {
        return state == State.AWAITING_CONTENT && containerId == capturedContainerId;
    }

    /** Reads the effects from the content packet, updates the store, and closes the menu server-side. */
    public static void onContentIntercepted(List<ItemStack> items) {
        // The list is the menu's own slots followed by the player inventory; drop the latter so held
        // consumables can't be mistaken for active effects.
        int containerSlots = Math.max(0, items.size() - PLAYER_INVENTORY_SLOTS);
        List<ActiveConsumable> effects = EffectMenuParser.parse(items.subList(0, containerSlots));
        ActiveEffectsStore.update(effects);
        close(capturedContainerId);
        reset();
        LOGGER.debug("Silent effect scan captured {} active effect(s)", effects.size());
    }

    public static void onClientTick(Minecraft client) {
        switch (state) {
            case IDLE -> { }
            case PENDING -> tickPending(client);
            case AWAITING_OPEN -> {
                if (client.player == null || --timer <= 0) {
                    reset();
                }
            }
            case AWAITING_CONTENT -> {
                if (client.player == null || --timer <= 0) {
                    close(capturedContainerId);
                    reset();
                }
            }
        }
    }

    private static void tickPending(Minecraft client) {
        if (--timer > 0) {
            return;
        }
        // Wait out loading screens and open menus - the join scan especially fires while terrain is
        // still loading - and retry, rather than aborting on a transient screen.
        if (client.getConnection() == null || client.player == null || client.gui.screen() != null) {
            if (readyRetries-- > 0) {
                timer = READY_RETRY_TICKS;
            } else {
                reset();
            }
            return;
        }
        client.getConnection().sendCommand(EFFECT_COMMAND);
        state = State.AWAITING_OPEN;
        timer = OPEN_TIMEOUT_TICKS;
    }

    private static void close(int containerId) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null && containerId >= 0) {
            client.getConnection().send(new ServerboundContainerClosePacket(containerId));
        }
    }

    private static void reset() {
        state = State.IDLE;
        timer = 0;
        capturedContainerId = -1;
    }

    private static boolean titleMatches(Component title) {
        String normalized = normalize(title.getString());
        return EFFECT_MENU_TITLES.stream().anyMatch(normalized::contains);
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }
}
