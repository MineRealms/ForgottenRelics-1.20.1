package dev.tc4port.forgottenrelics.entity;

import dev.tc4port.forgottenrelics.FRConfig;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

/**
 * Crimson orb from the Crimson Spell. Relentlessly homes onto the nearest
 * visible victim, retargeting while it flies. The red flag mirrors the
 * source's "active" marker; blue variants fizzle out immediately.
 */
public class EntityCrimsonOrb extends Projectile implements IEntityAdditionalSpawnData {

    private int targetId = -1;
    private int casterId = -1;
    @Nullable
    private LivingEntity target;
    @Nullable
    private LivingEntity caster;
    private boolean red;

    public EntityCrimsonOrb(EntityType<? extends EntityCrimsonOrb> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityCrimsonOrb(EntityType<? extends EntityCrimsonOrb> type, Level level, LivingEntity caster, LivingEntity target, boolean red) {
        this(type, level);
        this.setOwner(caster);
        this.caster = caster;
        this.casterId = caster.getId();
        this.target = target;
        this.targetId = target.getId();
        this.red = red;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.target == null ? -1 : this.target.getId());
        buffer.writeVarInt(this.caster == null ? -1 : this.caster.getId());
        buffer.writeBoolean(this.red);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.targetId = buffer.readVarInt();
        this.casterId = buffer.readVarInt();
        this.target = resolve(this.targetId);
        this.caster = resolve(this.casterId);
        this.red = buffer.readBoolean();
    }

    @Nullable
    private LivingEntity resolve(int id) {
        if (id < 0 || this.level() == null) {
            return null;
        }
        Entity entity = this.level().getEntity(id);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 1000 || !this.red) {
            this.discard();
            return;
        }

        if (this.target == null || !this.target.isAlive()) {
            this.retarget();
        }

        if (this.target != null) {
            Vec3 motion = this.getDeltaMovement();
            double dx = this.target.getX() - this.getX();
            double dy = this.target.getBoundingBox().minY + this.target.getBbHeight() * 0.6D - this.getY();
            double dz = this.target.getZ() - this.getZ();
            double distanceSqr = Math.max(0.0001D, dx * dx + dy * dy + dz * dz);
            double factor = 0.3D / distanceSqr;
            double nextY = motion.y + dy * factor;
            if (this.tickCount < 5 && nextY < 0.0D) {
                nextY = Math.abs(nextY);
            }
            this.setDeltaMovement(clamp(motion.x + dx * factor), clamp(nextY), clamp(motion.z + dz * factor));
        }

        if (this.level().isClientSide()) {
            this.level().addParticle(this.red ? ParticleTypes.FLAME : ParticleTypes.SOUL,
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        Vec3 next = this.position().add(motion);

        BlockHitResult blockHit = this.level().clip(new net.minecraft.world.level.ClipContext(
                this.position(), next, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            BlockState state = this.level().getBlockState(blockHit.getBlockPos());
            if (!isPassable(state)) {
                detonate(null);
                return;
            }
        }

        EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                this.level(), this, this.position(), next,
                this.getBoundingBox().expandTowards(motion).inflate(0.5D),
                candidate -> candidate instanceof LivingEntity && candidate != this.caster && candidate.isAlive());
        if (entityHit != null) {
            detonate(entityHit.getEntity());
            return;
        }

        this.setPos(next.x, next.y, next.z);
    }

    private static boolean isPassable(BlockState state) {
        return state.is(BlockTags.LEAVES)
                || state.getBlock() instanceof BushBlock
                || !state.getFluidState().isEmpty();
    }

    private void retarget() {
        if (this.level().isClientSide()) {
            return;
        }
        var candidates = this.level().getEntities(this, this.getBoundingBox().inflate(32.0D),
                entity -> entity instanceof LivingEntity && entity != this.caster && entity != this);
        candidates.removeIf(entity -> !(entity instanceof LivingEntity living) || !living.hasLineOfSight(this));
        if (!candidates.isEmpty()) {
            Entity chosen = candidates.get((int) (Math.random() * candidates.size()));
            if (chosen instanceof LivingEntity living) {
                this.target = living;
                this.targetId = living.getId();
            }
        }
    }

    private void detonate(@Nullable Entity directHit) {
        if (directHit instanceof LivingEntity living) {
            double min = FRConfig.crimsonSpellDamageMIN();
            double max = FRConfig.crimsonSpellDamageMAX();
            float amount = (float) (min + Math.random() * (max - min));
            living.hurt(FRDamageTypes.source(this.level(), FRDamageTypes.FORGOTTEN_MAGIC, this, this.getOwner()), amount);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F,
                1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, this.getX(), this.getY(), this.getZ(), 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.BURST, this.getX(), this.getY(), this.getZ(), 0, 1.0F, 24));
        }
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (source.getEntity() != null) {
            Vec3 look = source.getEntity().getLookAngle().scale(0.9D);
            this.setDeltaMovement(look);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_HURT, SoundSource.PLAYERS, 1.0F,
                    1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            return true;
        }
        return false;
    }

    private static double clamp(double value) {
        return Math.max(-0.25D, Math.min(0.25D, value));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.targetId = tag.getInt("TargetId");
        this.casterId = tag.getInt("CasterId");
        this.red = tag.getBoolean("Red");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TargetId", this.targetId);
        tag.putInt("CasterId", this.casterId);
        tag.putBoolean("Red", this.red);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }
}
