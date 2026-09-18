package dev.tc4port.forgottenrelics.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Telekinesis Tome "use" key (source {@code TelekinesisUseMessage}). */
public record TelekinesisUsePayload() {

    public static void encode(TelekinesisUsePayload message, FriendlyByteBuf buffer) {
    }

    public static TelekinesisUsePayload decode(FriendlyByteBuf buffer) {
        return new TelekinesisUsePayload();
    }

    public static void handle(TelekinesisUsePayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                dev.tc4port.forgottenrelics.common.RelicAbilities.triggerTelekinesisUse(player);
            }
        });
        context.setPacketHandled(true);
    }
}
