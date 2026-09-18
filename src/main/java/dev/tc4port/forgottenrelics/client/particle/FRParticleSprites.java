package dev.tc4port.forgottenrelics.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/** Lazy sprite lookups for the ported FX textures. */
final class FRParticleSprites {

    static final ResourceLocation WISP_LARGE = ResourceLocation.fromNamespaceAndPath("forgottenrelics", "particle/wisp_large");
    static final ResourceLocation SPARKLE_SHEET = ResourceLocation.fromNamespaceAndPath("forgottenrelics", "particle/sparkle_sheet");

    private static TextureAtlasSprite wisp;
    private static TextureAtlasSprite sparkle;

    private FRParticleSprites() {
    }

    static TextureAtlasSprite wisp() {
        if (wisp == null) {
            wisp = atlas().apply(WISP_LARGE);
        }
        return wisp;
    }

    static TextureAtlasSprite sparkle() {
        if (sparkle == null) {
            sparkle = atlas().apply(SPARKLE_SHEET);
        }
        return sparkle;
    }

    static void invalidate() {
        wisp = null;
        sparkle = null;
    }

    private static java.util.function.Function<ResourceLocation, TextureAtlasSprite> atlas() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_PARTICLES);
    }

}
