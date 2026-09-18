package dev.tc4port.forgottenrelics.entity;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Nuclear Fury missile from the Missile Tome. Hunts a random victim and
 * detonates on contact; "evil" missiles detonate as soon as they close in.
 */
public class EntityRageousMissile extends Projectile {

    private static final EntityDataAccessor<Boolean> DATA_EVIL =
            SynchedEntityData.defineId(EntityRageousMissile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TARGET =
            SynchedEntityData.defineId(EntityRageousMissile.class, EntityDataSerializers.INT);

    private int time;

    public EntityRageousMissile(EntityType<? extends EntityRageousMissile> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityRageousMissile(EntityType<? extends EntityRageousMissile> type, Level level, LivingEntity thrower, boolean evil) {
        this(type, level);
        this.setOwner(thrower);
        this.setEvil(evil);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_EVIL, false);
        this.entityData.define(DATA_TARGET, -1);
    }

    public void setEvil(boolean evil) {
        this.entityData.set(DATA_EVIL, evil);
    }

    public boolean isEvil() {
        return this.entityData.get(DATA_EVIL);
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.entityData.set(DATA_TARGET, target == null ? -1 : target.getId());
    }

    @Nullable
    public LivingEntity getTargetEntity() {
        int id = this.entityData.get(DATA_TARGET);
        if (id < 0 || this.level() == null) {
            return null;
        }
        Entity entity = this.level().getEntity(id);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        this.time++;

        if (!this.level().isClientSide() && this.getTargetEntity() == null && this.time > 160) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            spawnTrail();
        }

        LivingEntity target = this.getTargetEntity();
        if (target == null) {
            if (!this.level().isClientSide()) {
                this.findTarget();
            }
            Vec3 random = new Vec3((Math.random() - 0.5D) * 16.0D, (Math.random() - 0.5D) * 16.0D, (Math.random() - 0.5D) * 16.0D);
            Vec3 direction = random.normalize().scale(0.5D);
            this.setDeltaMovement(direction);
        } else {
            Vec3 targetCenter = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
            Vec3 direction = targetCenter.subtract(this.position()).normalize().scale(0.5D);
            if (this.time < 30) {
                direction = new Vec3(direction.x, Math.abs(direction.y), direction.z);
            }
            this.setDeltaMovement(direction);

            if (this.isEvil() && this.position().distanceTo(targetCenter) < 1.0D) {
                explode(target);
                return;
            }
        }

        if (this.level().isClientSide()) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        Vec3 next = this.position().add(motion);
        EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                this.level(), this, this.position(), next,
                this.getBoundingBox().expandTowards(motion).inflate(0.5D),
                candidate -> candidate instanceof LivingEntity && candidate != this.getOwner() && candidate.isAlive());
        if (entityHit != null) {
            this.setPos(entityHit.getLocation());
            explode(entityHit.getEntity());
            return;
        }
        this.setPos(next.x, next.y, next.z);
    }

    private void spawnTrail() {
        double steps = Math.max(1.0D, this.getDeltaMovement().length() / 0.05D);
        for (int i = 0; i < Math.min(steps, 24); i++) {
            double t = i / steps;
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.xo + (this.getX() - this.xo) * t,
                    this.yo + (this.getY() - this.yo) * t,
                    this.zo + (this.getZ() - this.zo) * t,
                    0.0D, 0.0D, 0.0D);
        }
    }

    private void findTarget() {
        var candidates = this.level().getEntities(this, this.getBoundingBox().inflate(32.0D),
                entity -> entity instanceof LivingEntity living && living.isAlive() && living != this.getOwner() && entity != this);
        if (!candidates.isEmpty()) {
            Entity chosen = candidates.get(this.random.nextInt(candidates.size()));
            if (chosen instanceof LivingEntity living) {
                this.setTarget(living);
            }
        }
    }

    private void explode(@Nullable Entity directHit) {
        if (directHit instanceof LivingEntity living) {
            double min = FRConfig.nuclearFuryDamageMIN();
            double max = FRConfig.nuclearFuryDamageMAX();
            living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_MAGIC, this, this.getOwner()),
                    (float) (min + Math.random() * (max - min)));
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 2.0F, 0.8F + (float) Math.random() * 0.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, this.getX(), this.getY(), this.getZ(), 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.BURST, this.getX(), this.getY(), this.getZ(), 0, 1.5F, 32));
        }
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.time = tag.getInt("Time");
        this.entityData.set(DATA_EVIL, tag.getBoolean("Evil"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Time", this.time);
        tag.putBoolean("Evil", this.isEvil());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }
}
