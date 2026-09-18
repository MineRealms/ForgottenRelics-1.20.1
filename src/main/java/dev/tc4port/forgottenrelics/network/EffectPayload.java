package dev.tc4port.forgottenrelics.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Consolidated client effect message. The source mod shipped more than ten
 * one-purpose particle packets; on 1.20.1 they collapse into one typed
 * payload, still fully data driven.
 */
public record EffectPayload(EffectType type, double x, double y, double z,
                            double targetX, double targetY, double targetZ,
                            int color, float scale, int count) {

    public enum EffectType {
        PORTAL_TRACE,
        BURST,
        APOTHEOSIS,
        LUNAR_BURST,
        LUNAR_FLARES,
        INFERNAL,
        VOID,
        TELEKINESIS,
        BANISHMENT,
        GUARDIAN_VANISH,
        SHINY,
        SPARKLE
    }

    public static EffectPayload at(EffectType type, double x, double y, double z, int color, float scale, int count) {
        return new EffectPayload(type, x, y, z, 0.0D, 0.0D, 0.0D, color, scale, count);
    }

    public static EffectPayload towards(EffectType type, double x, double y, double z,
                                        double targetX, double targetY, double targetZ, int color, float scale, int count) {
        return new EffectPayload(type, x, y, z, targetX, targetY, targetZ, color, scale, count);
    }

    public static void encode(EffectPayload message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.type().ordinal());
        buffer.writeDouble(message.x());
        buffer.writeDouble(message.y());
        buffer.writeDouble(message.z());
        buffer.writeDouble(message.targetX());
        buffer.writeDouble(message.targetY());
        buffer.writeDouble(message.targetZ());
        buffer.writeInt(message.color());
        buffer.writeFloat(message.scale());
        buffer.writeVarInt(message.count());
    }

    public static EffectPayload decode(FriendlyByteBuf buffer) {
        EffectType type = EffectType.values()[buffer.readVarInt()];
        return new EffectPayload(type,
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readInt(), buffer.readFloat(), buffer.readVarInt());
    }

    public static void handle(EffectPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> dev.tc4port.forgottenrelics.client.FRClientEffects.spawn(message));
        context.setPacketHandled(true);
    }
}
