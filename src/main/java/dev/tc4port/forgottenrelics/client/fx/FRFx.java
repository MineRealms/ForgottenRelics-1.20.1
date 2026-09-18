package dev.tc4port.forgottenrelics.client.fx;

import dev.tc4port.forgottenrelics.client.particle.FRSparkleParticle;
import dev.tc4port.forgottenrelics.client.particle.FRWispParticle;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.20.1 counterpart of Botania's client FX proxy surface used by the source
 * mod: {@code sparkleFX}, {@code wispFX} plus the global
 * {@code setWispFXDistanceLimit / setWispFXDepthTest / setSparkleFXNoClip /
 * setSparkleFXCorrupt} toggles.
 */
@OnlyIn(Dist.CLIENT)
public final class FRFx {

    private static boolean wispDistanceLimit = true;
    private static boolean wispDepthTest = true;
    private static boolean sparkleNoClip;
    private static boolean sparkleCorrupt;

    private FRFx() {
    }

    public static void setWispDistanceLimit(boolean value) {
        wispDistanceLimit = value;
    }

    public static void setWispDepthTest(boolean value) {
        wispDepthTest = value;
    }

    public static void setSparkleNoClip(boolean value) {
        sparkleNoClip = value;
    }

    public static void setSparkleCorrupt(boolean value) {
        sparkleCorrupt = value;
    }

    // ------------------------------------------------------------------
    // sparkles
    // ------------------------------------------------------------------

    public static void sparkle(ClientLevel level, double x, double y, double z, float red, float green, float blue, float size, int multiplier) {
        sparkle(level, x, y, z, red, green, blue, size, multiplier, false);
    }

    public static void sparkle(ClientLevel level, double x, double y, double z, float red, float green, float blue, float size, int multiplier, boolean fake) {
        if (!doParticle(level)) {
            return;
        }
        FRSparkleParticle particle = new FRSparkleParticle(level, x, y, z, size, red, green, blue, multiplier);
        particle.fake = fake;
        particle.noClip = fake || sparkleNoClip;
        particle.corrupt = sparkleCorrupt;
        Minecraft.getInstance().particleEngine.add(particle);
    }

    public static void sparkleMoving(ClientLevel level, double x, double y, double z, double mx, double my, double mz,
                                     float red, float green, float blue, float size, int multiplier) {
        if (!doParticle(level)) {
            return;
        }
        FRSparkleParticle particle = new FRSparkleParticle(level, x, y, z, size, red, green, blue, multiplier);
        particle.noClip = sparkleNoClip;
        particle.corrupt = sparkleCorrupt;
        particle.setParticleMotion(mx, my, mz);
        Minecraft.getInstance().particleEngine.add(particle);
    }

    // ------------------------------------------------------------------
    // wisps
    // ------------------------------------------------------------------

    public static void wisp(ClientLevel level, double x, double y, double z,
                            float red, float green, float blue, float size,
                            double mx, double my, double mz, float maxAgeMul) {
        if (!spawnAllowed(level, x, y, z)) {
            return;
        }
        FRWispParticle particle = new FRWispParticle(level, x, y, z, size, red, green, blue, wispDistanceLimit, wispDepthTest, maxAgeMul);
        particle.setParticleMotion(mx, my, mz);
        Minecraft.getInstance().particleEngine.add(particle);
    }

    /** Source {@code wispFX3}: drift to a destination over the particle's life. */
    public static void wispTo(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ,
                              float red, float green, float blue, float size) {
        if (!spawnAllowed(level, x, y, z)) {
            return;
        }
        FRWispParticle particle = new FRWispParticle(level, x, y, z, size, red, green, blue, wispDistanceLimit, wispDepthTest, 1.0F);
        particle.setDestination(targetX, targetY, targetZ);
        Minecraft.getInstance().particleEngine.add(particle);
    }

    /** Source {@code wispFX4} / {@code wispFXEG}: follow an entity. */
    public static void wispFollow(ClientLevel level, double x, double y, double z, Entity target,
                                  float red, float green, float blue, float size) {
        if (!spawnAllowed(level, x, y, z)) {
            return;
        }
        FRWispParticle particle = new FRWispParticle(level, x, y, z, size, red, green, blue, wispDistanceLimit, wispDepthTest, 1.0F);
        particle.follow(target);
        Minecraft.getInstance().particleEngine.add(particle);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static boolean spawnAllowed(ClientLevel level, double x, double y, double z) {
        if (!doParticle(level)) {
            return false;
        }
        if (wispDistanceLimit) {
            Entity camera = Minecraft.getInstance().getCameraEntity();
            if (camera == null) {
                return false;
            }
            boolean fancy = Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST;
            double visibleDistance = fancy ? 50.0D : 25.0D;
            if (camera.distanceToSqr(x, y, z) > visibleDistance * visibleDistance) {
                return false;
            }
        }
        return true;
    }

    private static boolean doParticle(ClientLevel level) {
        if (level == null) {
            return false;
        }
        return Minecraft.getInstance().options.particles().get() != ParticleStatus.MINIMAL;
    }
}
