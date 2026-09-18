package dev.tc4port.forgottenrelics.entity;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.network.LightningPayload;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Thunderpeal orb. Detonates into a chain of lightning arcs: each struck
 * victim arcs to up to three more targets at half damage.
 */
public class EntityThunderpealOrb extends Projectile {

    private static final int AREA = 4;

    public EntityThunderpealOrb(EntityType<? extends EntityThunderpealOrb> type, Level level) {
        super(type, level);
    }

    public EntityThunderpealOrb(EntityType<? extends EntityThunderpealOrb> type, Level level, LivingEntity thrower) {
        this(type, level);
        this.setOwner(thrower);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 500) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        motion = motion.add(0.0D, -0.05D, 0.0D);
        Vec3 next = this.position().add(motion);

        BlockHitResult blockHit = this.level().clip(new net.minecraft.world.level.ClipContext(
                this.position(), next, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.setPos(blockHit.getLocation());
            detonate(null);
            return;
        }

        EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                this.level(), this, this.position(), next,
                this.getBoundingBox().expandTowards(motion).inflate(0.5D),
                candidate -> candidate instanceof LivingEntity && candidate != this.getOwner() && candidate.isAlive());
        if (entityHit != null) {
            detonate(entityHit.getEntity());
            return;
        }

        this.setDeltaMovement(motion);
        this.setPos(next.x, next.y, next.z);
    }

    private void detonate(@Nullable Entity directHit) {
        if (this.level().isClientSide()) {
            return;
        }

        if (directHit instanceof LivingEntity living) {
            living.invulnerableTime = 0;
            living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_LIGHTNING, this, this.getOwner()),
                    (float) FRConfig.damageThunderpealDirect());
        }

        List<Entity> nearby = new ArrayList<>(this.level().getEntities(this, this.getBoundingBox().inflate(AREA),
                entity -> entity instanceof LivingEntity && entity != this.getOwner() && entity != directHit));
        for (Entity candidate : nearby) {
            if (!(candidate instanceof LivingEntity living)) {
                continue;
            }
            lightning(this, living);
            living.invulnerableTime = 0;
            living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_LIGHTNING, this, this.getOwner()),
                    (float) FRConfig.damageThunderpealBolt());

            List<Entity> chained = new ArrayList<>(this.level().getEntities(living, living.getBoundingBox().inflate(AREA),
                    entity -> entity instanceof LivingEntity && entity != this.getOwner() && entity != living));
            while (chained.size() > 3) {
                chained.remove((int) (Math.random() * chained.size()));
            }
            for (Entity chain : chained) {
                if (chain instanceof LivingEntity livingChain) {
                    lightning(living, livingChain);
                    livingChain.invulnerableTime = 0;
                    livingChain.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_LIGHTNING, this, this.getOwner()),
                            (float) FRConfig.damageThunderpealBolt() / 2.0F);
                }
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, this.getX(), this.getY(), this.getZ(), 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.BURST, this.getX(), this.getY(), this.getZ(), 0, 2.0F, 40));
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        this.discard();
    }

    private void lightning(Entity from, Entity to) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 start = from.position().add(0.0D, from.getBbHeight() * 0.5D, 0.0D);
        Vec3 end = to.position().add(0.0D, to.getBbHeight() * 0.5D, 0.0D);
        FRNetwork.sendNear(serverLevel, start.x, start.y, start.z, 64.0D,
                new LightningPayload(start.x, start.y, start.z, end.x, end.y, end.z, 0x7FC4FF, 0.075F));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (source.getEntity() != null) {
            this.setDeltaMovement(source.getEntity().getLookAngle().scale(0.9D));
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_HURT, SoundSource.PLAYERS, 1.0F,
                    1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            return true;
        }
        return false;
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
