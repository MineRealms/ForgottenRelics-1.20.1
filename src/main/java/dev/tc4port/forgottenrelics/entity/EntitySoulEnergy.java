package dev.tc4port.forgottenrelics.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

/**
 * Soul mote from the Edict of a Thousand Damned Souls. Homes onto a friendly
 * target and heals one health (plus one food point for players) on arrival.
 */
public class EntitySoulEnergy extends Projectile implements IEntityAdditionalSpawnData {

    private int targetId = -1;
    @Nullable
    private LivingEntity target;

    public EntitySoulEnergy(EntityType<? extends EntitySoulEnergy> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntitySoulEnergy(EntityType<? extends EntitySoulEnergy> type, Level level, LivingEntity thrower, LivingEntity target) {
        this(type, level);
        this.setOwner(thrower);
        this.target = target;
        this.targetId = target.getId();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.target == null ? -1 : this.target.getId());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.targetId = buffer.readVarInt();
        if (this.targetId >= 0 && this.level() != null) {
            Entity entity = this.level().getEntity(this.targetId);
            if (entity instanceof LivingEntity living) {
                this.target = living;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 1000) {
            this.discard();
            return;
        }

        if (this.target == null) {
            Entity entity = this.level().getEntity(this.targetId);
            if (entity instanceof LivingEntity living) {
                this.target = living;
            } else {
                this.discard();
                return;
            }
        }

        if (this.level().isClientSide()) {
            spawnTrailParticles();
        }

        Vec3 motion = this.getDeltaMovement();
        double dx = this.target.getX() - this.getX();
        double dy = this.target.getBoundingBox().minY + this.target.getBbHeight() * 0.6D - this.getY();
        double dz = this.target.getZ() - this.getZ();
        double distanceSqr = Math.max(0.0001D, dx * dx + dy * dy + dz * dz);
        double factor = 0.3D / distanceSqr;
        this.setDeltaMovement(
                clamp(motion.x + dx * factor),
                clamp(motion.y + dy * factor),
                clamp(motion.z + dz * factor));

        if (!this.level().isClientSide() && this.getBoundingBox().inflate(0.25D).intersects(this.target.getBoundingBox())) {
            this.target.heal(1.0F);
            if (this.target instanceof ServerPlayer player) {
                player.getFoodData().eat(1, 1.0F);
            }
            this.level().playSound(null, this.target.getX(), this.target.getY(), this.target.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 0.8F + (float) Math.random() * 0.2F);
            this.discard();
        }
    }

    private void spawnTrailParticles() {
        double steps = Math.max(1.0D, this.getDeltaMovement().length() / 0.05D);
        for (int i = 0; i < Math.min(steps, 32); i++) {
            double t = i / steps;
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    this.xo + (this.getX() - this.xo) * t,
                    this.yo + (this.getY() - this.yo) * t,
                    this.zo + (this.getZ() - this.zo) * t,
                    0.0D, 0.0D, 0.0D);
        }
    }

    private static double clamp(double value) {
        return Math.max(-0.35D, Math.min(0.35D, value));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.targetId = tag.getInt("TargetId");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TargetId", this.targetId);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }
}
