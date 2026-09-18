package dev.tc4port.forgottenrelics.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

/**
 * Particle render types for the ported Botania FX.
 *
 * <p>The 1.7.10 {@code FXWisp} rendered its depth-ignoring queue with
 * {@code glDisable(GL_DEPTH_TEST)}. Particle render types are the 1.20.1
 * equivalent of that batching, so the depth toggle is reproduced here.</p>
 */
public final class FRParticleRenderTypes {

    /** Depth-ignoring translucent wisps (the source's {@code depthTest = false} path). */
    public static final ParticleRenderType DEPTH_IGNORING = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "forgottenrelics:wisp_depth_ignoring";
        }
    };

    /** Corrupt sparkles; the source applied Botania's film-grain shader here. */
    public static final ParticleRenderType SPARKLE_CORRUPT = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
        }

        @Override
        public String toString() {
            return "forgottenrelics:sparkle_corrupt";
        }
    };

    private FRParticleRenderTypes() {
    }
}
