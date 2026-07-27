package fr.silv.mixin.client;

import fr.silv.effects.EffectScanController;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Swallows the effects menu opened by a silent scan so it never appears on screen.
 *
 * <p>{@code Gui.setScreen} is the single choke point every screen change funnels through - the
 * server's menu (via {@code MenuScreens}) and any {@code setScreenAndShow} both call it, after the
 * menu has already been bound to the player. When {@link EffectScanController} is mid-scan it keeps
 * that bound menu (so the server's content packet still fills it) and cancels the display. Every
 * other call - including the menu a player opens with a manual {@code /effect} - passes through.
 */
@Mixin(Gui.class)
public class GuiSetScreenMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void mineboxtools$suppressSilentEffectScreen(Screen screen, CallbackInfo ci) {
        if (EffectScanController.shouldSuppress(screen)) {
            EffectScanController.onScreenSuppressed((AbstractContainerScreen<?>) screen);
            ci.cancel();
        }
    }
}
