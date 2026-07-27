package fr.silv.mixin.client;

import fr.silv.effects.ConsumableConsumeWatcher;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Notifies {@link ConsumableConsumeWatcher} when the local player right-clicks to use an item.
 *
 * <p>This is the interaction the client sends to the server, and on Minebox it is what actually
 * triggers a consumable's buff - the items are {@code canAlwaysEat=false} and the server applies the
 * effect on interaction, so the vanilla eat-completion path ({@code completeUsingItem}) does not fire
 * reliably (e.g. when hunger is full) and can't be used as the trigger. Firing here instead is the
 * signal that matches when the effect is granted; the scan itself waits out the server's apply delay
 * before reading, so triggering at use-start rather than use-finish is fine.
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeUseMixin {
    @Inject(method = "useItem", at = @At("HEAD"))
    private void mineboxtools$onUseItem(Player player, InteractionHand hand,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        ConsumableConsumeWatcher.onConsume(player.getItemInHand(hand));
    }
}
