package dev.tc4port.forgottenrelics.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Thaumcraft-style status notification (source {@code NotificationMessage}).
 * The 1.7.10 build pushed these into TC's notification overlay; 1.20.1 uses the
 * vanilla overlay line, which is the closest built-in equivalent.
 */
public record NotificationPayload(int type) {

    public static final int FATE_COOLDOWN_OVER = 1;
    public static final int OVERDAMAGE_BLOCK = 2;

    public static void encode(NotificationPayload message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.type());
    }

    public static NotificationPayload decode(FriendlyByteBuf buffer) {
        return new NotificationPayload(buffer.readVarInt());
    }

    public static void handle(NotificationPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> dev.tc4port.forgottenrelics.client.FRClientEffects.notification(message.type()));
        context.setPacketHandled(true);
    }
}
