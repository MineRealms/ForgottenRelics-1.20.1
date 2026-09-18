package dev.tc4port.forgottenrelics.entity;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

/**
 * Chaotic orb from the Chaos Tome. Seeker orbs hunt the nearest living target
 * (frequently ignoring the caster); wild orbs ricochet randomly. Impacts
 * explode, and wild orbs occasionally leave something stranger behind.
 *
 * <p>The source's taint-biome/node special is approximated with a taint
 * particle burst; TC4R's taint spread is data driven and out of scope for an
 * orb impact.</p>
 */
public class EntityChaoticOrb extends Projectile implements IEntityAdditionalSpawnData {

    private boolean seeker;
    private int ownerId = -1;
    private int count;

    public EntityChaoticOrb(EntityType<? extends EntityChaoticOrb> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityChaoticOrb(EntityType<? extends EntityChaoticOrb> type, Level level, LivingEntity thrower, boolean seeker) {
        this(type, level);
        this.setOwner(thrower);
        this.seeker = seeker;
        this.ownerId = thrower.getId();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.seeker);
        buffer.writeVarInt(this.ownerId);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.seeker = buffer.readBoolean();
        this.ownerId = buffer.readVarInt();
    }

    @Override
    public void tick() {
        super.tick();
        this.count++;

        if (this.level().isClientSide()) {
            for (int i = 0; i < 4; i++) {
                this.level().addParticle(ParticleTypes.WITCH,
                        this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                        this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                        this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                        0.0D, 0.0D, 0.0D);
            }
        }

        Random seeded = new Random(this.getId() + this.count);
        if (this.tickCount > 20) {
            if (!this.seeker) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(
                        motion.x + (seeded.nextFloat() - seeded.nextFloat()) * 0.01F,
                        motion.y + (seeded.nextFloat() - seeded.nextFloat()) * 0.01F,
                        motion.z + (seeded.nextFloat() - seeded.nextFloat()) * 0.01F);
            } else {
                steerTowardsTarget();
            }
        }

        if (this.tickCount > 5000) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            return;
        }

        this.applyGravityDrift();
        Vec3 motion = this.getDeltaMovement();
        Vec3 next = this.position().add(motion);

        BlockHitResult blockHit = this.level().clip(new net.minecraft.world.level.ClipContext(
                this.position(), next, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            detonate(null, false);
            return;
        }

        EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                this.level(), this, this.position(), next,
                this.getBoundingBox().expandTowards(motion).inflate(0.5D),
                candidate -> candidate instanceof LivingEntity && candidate != this.getOwner() && candidate.isAlive());
        if (entityHit != null) {
            detonate(entityHit.getEntity(), false);
            return;
        }

        this.setPos(next.x, next.y, next.z);
    }

    private void applyGravityDrift() {
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y - 0.001D, motion.z);
    }

    private void steerTowardsTarget() {
        List<Entity> candidates = this.level().getEntities(this, this.getBoundingBox().inflate(16.0D),
                entity -> entity instanceof LivingEntity && entity.isAlive() && entity != this);
        double best = Double.MAX_VALUE;
        Entity target = null;
        for (Entity candidate : candidates) {
            if (candidate.getId() == this.ownerId && Math.random() < 0.8D) {
                continue;
            }
            double distance = this.distanceToSqr(candidate);
            if (distance < best) {
                best = distance;
                target = candidate;
            }
        }
        if (target != null) {
            Vec3 motion = this.getDeltaMovement();
            double dx = target.getX() - this.getX();
            double dy = target.getBoundingBox().minY + target.getBbHeight() * 0.9D - this.getY();
            double dz = target.getZ() - this.getZ();
            double distanceSqr = Math.max(0.0001D, dx * dx + dy * dy + dz * dz);
            double factor = 0.2D / distanceSqr;
            this.setDeltaMovement(clamp(motion.x + dx * factor), clamp(motion.y + dy * factor), clamp(motion.z + dz * factor));
        }
    }

    private static double clamp(double value) {
        return Math.max(-0.2D, Math.min(0.2D, value));
    }

    private void detonate(@Nullable Entity directHit, boolean portal) {
        if (directHit instanceof LivingEntity living) {
            float amount = (float) (1.0D + Math.random() * FRConfig.chaosTomeDamageCap());
            living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_MAGIC, this, this.getOwner()), amount);
        }

        float power = 1.0F + (float) Math.random() * 6.0F;
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), power,
                Level.ExplosionInteraction.MOB);

        if (!this.seeker && this.random.nextInt(100) <= (portal ? 10 : 1)) {
            specialEffect();
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, this.getX(), this.getY(), this.getZ(), 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.INFERNAL, this.getX(), this.getY(), this.getZ(), 0, 1.0F, 32));
        }
        this.discard();
    }

    private void specialEffect() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 0.8F + (float) Math.random() * 0.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                    this.getX(), this.getY() + 1.0D, this.getZ(), 48, 3.0D, 2.0D, 3.0D, 0.05D);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.seeker = tag.getBoolean("Seeker");
        this.ownerId = tag.getInt("OwnerId");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Seeker", this.seeker);
        tag.putInt("OwnerId", this.ownerId);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }
}
