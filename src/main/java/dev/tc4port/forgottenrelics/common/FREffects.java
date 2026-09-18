package dev.tc4port.forgottenrelics.common;

import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * Server-side effect senders replacing {@code SuperpositionHandler.imposeBurst}
 * and the friends.
 */
public final class FREffects {

    private FREffects() {
    }

    /** Source {@code imposeBurst}. */
    public static void burst(Level level, double x, double y, double z, float scale) {
        if (level instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, x, y, z, 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.BURST, x, y, z, 0, scale, 24));
        }
    }

    /** Particle blast for discarded/voided items. */
    public static void voidPulse(Level level, double x, double y, double z) {
        if (level instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, x, y, z, 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.VOID, x, y, z, 0, 1.0F, 32));
        }
    }

    /** Guardian vanish effect to nearby (or all) players. */
    public static void guardianVanish(Level level, double x, double y, double z, double radius) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (radius > 0.0D) {
            FRNetwork.sendNear(serverLevel, x, y, z, radius,
                    EffectPayload.at(EffectPayload.EffectType.GUARDIAN_VANISH, x, y, z, 0, 1.0F, 24));
        } else {
            FRNetwork.sendToAll(serverLevel,
                    EffectPayload.at(EffectPayload.EffectType.GUARDIAN_VANISH, x, y, z, 0, 1.0F, 24));
        }
    }

    /** Red banishment trail around a position. */
    public static void banishment(Level level, double x, double y, double z, int amount) {
        if (level instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, x, y, z, 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.BANISHMENT, x, y, z, 0xFF1A00, 0.35F, amount));
        }
    }

    public static void spellFailure(Level level, ServerPlayer player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.9F + (float) Math.random() * 0.1F);
    }
}
