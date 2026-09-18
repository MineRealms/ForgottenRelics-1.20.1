package dev.tc4port.forgottenrelics.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Telekinesis Tome left-click (source {@code TelekinesisAttackMessage}). */
public record TelekinesisAttackPayload(boolean attack) {

    public static void encode(TelekinesisAttackPayload message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.attack());
    }

    public static TelekinesisAttackPayload decode(FriendlyByteBuf buffer) {
        return new TelekinesisAttackPayload(buffer.readBoolean());
    }

    public static void handle(TelekinesisAttackPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && message.attack()) {
                dev.tc4port.forgottenrelics.common.RelicAbilities.triggerTelekinesisAttack(player);
            }
        });
        context.setPacketHandled(true);
    }
}
