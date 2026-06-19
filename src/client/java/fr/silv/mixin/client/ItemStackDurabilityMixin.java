package fr.silv.mixin.client;

import fr.silv.ModConfig;
import fr.silv.items.DurabilityBarHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalDouble;

@Mixin(ItemStack.class)
public class ItemStackDurabilityMixin {

    @Inject(method = "isBarVisible", at = @At("RETURN"), cancellable = true)
    private void onIsBarVisible(CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.isEnabled(ModConfig.FeatureFlag.DURABILITY)) return;
        ItemStack self = (ItemStack) (Object) this;
        OptionalDouble fraction = DurabilityBarHandler.computeDurabilityFraction(self);
        if (fraction.isPresent()) {
            cir.setReturnValue(fraction.getAsDouble() < 1.0);
        }
    }

    @Inject(method = "getBarWidth", at = @At("RETURN"), cancellable = true)
    private void onGetBarWidth(CallbackInfoReturnable<Integer> cir) {
        if (!ModConfig.isEnabled(ModConfig.FeatureFlag.DURABILITY)) return;
        ItemStack self = (ItemStack) (Object) this;
        DurabilityBarHandler.computeDurabilityFraction(self)
                .ifPresent(fraction -> cir.setReturnValue(Math.round((float) fraction * 13)));
    }

    @Inject(method = "getBarColor", at = @At("RETURN"), cancellable = true)
    private void onGetBarColor(CallbackInfoReturnable<Integer> cir) {
        if (!ModConfig.isEnabled(ModConfig.FeatureFlag.DURABILITY)) return;
        ItemStack self = (ItemStack) (Object) this;
        DurabilityBarHandler.computeDurabilityFraction(self)
                .ifPresent(fraction -> cir.setReturnValue(Mth.hsvToArgb((float) fraction / 3.0F, 1.0F, 1.0F, 255)));
    }
}
