package dev.tc4port.forgottenrelics.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * Shiny Stone charge mote. A purely visual homing mote that fades after 30
 * ticks; the source mod spawned it from {@code ItemShinyStone}.
 */
public class EntityShinyEnergy extends Projectile implements IEntityAdditionalSpawnData {

    private int targetId = -1;
    @Nullable
    private LivingEntity target;
    private double lockX;
    private double lockY;
    private double lockZ;

    public EntityShinyEnergy(EntityType<? extends EntityShinyEnergy> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityShinyEnergy(EntityType<? extends EntityShinyEnergy> type, Level level, LivingEntity thrower, LivingEntity target,
                             double lockX, double lockY, double lockZ) {
        this(type, level);
        this.setOwner(thrower);
        this.target = target;
        this.targetId = target.getId();
        this.lockX = lockX;
        this.lockY = lockY;
        this.lockZ = lockZ;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.target == null ? -1 : this.target.getId());
        buffer.writeDouble(this.lockX);
        buffer.writeDouble(this.lockY);
        buffer.writeDouble(this.lockZ);
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
        this.lockX = buffer.readDouble();
        this.lockY = buffer.readDouble();
        this.lockZ = buffer.readDouble();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > 30) {
            this.discard();
            return;
        }

        if (this.target == null) {
            if (this.level().isClientSide()) {
                this.discard();
            } else if (!(this.level() instanceof ServerLevel)) {
                this.discard();
            }
            return;
        }

        if (this.level().isClientSide()) {
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        this.getX() + (Math.random() - 0.5D) * 0.1D,
                        this.getY() + (Math.random() - 0.5D) * 0.1D,
                        this.getZ() + (Math.random() - 0.5D) * 0.1D,
                        0.0D, 0.0D, 0.0D);
            }
        }

        Vec3 diff = new Vec3(this.target.getX() - this.getX(),
                this.target.getY() + this.target.getBbHeight() * 0.5D - this.getY(),
                this.target.getZ() - this.getZ());
        Vec3 motion = diff.normalize().scale(0.15D);
        this.setDeltaMovement(motion);

        if (!this.level().isClientSide()) {
            AABB box = this.getBoundingBox().inflate(0.1D);
            if (box.intersects(this.target.getBoundingBox())) {
                this.discard();
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.targetId = tag.getInt("TargetId");
        this.lockX = tag.getDouble("LockX");
        this.lockY = tag.getDouble("LockY");
        this.lockZ = tag.getDouble("LockZ");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TargetId", this.targetId);
        tag.putDouble("LockX", this.lockX);
        tag.putDouble("LockY", this.lockY);
        tag.putDouble("LockZ", this.lockZ);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }
}
