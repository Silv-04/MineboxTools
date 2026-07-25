package fr.silv.mixin.client;

import fr.silv.items.ItemHighlightHandler;
import fr.silv.items.MuseumHighlightHandler;
import fr.silv.hud.widget.HudWidgetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudRenderMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        for (var widget : HudWidgetManager.getWidgets()) {
            widget.keepInBounds(screenWidth, screenHeight);
            widget.render(context, client);
        }
        ItemHighlightHandler.renderHotbar(client, context);
        MuseumHighlightHandler.renderHotbar(client, context);
    }
}
