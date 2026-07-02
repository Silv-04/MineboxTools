package fr.silv.mixin.client;

import fr.silv.api.MuseumScreenRegistry;
import fr.silv.utils.MineboxItemDataUtils;
import fr.silv.utils.ModLog;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Fingerprints the museum donation screen ("give items to museum") by its slot layout rather
 * than its title. The screen's title text is not exposed through any reachable vanilla channel
 * (observed to always be a generic fallback both on the Screen and in the raw open-screen
 * packet), so this instead reads real item NBT from {@link ClientboundContainerSetContentPacket}:
 * slot 0 is a "back" button (unlike the "Musée" browse screen, whose slot 0 is a plain
 * decorative pane), and the donation area is bordered by a distinctive block of
 * "orange_gui_empty" decorative panes not present elsewhere.
 *
 * <p>The close side of detection lives in {@code MineboxToolsClient} via {@code ScreenEvents.remove}
 * instead of a close packet: {@code ClientboundContainerClosePacket} is server-initiated and does
 * not fire for a player voluntarily closing their own screen.
 */
@Mixin(ClientPacketListener.class)
public abstract class MuseumScreenDetectionMixin {
    private static final Logger LOGGER = ModLog.getLogger(MuseumScreenDetectionMixin.class);
    private static final String MENU_TYPE_PATH = "PublicBukkitValues.mythicmobs:type";
    private static final String BACK_BUTTON_TYPE = "back";
    private static final String DONATION_BORDER_TYPE = "orange_gui_empty";
    private static final int MIN_DONATION_BORDER_PANES = 10;

    @Inject(method = "handleContainerContent", at = @At("HEAD"))
    private void onContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        List<ItemStack> items = packet.items();
        boolean hasBackButtonAtSlotZero = !items.isEmpty() && matchesMenuType(items.get(0), BACK_BUTTON_TYPE);

        long donationBorderPanes = items.stream()
                .filter(stack -> matchesMenuType(stack, DONATION_BORDER_TYPE))
                .count();

        boolean isMuseumDonationScreen = hasBackButtonAtSlotZero && donationBorderPanes >= MIN_DONATION_BORDER_PANES;
        MuseumScreenRegistry.markScreen(packet.containerId(), isMuseumDonationScreen);
        LOGGER.info("[fingerprint] ContainerContent containerId={}, slot0=back:{}, orangePanes={}, isMuseumDonationScreen={}",
                packet.containerId(), hasBackButtonAtSlotZero, donationBorderPanes, isMuseumDonationScreen);
    }

    private static boolean matchesMenuType(ItemStack stack, String expectedType) {
        if (stack.isEmpty()) {
            return false;
        }
        return MineboxItemDataUtils.getCustomData(stack)
                .flatMap(nbt -> MineboxItemDataUtils.getStringValue(nbt, MENU_TYPE_PATH))
                .filter(expectedType::equals)
                .isPresent();
    }
}
