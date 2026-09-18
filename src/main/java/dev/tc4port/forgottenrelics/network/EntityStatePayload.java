package dev.tc4port.forgottenrelics.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

/** Authoritative position/rotation sync for held targets (source {@code EntityStateMessage}). */
public record EntityStatePayload(int entityId, double x, double y, double z,
                                 float yaw, float pitch, float yawHead, boolean motionless) {

    public static void encode(EntityStatePayload message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId());
        buffer.writeDouble(message.x());
        buffer.writeDouble(message.y());
        buffer.writeDouble(message.z());
        buffer.writeFloat(message.yaw());
        buffer.writeFloat(message.pitch());
        buffer.writeFloat(message.yawHead());
        buffer.writeBoolean(message.motionless());
    }

    public static EntityStatePayload decode(FriendlyByteBuf buffer) {
        return new EntityStatePayload(buffer.readVarInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    public static void handle(EntityStatePayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Entity entity = dev.tc4port.forgottenrelics.client.FRClientHooks.localPlayerEntity(message.entityId());
            if (!(entity instanceof LivingEntity living) || living == dev.tc4port.forgottenrelics.client.FRClientHooks.localPlayer()) {
                return;
            }
            living.moveTo(message.x(), message.y(), message.z(), message.yaw(), message.pitch());
            living.setYHeadRot(message.yawHead());
            if (message.motionless()) {
                double y = living.getDeltaMovement().y;
                if (y > 0.0D) {
                    y = 0.0D;
                }
                living.setDeltaMovement(0.0D, y, 0.0D);
            }
            living.hurtMarked = true;
        });
        context.setPacketHandled(true);
    }
}
