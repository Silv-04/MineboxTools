package fr.silv.mixin.client;

import fr.silv.items.ItemHighlightHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenHighlightMixin {
    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onAfterRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ItemHighlightHandler.render((AbstractContainerScreen<?>) (Object) this, context, leftPos, topPos);
    }
}
