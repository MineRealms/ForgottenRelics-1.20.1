package dev.tc4port.forgottenrelics.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/** Velocity applied to the local player (source {@code PlayerMotionUpdateMessage}). */
public record PlayerMotionPayload(double x, double y, double z) {

    public static void encode(PlayerMotionPayload message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x());
        buffer.writeDouble(message.y());
        buffer.writeDouble(message.z());
    }

    public static PlayerMotionPayload decode(FriendlyByteBuf buffer) {
        return new PlayerMotionPayload(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(PlayerMotionPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = dev.tc4port.forgottenrelics.client.FRClientHooks.localPlayer();
            if (player != null) {
                player.setDeltaMovement(message.x(), message.y(), message.z());
                player.hurtMarked = true;
            }
        });
        context.setPacketHandled(true);
    }
}
