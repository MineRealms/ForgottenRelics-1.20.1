package dev.tc4port.forgottenrelics.entity;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import dev.tc4port.thaumcraft.block.entity.EldritchPortalBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Dark matter orb from the Eldritch Spell. Drifts for 200 ticks, damages
 * everything around its impact point and applies weakness, slowness and
 * wither - harsher inside the Outer Lands.
 */
public class EntityDarkMatterOrb extends Projectile {

    private static final int MAX_AGE = 200;
    private static final int IMPACT_EVENT = 16;

    public EntityDarkMatterOrb(EntityType<? extends EntityDarkMatterOrb> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityDarkMatterOrb(EntityType<? extends EntityDarkMatterOrb> type, Level level, LivingEntity thrower) {
        this(type, level);
        this.setOwner(thrower);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount >= MAX_AGE) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.SCULK_SOUL,
                        this.getX() + (Math.random() - 0.5D) * 0.2D,
                        this.getY() + 0.2D + (Math.random() - 0.5D) * 0.2D,
                        this.getZ() + (Math.random() - 0.5D) * 0.2D,
                        0.0D, 0.0D, 0.0D);
            }
        } else {
            Vec3 motion = this.getDeltaMovement();
            if (this.tickCount >= 100 && motion.lengthSqr() < 0.0001D) {
                this.impact(null);
                return;
            }
        }

        Vec3 motion = this.getDeltaMovement();
        Vec3 next = this.position().add(motion);

        BlockHitResult blockHit = this.level().clip(new net.minecraft.world.level.ClipContext(
                this.position(), next, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            BlockState state = this.level().getBlockState(blockHit.getBlockPos());
            if (!isPassable(state)) {
                this.impact(null);
                return;
            }
        }

        EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                this.level(), this, this.position(), next,
                this.getBoundingBox().expandTowards(motion).inflate(0.5D),
                candidate -> candidate instanceof LivingEntity && candidate != this.getOwner() && candidate.isAlive());
        if (entityHit != null) {
            this.impact(entityHit.getEntity());
            return;
        }

        this.setPos(next.x, next.y, next.z);
    }

    private static boolean isPassable(BlockState state) {
        return state.is(BlockTags.LEAVES)
                || state.getBlock() instanceof BushBlock
                || !state.getFluidState().isEmpty();
    }

    private void impact(@Nullable Entity directHit) {
        if (this.level().isClientSide()) {
            return;
        }
        boolean outerLands = this.level().dimension().equals(EldritchPortalBlockEntity.OUTER_LANDS);

        if (directHit instanceof LivingEntity living) {
            applyDamage(living, outerLands);
        }
        for (Entity candidate : this.level().getEntities(this, this.getBoundingBox().inflate(1.0D))) {
            if (candidate instanceof LivingEntity living && candidate != this.getOwner() && candidate != directHit) {
                applyDamage(living, outerLands);
            }
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 2.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.8F);
        this.level().broadcastEntityEvent(this, (byte) IMPACT_EVENT);
        if (this.level() instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, this.getX(), this.getY(), this.getZ(), 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.VOID, this.getX(), this.getY(), this.getZ(), 0, 1.0F, 28));
        }
        this.discard();
    }

    private void applyDamage(LivingEntity living, boolean outerLands) {
        Entity owner = this.getOwner();
        float amount = (float) (outerLands ? FRConfig.eldritchSpellDamageEx() : FRConfig.eldritchSpellDamage());
        living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.DARK_MATTER, this, owner), amount);
        if (outerLands) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 320, 2));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 2));
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 250, 3));
        } else {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == IMPACT_EVENT) {
            for (int i = 0; i < 30; i++) {
                double vx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3D;
                double vy = (this.random.nextFloat() - this.random.nextFloat()) * 0.3D;
                double vz = (this.random.nextFloat() - this.random.nextFloat()) * 0.3D;
                this.level().addParticle(ParticleTypes.SCULK_SOUL,
                        this.getX() + vx, this.getY() + vy, this.getZ() + vz,
                        vx * 8.0D, vy * 8.0D, vz * 8.0D);
            }
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }
}
