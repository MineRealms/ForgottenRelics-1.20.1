package dev.tc4port.forgottenrelics.entity;

import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
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
 * Lunar flare from the Tome of Lunar Flares. Streaks toward a locked block and
 * detonates on arrival, hitting everything in a 2.5 block radius and pulling
 * survivors off their feet.
 */
public class EntityLunarFlare extends Projectile implements IEntityAdditionalSpawnData {

    private int lockX;
    private int lockY;
    private int lockZ;

    public EntityLunarFlare(EntityType<? extends EntityLunarFlare> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityLunarFlare(EntityType<? extends EntityLunarFlare> type, Level level, LivingEntity thrower, int lockX, int lockY, int lockZ) {
        this(type, level);
        this.setOwner(thrower);
        this.lockX = lockX;
        this.lockY = lockY;
        this.lockZ = lockZ;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.lockX);
        buffer.writeVarInt(this.lockY);
        buffer.writeVarInt(this.lockZ);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.lockX = buffer.readVarInt();
        this.lockY = buffer.readVarInt();
        this.lockZ = buffer.readVarInt();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 1000) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            double steps = Math.max(1.0D, this.getDeltaMovement().length() / 0.05D);
            for (int i = 0; i < Math.min(steps, 24); i++) {
                double t = i / steps;
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.xo + (this.getX() - this.xo) * t,
                        this.yo + (this.getY() - this.yo) * t,
                        this.zo + (this.getZ() - this.zo) * t,
                        0.0D, 0.0D, 0.0D);
            }
            return;
        }

        if (this.getOwner() == null) {
            this.discard();
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        Vec3 next = this.position().add(motion);

        BlockHitResult blockHit = this.level().clip(new net.minecraft.world.level.ClipContext(
                this.position(), next, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        boolean landed = blockHit.getType() != HitResult.Type.MISS;
        if (landed) {
            this.setPos(blockHit.getLocation().x, blockHit.getLocation().y, blockHit.getLocation().z);
            if (blockHit.getBlockPos().getX() == this.lockX
                    && blockHit.getBlockPos().getY() == this.lockY
                    && blockHit.getBlockPos().getZ() == this.lockZ) {
                detonate(null);
                return;
            }
        }

        EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                this.level(), this, this.position(), next,
                this.getBoundingBox().expandTowards(motion).inflate(0.5D),
                candidate -> candidate instanceof LivingEntity && candidate != this.getOwner() && candidate.isAlive());
        if (entityHit != null) {
            detonate(entityHit.getEntity());
            return;
        }

        if (!landed) {
            this.setPos(next.x, next.y, next.z);
        }
    }

    private void detonate(@Nullable Entity directHit) {
        if (directHit instanceof LivingEntity living) {
            living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_MAGIC, this, this.getOwner()), 100.0F);
        }

        Vec3 center = this.position();
        for (Entity candidate : this.level().getEntities(this, this.getBoundingBox().inflate(2.5D))) {
            if (!(candidate instanceof LivingEntity living) || living == this.getOwner()) {
                continue;
            }
            living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_MAGIC, this, this.getOwner()), 75.0F);

            Vec3 pull = living.position().subtract(center);
            double distance = Math.max(1.0D, pull.length());
            pull = pull.normalize().scale(1.0D / distance);
            if (pull.length() > 1.0D) {
                pull = pull.normalize();
            }
            boolean boss = living.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER
                    && living.getMaxHealth() >= 100.0F;
            if (boss) {
                pull = pull.scale(0.5D);
            }
            living.push(pull.x, pull.y, pull.z);
            living.hurtMarked = true;
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 16.0F, 0.8F + (float) Math.random() * 0.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, this.getX(), this.getY(), this.getZ(), 128.0D,
                    EffectPayload.at(EffectPayload.EffectType.LUNAR_FLARES, this.getX(), this.getY(), this.getZ(), 0, 2.0F, 48));
            FRNetwork.sendNear(serverLevel, this.getX(), this.getY(), this.getZ(), 128.0D,
                    EffectPayload.at(EffectPayload.EffectType.LUNAR_BURST, this.getX(), this.getY(), this.getZ(), 0, 2.0F, 32));
        }
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.lockX = tag.getInt("LockX");
        this.lockY = tag.getInt("LockY");
        this.lockZ = tag.getInt("LockZ");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LockX", this.lockX);
        tag.putInt("LockY", this.lockY);
        tag.putInt("LockZ", this.lockZ);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }
}
