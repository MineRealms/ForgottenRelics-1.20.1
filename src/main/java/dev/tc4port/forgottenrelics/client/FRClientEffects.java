package dev.tc4port.forgottenrelics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.LightningPayload;
import dev.tc4port.forgottenrelics.network.NotificationPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * Client-side effect rendering. Maps the consolidated {@link EffectPayload}
 * onto vanilla particle primitives; this replaces the source mod's Botania
 * {@code wispFX} calls and its dedicated FX classes.
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
                    double px = message.x() + (message.targetX() - message.x()) * t + (Math.random() - 0.5D) * scale;
                    double py = message.y() + (message.targetY() - message.y()) * t + (Math.random() - 0.5D) * scale;
                    double pz = message.z() + (message.targetZ() - message.z()) * t + (Math.random() - 0.5D) * scale;
                    level.addParticle(ParticleTypes.PORTAL, px, py, pz, 0.0D, 0.0D, 0.0D);
                }
            }
            case BURST, LUNAR_BURST -> burst(level, message, ParticleTypes.CRIT, ParticleTypes.END_ROD, count);
            case APOTHEOSIS -> burst(level, message, ParticleTypes.END_ROD, ParticleTypes.FLASH, count / 2 + 1);
            case LUNAR_FLARES -> burst(level, message, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.END_ROD, count);
            case INFERNAL -> burst(level, message, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SMOKE, count);
            case VOID -> burst(level, message, ParticleTypes.SCULK_SOUL, ParticleTypes.PORTAL, count);
            case TELEKINESIS -> burst(level, message, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.ENCHANT, count);
            case BANISHMENT -> {
                ParticleOptions dust = new DustParticleOptions(colorVector(message.color(), 0.9F, 0.1F, 0.0F), scale);
                for (int i = 0; i < count; i++) {
                    double px = message.x() + (Math.random() - 0.5D) * 8.0D;
                    double py = message.y() + (Math.random() - 0.5D) * 8.0D;
                    double pz = message.z() + (Math.random() - 0.5D) * 8.0D;
                    Vec3 pull = new Vec3(message.x() - px, message.y() - py, message.z() - pz).scale(0.08D);
                    level.addParticle(dust, px, py, pz, pull.x, pull.y, pull.z);
                }
            }
            case GUARDIAN_VANISH -> burst(level, message, ParticleTypes.END_ROD, ParticleTypes.CLOUD, count);
            case SHINY -> burst(level, message, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.END_ROD, count);
            case SPARKLE -> {
                ParticleOptions dust = new DustParticleOptions(colorVector(message.color(), 0.5F, 0.8F, 1.0F), scale);
                for (int i = 0; i < count; i++) {
                    level.addParticle(dust,
                            message.x() + (Math.random() - 0.5D) * scale,
                            message.y() + (Math.random() - 0.5D) * scale,
                            message.z() + (Math.random() - 0.5D) * scale,
                            0.0D, 0.02D, 0.0D);
                }
            }
        }
    }

    private static void burst(ClientLevel level, EffectPayload message, ParticleOptions primary, ParticleOptions secondary, int count) {
        for (int i = 0; i < count; i++) {
            double vx = (Math.random() - 0.5D) * 0.3D;
            double vy = (Math.random() - 0.5D) * 0.3D;
            double vz = (Math.random() - 0.5D) * 0.3D;
            level.addParticle(primary, message.x(), message.y(), message.z(), vx, vy, vz);
            if (secondary != null) {
                level.addParticle(secondary, message.x(), message.y(), message.z(), vx * 0.5D, vy * 0.5D, vz * 0.5D);
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

    private static Vector3f colorVector(int color, float fallbackR, float fallbackG, float fallbackB) {
        if (color == 0) {
            return new Vector3f(fallbackR, fallbackG, fallbackB);
        }
        return new Vector3f(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F);
    }

    @SuppressWarnings("unused")
    private static void unused(PoseStack stack) {
    }
}
