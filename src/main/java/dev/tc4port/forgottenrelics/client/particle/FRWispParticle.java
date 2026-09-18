package dev.tc4port.forgottenrelics.client.particle;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * 1.20.1 port of Botania's {@code FXWisp} (originally by Azanor).
 *
 * <p>Behavior preserved from the 1.7.10 class: fullbright, half-transparent,
 * gravity-driven drift with 0.98 damping, a triangular size curve that grows
 * to half life and shrinks afterwards, optional distance culling and optional
 * depth-ignoring rendering. Destination and entity-following movement are
 * folded in as modes.</p>
 */
public final class FRWispParticle extends TextureSheetParticle {

    private final float baseSize;
    private final int halfLife;
    private final boolean depthTest;
    @Nullable
    private Entity followTarget;
    private boolean destinationMode;

    public FRWispParticle(ClientLevel level, double x, double y, double z, float size, float red, float green, float blue,
                          boolean distanceLimit, boolean depthTest, float maxAgeMul) {
        super(level, x, y, z);
        this.sprite = FRParticleSprites.wisp();
        this.setSize(0.1F, 0.1F);
        this.gravity = 0.0F;
        this.friction = 0.98F;
        this.hasPhysics = false;
        this.lifetime = Math.max(1, (int) (28.0D / (this.random.nextDouble() * 0.3D + 0.7D) * maxAgeMul));
        this.halfLife = Math.max(1, this.lifetime / 2);
        this.baseSize = 0.1F * 5.0F * size;
        this.quadSize = this.baseSize;
        float resolvedRed = red == 0.0F ? 1.0F : red;
        this.setColor(resolvedRed, green, blue);
        this.setAlpha(0.5F);
        this.depthTest = depthTest;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
    }

    public void setParticleMotion(double mx, double my, double mz) {
        this.xd = mx;
        this.yd = my;
        this.zd = mz;
    }

    /** Source {@code wispFX3}: the arguments are the full displacement to the destination. */
    public void setDestination(double targetX, double targetY, double targetZ) {
        double divisor = Math.max(1, this.lifetime);
        this.xd = (targetX - this.x) / divisor;
        this.yd = (targetY - this.y) / divisor;
        this.zd = (targetZ - this.z) / divisor;
        this.destinationMode = true;
    }

    /** Source {@code wispFX4}/{@code wispFXEG}: home onto an entity. */
    public void follow(Entity target) {
        this.followTarget = target;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.depthTest ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : FRParticleRenderTypes.DEPTH_IGNORING;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd -= 0.04D * this.gravity;

        if (this.followTarget != null) {
            this.xd *= 0.985D;
            this.yd *= 0.985D;
            this.zd *= 0.985D;
            double deltaX = this.followTarget.getX() - this.x;
            double deltaY = this.followTarget.getY() + this.followTarget.getBbHeight() * 0.5D - this.y;
            double deltaZ = this.followTarget.getZ() - this.z;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            if (distance > 0.0D) {
                this.xd += deltaX / distance * 0.2D;
                this.yd += deltaY / distance * 0.2D;
                this.zd += deltaZ / distance * 0.2D;
            }
            this.xd = Mth.clamp(this.xd, -0.2D, 0.2D);
            this.yd = Mth.clamp(this.yd, -0.2D, 0.2D);
            this.zd = Mth.clamp(this.zd, -0.2D, 0.2D);
        }

        // The source class moved via raw position writes (noClip), not entity collision.
        this.setPos(this.x + this.xd, this.y + this.yd, this.z + this.zd);

        if (!this.destinationMode) {
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
        }

        float ageScale = this.age / (float) this.halfLife;
        if (ageScale > 1.0F) {
            ageScale = 2.0F - ageScale;
        }
        this.quadSize = this.baseSize * Math.max(0.0F, ageScale);
    }
}
