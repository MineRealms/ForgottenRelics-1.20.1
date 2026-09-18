package dev.tc4port.forgottenrelics.client;

import dev.tc4port.forgottenrelics.client.fx.FRFx;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.LightningPayload;
import dev.tc4port.forgottenrelics.network.NotificationPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side effect rendering. Uses the ported Botania wisp/sparkle particles
 * (see {@link FRFx}), which is what the source mod's effect packets ended up
 * spawning on the 1.7.10 client.
 */
@OnlyIn(Dist.CLIENT)
public final class FRClientEffects {

    private FRClientEffects() {
    }

    public static void notification(int type) {
        String key = switch (type) {
            case NotificationPayload.FATE_COOLDOWN_OVER -> "notification.fate_cooldown_over";
            case NotificationPayload.OVERDAMAGE_BLOCK -> "notification.overdamage_block";
            default -> null;
        };
        if (key != null) {
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(key), false);
        }
    }

    public static void spawn(EffectPayload message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        int count = Math.max(1, message.count());
        float scale = message.scale() > 0.0F ? message.scale() : 1.0F;

        switch (message.type()) {
            case PORTAL_TRACE -> {
                for (int i = 0; i < count; i++) {
                    double t = i / (double) count;
                    double px = message.x() + (message.targetX() - message.x()) * t;
                    double py = message.y() + (message.targetY() - message.y()) * t;
                    double pz = message.z() + (message.targetZ() - message.z()) * t;
                    FRFx.sparkle(level, px + jitter(scale), py + jitter(scale), pz + jitter(scale), 0.6F, 0.9F, 1.0F, 1.6F, 6);
                    FRFx.wisp(level, px + jitter(scale), py + jitter(scale), pz + jitter(scale), 0.5F, 0.7F, 1.0F, 0.5F,
                            0.0D, 0.0D, 0.0D, 1.0F);
                }
            }
            case BURST -> {
                for (int i = 0; i < count / 2 + 4; i++) {
                    FRFx.sparkleMoving(level,
                            message.x() + jitter(scale * 0.6D), message.y() + jitter(scale * 0.6D), message.z() + jitter(scale * 0.6D),
                            jitter(0.3D), jitter(0.3D), jitter(0.3D),
                            0.9F, 0.9F, 1.0F, 2.0F, 4);
                }
                for (int i = 0; i < count / 2; i++) {
                    FRFx.wisp(level, message.x() + jitter(scale), message.y() + jitter(scale), message.z() + jitter(scale),
                            0.8F, 0.8F, 1.0F, 0.6F, jitter(0.05D), jitter(0.05D), jitter(0.05D), 1.0F);
                }
            }
            case APOTHEOSIS -> {
                for (int i = 0; i < count; i++) {
                    FRFx.sparkle(level, message.x() + jitter(scale), message.y() + jitter(scale), message.z() + jitter(scale),
                            1.0F, 0.85F, 0.3F, 2.4F, 5);
                }
                for (int i = 0; i < count / 2; i++) {
                    FRFx.wisp(level, message.x() + jitter(scale), message.y() + jitter(scale), message.z() + jitter(scale),
                            1.0F, 0.9F, 0.4F, 0.8F, jitter(0.08D), jitter(0.08D), jitter(0.08D), 1.0F);
                }
            }
            case LUNAR_BURST -> {
                for (int i = 0; i < count; i++) {
                    FRFx.sparkleMoving(level,
                            message.x() + jitter(scale), message.y() + jitter(scale), message.z() + jitter(scale),
                            jitter(0.2D), jitter(0.2D), jitter(0.2D),
                            0.75F, 0.9F, 1.0F, 2.2F, 4);
                }
            }
            case LUNAR_FLARES -> {
                for (int i = 0; i < count; i++) {
                    FRFx.sparkle(level, message.x() + jitter(scale * 1.5D), message.y() + jitter(scale * 1.5D), message.z() + jitter(scale * 1.5D),
                            0.65F, 0.85F, 1.0F, 1.8F, 5);
                }
                for (int i = 0; i < count / 3; i++) {
                    FRFx.wisp(level, message.x() + jitter(scale * 1.5D), message.y() + jitter(scale * 1.5D), message.z() + jitter(scale * 1.5D),
                            0.7F, 0.9F, 1.0F, 0.7F, jitter(0.06D), jitter(0.06D), jitter(0.06D), 1.2F);
                }
            }
            case INFERNAL -> {
                for (int i = 0; i < count / 2; i++) {
                    FRFx.wisp(level, message.x() + jitter(2.0D), message.y() + jitter(2.0D), message.z() + jitter(2.0D),
                            0.9F, 0.25F, 0.1F, 0.8F, jitter(0.1D), 0.05D, jitter(0.1D), 1.0F);
                }
                for (int i = 0; i < count / 2; i++) {
                    FRFx.sparkle(level, message.x() + jitter(1.5D), message.y() + jitter(1.5D), message.z() + jitter(1.5D),
                            1.0F, 0.4F, 0.1F, 2.0F, 4);
                }
            }
            case VOID -> {
                for (int i = 0; i < count; i++) {
                    FRFx.wispTo(level,
                            message.x() + jitter(3.0D), message.y() + jitter(3.0D), message.z() + jitter(3.0D),
                            message.x(), message.y(), message.z(),
                            0.45F, 0.1F, 0.7F, 0.7F);
                }
            }
            case TELEKINESIS -> {
                for (int i = 0; i < count; i++) {
                    FRFx.sparkle(level, message.x() + jitter(1.0D), message.y() + jitter(1.0D), message.z() + jitter(1.0D),
                            0.35F, 0.9F, 0.9F, 1.8F, 4);
                }
            }
            case BANISHMENT -> {
                for (int i = 0; i < count; i++) {
                    double px = message.x() + jitter(4.0D);
                    double py = message.y() + jitter(4.0D);
                    double pz = message.z() + jitter(4.0D);
                    Vec3 pull = new Vec3(message.x() - px, message.y() - py, message.z() - pz).scale(0.08D);
                    FRFx.wisp(level, px, py, pz, 0.9F, 0.12F, 0.02F, 0.7F,
                            pull.x, pull.y, pull.z, 1.0F);
                }
            }
            case GUARDIAN_VANISH -> {
                for (int i = 0; i < count; i++) {
                    FRFx.sparkle(level, message.x() + jitter(2.0D), message.y() + jitter(2.0D), message.z() + jitter(2.0D),
                            0.95F, 0.85F, 1.0F, 2.6F, 6);
                }
            }
            case SHINY -> {
                for (int i = 0; i < count; i++) {
                    FRFx.sparkle(level, message.x() + jitter(1.0D), message.y() + jitter(1.0D), message.z() + jitter(1.0D),
                            0.9F, 0.95F, 1.0F, 1.8F, 3);
                }
                for (int i = 0; i < count / 3; i++) {
                    FRFx.wisp(level, message.x() + jitter(1.0D), message.y() + jitter(1.0D), message.z() + jitter(1.0D),
                            0.8F, 0.9F, 1.0F, 0.5F, jitter(0.03D), jitter(0.03D), jitter(0.03D), 1.0F);
                }
            }
            case SPARKLE -> {
                float red = ((message.color() >> 16) & 0xFF) / 255.0F;
                float green = ((message.color() >> 8) & 0xFF) / 255.0F;
                float blue = (message.color() & 0xFF) / 255.0F;
                if (message.color() == 0) {
                    red = 0.75F;
                    green = 0.85F;
                    blue = 1.0F;
                }
                for (int i = 0; i < count; i++) {
                    FRFx.sparkle(level, message.x() + jitter(scale), message.y() + jitter(scale), message.z() + jitter(scale),
                            red, green, blue, 2.0F, 5);
                }
                for (int i = 0; i < count / 2; i++) {
                    FRFx.wisp(level, message.x() + jitter(scale), message.y() + jitter(scale), message.z() + jitter(scale),
                            red, green, blue, 0.6F, jitter(0.06D), jitter(0.06D), jitter(0.06D), 1.0F);
                }
            }
        }
    }

    public static void lightning(LightningPayload message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Vec3 from = new Vec3(message.x(), message.y(), message.z());
        Vec3 to = new Vec3(message.targetX(), message.targetY(), message.targetZ());
        Vec3 delta = to.subtract(from);
        int steps = Math.max(4, (int) (delta.length() * 4.0D));
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3 point = from.add(delta.scale(t));
            double jitter = 0.15D;
            level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    point.x + (Math.random() - 0.5D) * jitter,
                    point.y + (Math.random() - 0.5D) * jitter,
                    point.z + (Math.random() - 0.5D) * jitter,
                    0.0D, 0.0D, 0.0D);
        }
        level.playLocalSound(message.x(), message.y(), message.z(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.35F,
                0.8F + (float) Math.random() * 0.2F, false);
    }

    private static double jitter(double scale) {
        return (Math.random() - 0.5D) * 2.0D * scale;
    }
}
