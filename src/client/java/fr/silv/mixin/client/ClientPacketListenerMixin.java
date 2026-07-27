package fr.silv.mixin.client;

import fr.silv.effects.EffectScanController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Silently captures the effects menu for a scan by intercepting its packets, so it is never turned
 * into a screen the player sees.
 *
 * <p>When {@link EffectScanController} is mid-scan and the open-screen packet's title is the effects
 * menu, the packet is cancelled (no menu or screen is created) and its window id remembered; the
 * matching content packet is then read for its items and cancelled too. The menu is recognised by
 * title, so a menu the player opens themselves never matches and is left completely alone.
 *
 * <p>Each handler runs first on the network thread (which reschedules to the main thread) and then on
 * the main thread; the {@code isSameThread} guard makes the interception happen once, on the client
 * thread, where the store is updated.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleOpenScreen", at = @At("HEAD"), cancellable = true)
    private void mineboxtools$interceptEffectMenuOpen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) {
            return;
        }
        if (EffectScanController.shouldInterceptOpen(packet.getTitle(), packet.getContainerId())) {
            EffectScanController.onOpenIntercepted(packet.getContainerId());
            ci.cancel();
        }
    }

    @Inject(method = "handleContainerContent", at = @At("HEAD"), cancellable = true)
    private void mineboxtools$interceptEffectMenuContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) {
            return;
        }
        if (EffectScanController.shouldInterceptContent(packet.containerId())) {
            EffectScanController.onContentIntercepted(packet.items());
            ci.cancel();
        }
    }
}
