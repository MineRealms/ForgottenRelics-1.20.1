package dev.tc4port.forgottenrelics.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/** Authoritative entity motion sync for held-target abilities (source {@code EntityMotionMessage}). */
public record EntityMotionPayload(int entityId, double x, double y, double z, boolean motionless) {

    public static void encode(EntityMotionPayload message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId());
        buffer.writeDouble(message.x());
        buffer.writeDouble(message.y());
        buffer.writeDouble(message.z());
        buffer.writeBoolean(message.motionless());
    }

    public static EntityMotionPayload decode(FriendlyByteBuf buffer) {
        return new EntityMotionPayload(buffer.readVarInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean());
    }

    public static void handle(EntityMotionPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Entity entity = dev.tc4port.forgottenrelics.client.FRClientHooks.localPlayerEntity(message.entityId());
            if (entity == null) {
                return;
            }
            if (message.motionless()) {
                entity.fallDistance = 0.0F;
            }
            entity.setDeltaMovement(message.x(), message.y(), message.z());
            entity.hurtMarked = true;
            if (entity instanceof Player player) {
                player.setDeltaMovement(message.x(), message.y(), message.z());
            }
        });
        context.setPacketHandled(true);
    }
}
