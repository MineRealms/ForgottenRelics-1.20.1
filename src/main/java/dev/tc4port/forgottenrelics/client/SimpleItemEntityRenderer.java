package dev.tc4port.forgottenrelics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Billboard item renderer for the port's projectile entities. The source mod
 * rendered these with custom models; a fullbright item billboard preserves the
 * silhouette and glow without porting Botania's TESR helpers.
 */
@OnlyIn(Dist.CLIENT)
public class SimpleItemEntityRenderer<T extends Entity> extends EntityRenderer<T> {

    private final ItemStack display;
    private final float scale;
    private final boolean spin;

    public SimpleItemEntityRenderer(EntityRendererProvider.Context context, ItemStack display, float scale, boolean spin) {
        super(context);
        this.display = display;
        this.scale = scale;
        this.spin = spin;
        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, 0.0D);
        float spinAngle = this.spin ? (entity.tickCount + partialTicks) * 12.0F : 180.0F - entityYaw;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinAngle));
        poseStack.scale(this.scale, this.scale, this.scale);
        net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(
                this.display, ItemDisplayContext.GROUND, 0xF000F0, OverlayTexture.NO_OVERLAY, poseStack, buffer,
                entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
