package dev.tc4port.forgottenrelics.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * 1.20.1 port of Botania's {@code FXSparkle}.
 *
 * <p>The source sampled one frame of an 8x8 particle sheet every
 * {@code multiplier} ticks starting at frame 16, at full brightness, with an
 * optional size ramp and optional block push-out. That contract is preserved;
 * the corrupt variant renders additively because Botania's film-grain shader
 * has no 1.20.1 equivalent.</p>
 */
public final class FRSparkleParticle extends TextureSheetParticle {

    private final TextureAtlasSprite sheet;
    private final float baseSize;
    private final int multiplier;
    private final int firstFrame;
    private float frameU0;
    private float frameU1;
    private float frameV0;
    private float frameV1;

    public boolean corrupt;
    public boolean fake;
    public boolean shrink = true;
    public boolean noClip;
    public boolean slowdown = true;

    public FRSparkleParticle(ClientLevel level, double x, double y, double z, float size, float red, float green, float blue, int multiplier) {
        super(level, x, y, z);
        this.sheet = FRParticleSprites.sparkle();
        this.setSprite(this.sheet);
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.0F;
        this.friction = 0.908F;
        this.hasPhysics = false;
        this.multiplier = Math.max(1, multiplier);
        this.lifetime = Math.max(1, 3 * this.multiplier);
        this.firstFrame = 16;
        this.baseSize = this.quadSize * size;
        this.quadSize = this.baseSize;
        float resolvedRed = red == 0.0F ? 1.0F : red;
        this.setColor(resolvedRed, green, blue);
        this.setAlpha(1.0F);
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        updateFrame();
    }

    public void setParticleMotion(double mx, double my, double mz) {
        this.xd = mx;
        this.yd = my;
        this.zd = mz;
    }

    private void updateFrame() {
        int frame = this.firstFrame + this.age / this.multiplier;
        int frameX = frame % 8;
        int frameY = frame / 8;
        float uSpan = this.sheet.getU1() - this.sheet.getU0();
        float vSpan = this.sheet.getV1() - this.sheet.getV0();
        this.frameU0 = this.sheet.getU0() + uSpan * (frameX / 8.0F);
        this.frameV0 = this.sheet.getV0() + vSpan * (frameY / 8.0F);
        this.frameU1 = this.frameU0 + uSpan * (1.0F / 8.0F);
        this.frameV1 = this.frameV0 + vSpan * (1.0F / 8.0F);
    }

    @Override
    public float getU0() { return this.frameU0; }

    @Override
    public float getU1() { return this.frameU1; }

    @Override
    public float getV0() { return this.frameV0; }

    @Override
    public float getV1() { return this.frameV1; }

    @Override
    public ParticleRenderType getRenderType() {
        return this.corrupt ? FRParticleRenderTypes.SPARKLE_CORRUPT : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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

        if (!this.noClip && !this.fake) {
            pushOutOfSolidBlock();
        }

        this.setPos(this.x + this.xd, this.y + this.yd, this.z + this.zd);

        if (this.slowdown) {
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
        }

        if (this.fake && this.age > 1) {
            this.remove();
            return;
        }

        if (this.shrink) {
            this.quadSize = this.baseSize * Math.max(0.0F, (this.lifetime - this.age + 1.0F) / this.lifetime);
        }
        updateFrame();
    }

    /** Port of the source {@code pushOutOfBlocks}: nudge out along the nearest open face. */
    private void pushOutOfSolidBlock() {
        BlockPos occupied = BlockPos.containing(this.x, this.y, this.z);
        BlockState state = this.level.getBlockState(occupied);
        if (state.isAir() || !state.isCollisionShapeFullBlock(this.level, occupied) || !state.getFluidState().isEmpty()) {
            return;
        }

        double localX = this.x - occupied.getX();
        double localY = this.y - occupied.getY();
        double localZ = this.z - occupied.getZ();
        Direction escape = null;
        double nearest = Double.MAX_VALUE;

        if (canEscape(occupied.west()) && localX < nearest) {
            nearest = localX;
            escape = Direction.WEST;
        }
        if (canEscape(occupied.east()) && 1.0D - localX < nearest) {
            nearest = 1.0D - localX;
            escape = Direction.EAST;
        }
        if (canEscape(occupied.below()) && localY < nearest) {
            nearest = localY;
            escape = Direction.DOWN;
        }
        if (canEscape(occupied.above()) && 1.0D - localY < nearest) {
            nearest = 1.0D - localY;
            escape = Direction.UP;
        }
        if (canEscape(occupied.north()) && localZ < nearest) {
            nearest = localZ;
            escape = Direction.NORTH;
        }
        if (canEscape(occupied.south()) && 1.0D - localZ < nearest) {
            escape = Direction.SOUTH;
        }
        if (escape == null) {
            return;
        }

        float impulse = this.random.nextFloat() * 0.05F + 0.025F;
        float jitter = (this.random.nextFloat() - this.random.nextFloat()) * 0.1F;
        switch (escape) {
            case WEST -> {
                this.xd = -impulse;
                this.yd = jitter;
                this.zd = jitter;
            }
            case EAST -> {
                this.xd = impulse;
                this.yd = jitter;
                this.zd = jitter;
            }
            case DOWN -> {
                this.yd = -impulse;
                this.xd = jitter;
                this.zd = jitter;
            }
            case UP -> {
                this.yd = impulse;
                this.xd = jitter;
                this.zd = jitter;
            }
            case NORTH -> {
                this.zd = -impulse;
                this.yd = jitter;
                this.xd = jitter;
            }
            case SOUTH -> {
                this.zd = impulse;
                this.yd = jitter;
                this.xd = jitter;
            }
            default -> {
            }
        }
    }

    private boolean canEscape(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        return state.isAir() || !state.isCollisionShapeFullBlock(this.level, pos);
    }
}
