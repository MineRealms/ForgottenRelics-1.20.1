package dev.tc4port.forgottenrelics.network;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 1.20.1 replacement for the source mod's 1.7.10 {@code SimpleNetworkWrapper}.
 *
 * <p>Message classes are consolidated where the platform now syncs the same
 * information for free: item use durations, swings, research completion and
 * plain chat text all travel through vanilla mechanisms, so only real custom
 * gameplay/effect traffic gets a payload.</p>
 */
public final class FRNetwork {

    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ForgottenRelics.id("main"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals);

    private FRNetwork() {
    }

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(DiscordKeybindPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DiscordKeybindPayload::encode)
                .decoder(DiscordKeybindPayload::decode)
                .consumerMainThread(DiscordKeybindPayload::handle)
                .add();

        CHANNEL.messageBuilder(TelekinesisUsePayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TelekinesisUsePayload::encode)
                .decoder(TelekinesisUsePayload::decode)
                .consumerMainThread(TelekinesisUsePayload::handle)
                .add();

        CHANNEL.messageBuilder(TelekinesisAttackPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TelekinesisAttackPayload::encode)
                .decoder(TelekinesisAttackPayload::decode)
                .consumerMainThread(TelekinesisAttackPayload::handle)
                .add();

        CHANNEL.messageBuilder(EntityMotionPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EntityMotionPayload::encode)
                .decoder(EntityMotionPayload::decode)
                .consumerMainThread(EntityMotionPayload::handle)
                .add();

        CHANNEL.messageBuilder(EntityStatePayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EntityStatePayload::encode)
                .decoder(EntityStatePayload::decode)
                .consumerMainThread(EntityStatePayload::handle)
                .add();

        CHANNEL.messageBuilder(PlayerMotionPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayerMotionPayload::encode)
                .decoder(PlayerMotionPayload::decode)
                .consumerMainThread(PlayerMotionPayload::handle)
                .add();

        CHANNEL.messageBuilder(NotificationPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NotificationPayload::encode)
                .decoder(NotificationPayload::decode)
                .consumerMainThread(NotificationPayload::handle)
                .add();

        CHANNEL.messageBuilder(EffectPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EffectPayload::encode)
                .decoder(EffectPayload::decode)
                .consumerMainThread(EffectPayload::handle)
                .add();

        CHANNEL.messageBuilder(LightningPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LightningPayload::encode)
                .decoder(LightningPayload::decode)
                .consumerMainThread(LightningPayload::handle)
                .add();
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToAll(ServerLevel level, Object message) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }

    public static void sendNear(ServerLevel level, double x, double y, double z, double radius, Object message) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, radius, level.dimension())), message);
    }
}
