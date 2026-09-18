package dev.tc4port.forgottenrelics.network;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Discord Ring key press. The client only reports the press; all authority
 * stays on the server, matching the source mod's message flow.
 */
public record DiscordKeybindPayload(boolean pressed) {

    public static void encode(DiscordKeybindPayload message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.pressed());
    }

    public static DiscordKeybindPayload decode(FriendlyByteBuf buffer) {
        return new DiscordKeybindPayload(buffer.readBoolean());
    }

    public static void handle(DiscordKeybindPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !message.pressed()) {
                return;
            }
            ForgottenRelics.LOG.debug("Discord Ring keybind received from {}", player.getGameProfile().getName());
            dev.tc4port.forgottenrelics.common.RelicAbilities.triggerDiscordRing(player);
        });
        context.setPacketHandled(true);
    }
}
