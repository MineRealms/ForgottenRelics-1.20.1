package dev.tc4port.forgottenrelics.entity;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

/**
 * Summoned Babylon weapon from Apotheosis. Hangs in the air, charges for a
 * moment, then rockets toward whatever the summoner is looking at. Detonation
 * damages and knocks back everything nearby.
 */
public class EntityBabylonWeaponSS extends Projectile implements IEntityAdditionalSpawnData {

    private static final EntityDataAccessor<Integer> DATA_VARIETY =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHARGE_TICKS =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIVE_TICKS =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DELAY =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ROTATION =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(EntityBabylonWeaponSS.class, EntityDataSerializers.BOOLEAN);

    public EntityBabylonWeaponSS(EntityType<? extends EntityBabylonWeaponSS> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityBabylonWeaponSS(EntityType<? extends EntityBabylonWeaponSS> type, Level level, LivingEntity thrower) {
        this(type, level);
        this.setOwner(thrower);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_VARIETY, 0);
        this.entityData.define(DATA_CHARGE_TICKS, 0);
        this.entityData.define(DATA_LIVE_TICKS, 0);
        this.entityData.define(DATA_DELAY, 0);
        this.entityData.define(DATA_ROTATION, 0.0F);
        this.entityData.define(DATA_CHARGING, false);
    }

    public int getVariety() {
        return this.entityData.get(DATA_VARIETY);
    }

    public void setVariety(int variety) {
        this.entityData.set(DATA_VARIETY, variety);
    }

    public int getChargeTicks() {
        return this.entityData.get(DATA_CHARGE_TICKS);
    }

    public void setChargeTicks(int ticks) {
        this.entityData.set(DATA_CHARGE_TICKS, ticks);
    }

    public int getLiveTicks() {
        return this.entityData.get(DATA_LIVE_TICKS);
    }

    public void setLiveTicks(int ticks) {
        this.entityData.set(DATA_LIVE_TICKS, ticks);
    }

    public int getDelay() {
        return this.entityData.get(DATA_DELAY);
    }

    public void setDelay(int delay) {
        this.entityData.set(DATA_DELAY, delay);
    }

    public float getRotation() {
        return this.entityData.get(DATA_ROTATION);
    }

    public void setRotation(float rotation) {
        this.entityData.set(DATA_ROTATION, rotation);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.getRotation());
        buffer.writeVarInt(this.getVariety());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.setRotation(buffer.readFloat());
        this.setVariety(buffer.readVarInt());
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();
        if (!this.level().isClientSide() && (!(owner instanceof Player player) || !player.isAlive())) {
            this.discard();
            return;
        }

        int liveTime = this.getLiveTicks();
        int delay = this.getDelay();

        if (this.tickCount <= 15) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setChargeTicks(this.getChargeTicks() + 1);
            if (this.random.nextInt(20) == 0) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.1F, 1.0F + this.random.nextFloat() * 3.0F);
            }
        } else if (liveTime < delay) {
            this.setDeltaMovement(Vec3.ZERO);
        } else if (liveTime == delay && owner instanceof Player player) {
            Vec3 aim = aimPoint(player);
            Vec3 motion = aim.subtract(this.position()).normalize().scale(3.0D);
            this.setDeltaMovement(motion);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 2.0F, 0.1F + this.random.nextFloat() * 3.0F);
        }

        if (liveTime >= delay) {
            this.setLiveTicks(liveTime + 1);
        }

        if (!this.level().isClientSide()) {
            AABB sweep = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(2.0D);
            for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, sweep)) {
                if (candidate == owner || !candidate.isAlive()) {
                    continue;
                }
                detonate(candidate);
                return;
            }
        }

        super.tick();

        if (this.level().isClientSide() && liveTime > delay) {
            this.level().addParticle(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }

        if (liveTime > 200 + delay) {
            this.discard();
        }
    }

    private Vec3 aimPoint(Player player) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getLookAngle().scale(64.0D));
        BlockHitResult hit = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.MISS) {
            return hit.getLocation();
        }
        return to;
    }

    private void detonate(@Nullable Entity directHit) {
        if (directHit instanceof LivingEntity living) {
            living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_MAGIC, this, this.getOwner()),
                    (float) FRConfig.damageApotheosisDirect());
            pushAway(living);
        }

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.0D),
                entity -> entity != this.getOwner() && entity.isAlive());
        for (LivingEntity living : nearby) {
            if (living == directHit) {
                continue;
            }
            living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_MAGIC, this, this.getOwner()),
                    (float) FRConfig.damageApotheosisImpact());
            pushAway(living);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, this.getX(), this.getY(), this.getZ(), 128.0D,
                    EffectPayload.at(EffectPayload.EffectType.APOTHEOSIS, this.getX(), this.getY(), this.getZ(), 0, 1.5F, 40));
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 8.0F, 0.8F + (float) Math.random() * 0.2F);
        this.discard();
    }

    private void pushAway(LivingEntity living) {
        Vec3 diff = living.position().subtract(this.position());
        double distance = Math.max(1.0D, diff.length());
        Vec3 push = diff.normalize().scale(1.0D / distance);
        if (push.length() > 1.0D) {
            push = push.normalize();
        }
        boolean boss = living.getMaxHealth() >= 100.0F;
        if (boss) {
            push = push.scale(0.5D);
        }
        living.setDeltaMovement(living.getDeltaMovement().add(push));
        living.hurtMarked = true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setVariety(tag.getInt("Variety"));
        this.setChargeTicks(tag.getInt("ChargeTicks"));
        this.setLiveTicks(tag.getInt("LiveTicks"));
        this.setDelay(tag.getInt("Delay"));
        this.setRotation(tag.getFloat("Rotation"));
        this.setCharging(tag.getBoolean("Charging"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Variety", this.getVariety());
        tag.putInt("ChargeTicks", this.getChargeTicks());
        tag.putInt("LiveTicks", this.getLiveTicks());
        tag.putInt("Delay", this.getDelay());
        tag.putFloat("Rotation", this.getRotation());
        tag.putBoolean("Charging", this.isCharging());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }

}
