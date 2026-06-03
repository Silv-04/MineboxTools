package fr.silv.mixin.client;

import fr.silv.items.ItemHighlightHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenHighlightMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onAfterRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
        ItemHighlightHandler.render((AbstractContainerScreen<?>) (Object) this, context, accessor.getLeftPos(), accessor.getTopPos());
    }
}
