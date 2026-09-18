package dev.tc4port.forgottenrelics.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Visual lightning discharge (source {@code LightningMessage} /
 * {@code LightningBoltMessage} / {@code ArcLightningMessage}). Purely
 * cosmetic: gameplay damage is applied server-side separately.
 */
public record LightningPayload(double x, double y, double z,
                               double targetX, double targetY, double targetZ,
                               int color, float width) {

    public static void encode(LightningPayload message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x());
        buffer.writeDouble(message.y());
        buffer.writeDouble(message.z());
        buffer.writeDouble(message.targetX());
        buffer.writeDouble(message.targetY());
        buffer.writeDouble(message.targetZ());
        buffer.writeInt(message.color());
        buffer.writeFloat(message.width());
    }

    public static LightningPayload decode(FriendlyByteBuf buffer) {
        return new LightningPayload(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readInt(), buffer.readFloat());
    }

    public static void handle(LightningPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> dev.tc4port.forgottenrelics.client.FRClientEffects.lightning(message));
        context.setPacketHandled(true);
    }
}
