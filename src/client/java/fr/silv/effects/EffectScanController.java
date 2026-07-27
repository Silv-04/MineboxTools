package fr.silv.effects;

import fr.silv.utils.ModLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.slf4j.Logger;

import java.util.List;

/**
 * Runs a silent {@code /effect} scan: sends the command, swallows the menu it opens so nothing is
 * shown, reads the effects out of it, then closes it - leaving {@link ActiveEffectsStore} refreshed.
 *
 * <p>Driven off the client tick, because that keeps running while a menu is bound. The flow is a
 * small state machine:
 * <ol>
 *   <li>{@code PENDING} - a scan was requested; wait a beat so a burst of eats coalesces into one
 *       scan and the server has time to apply the just-eaten effect, then send {@code /effect};</li>
 *   <li>{@code AWAITING_OPEN} - the server's menu is intercepted at {@code setScreen} (see the mixin)
 *       and bound without being displayed;</li>
 *   <li>{@code AWAITING_CONTENT} - once the server fills the menu's slots, parse and close.</li>
 * </ol>
 *
 * <p>Suppression is armed only for our own scan and only for a plain container screen, so a menu the
 * player opens themselves - including a manual {@code /effect} - is never swallowed.
 */
public final class EffectScanController {
    private static final Logger LOGGER = ModLog.getLogger(EffectScanController.class);

    private static final String EFFECT_COMMAND = "effect";
    /**
     * ~2.5s: the trigger fires at use-start (right-click), so the scan must outlast the eat/drink
     * animation and the server applying the effect before it reads. Also coalesces a burst of uses.
     */
    private static final int SCAN_DELAY_TICKS = 50;
    private static final int OPEN_TIMEOUT_TICKS = 60;
    private static final int CONTENT_TIMEOUT_TICKS = 40;

    private enum State { IDLE, PENDING, AWAITING_OPEN, AWAITING_CONTENT }

    private static State state = State.IDLE;
    private static int timer = 0;
    private static int capturedContainerId = -1;

    private EffectScanController() {
    }

    /** Requests a scan after the normal debounce delay; called when a Minebox consumable is used. */
    public static void requestScan() {
        if (state == State.IDLE || state == State.PENDING) {
            state = State.PENDING;
            timer = SCAN_DELAY_TICKS;
        }
        // A scan already in flight will read the new effect too, so nothing to do otherwise.
    }

    /** Requests a scan on the next tick, skipping the debounce; used by the test command. */
    public static void forceScan() {
        state = State.PENDING;
        timer = 1;
    }

    /** Whether the {@code setScreen} mixin should swallow {@code screen} for an in-flight scan. */
    public static boolean shouldSuppress(Screen screen) {
        return state == State.AWAITING_OPEN
                && screen instanceof AbstractContainerScreen<?>
                && !(screen instanceof InventoryScreen);
    }

    /** Binds the swallowed menu so the server's content packet still populates it, without showing it. */
    public static void onScreenSuppressed(AbstractContainerScreen<?> screen) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            reset();
            return;
        }
        AbstractContainerMenu menu = screen.getMenu();
        player.containerMenu = menu;
        capturedContainerId = menu.containerId;
        state = State.AWAITING_CONTENT;
        timer = CONTENT_TIMEOUT_TICKS;
    }

    public static void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            if (state != State.IDLE) {
                reset();
            }
            return;
        }

        switch (state) {
            case IDLE -> { }
            case PENDING -> tickPending(client);
            case AWAITING_OPEN -> {
                if (--timer <= 0) {
                    reset(); // the menu never opened
                }
            }
            case AWAITING_CONTENT -> tickAwaitingContent(client, player);
        }
    }

    private static void tickPending(Minecraft client) {
        if (--timer > 0) {
            return;
        }
        // Only fire when the player isn't in a screen, so we don't fight or swallow their own menu.
        if (client.getConnection() == null || client.gui.screen() != null) {
            reset();
            return;
        }
        client.getConnection().sendCommand(EFFECT_COMMAND);
        state = State.AWAITING_OPEN;
        timer = OPEN_TIMEOUT_TICKS;
    }

    private static void tickAwaitingContent(Minecraft client, LocalPlayer player) {
        if (client.gui.screen() != null) {
            // The player opened something; abandon the scan and hand the menu back.
            closeBoundMenu(client, player);
            reset();
            return;
        }
        if (player.containerMenu == null || player.containerMenu.containerId != capturedContainerId) {
            reset(); // the menu we bound is gone
            return;
        }
        if (hasContainerContent(player)) {
            List<ActiveConsumable> effects = EffectMenuParser.parse(player.containerMenu, player.getInventory());
            ActiveEffectsStore.update(effects);
            closeBoundMenu(client, player);
            reset();
            LOGGER.debug("Silent effect scan captured {} active effect(s)", effects.size());
        } else if (--timer <= 0) {
            closeBoundMenu(client, player);
            reset();
        }
    }

    private static boolean hasContainerContent(LocalPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        for (Slot slot : menu.slots) {
            if (slot.container != player.getInventory() && !slot.getItem().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void closeBoundMenu(Minecraft client, LocalPlayer player) {
        if (client.getConnection() != null && capturedContainerId >= 0) {
            client.getConnection().send(new ServerboundContainerClosePacket(capturedContainerId));
        }
        player.containerMenu = player.inventoryMenu;
    }

    private static void reset() {
        state = State.IDLE;
        timer = 0;
        capturedContainerId = -1;
    }
}
