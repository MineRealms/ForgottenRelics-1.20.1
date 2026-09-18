package dev.tc4port.forgottenrelics.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tc4port.forgottenrelics.ForgottenRelics;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client key mappings. The Discord Ring's ability is activated with the same
 * default key as the source mod (X).
 */
@Mod.EventBusSubscriber(modid = ForgottenRelics.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FRClientKeys {

    public static final KeyMapping DISCORD_RING = new KeyMapping(
            "key.discordRing", InputConstants.KEY_X, "key.categories.forgottenrelics");

    private FRClientKeys() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DISCORD_RING);
    }

    @OnlyIn(Dist.CLIENT)
    public static String discordKeyName() {
        return DISCORD_RING.getTranslatedKeyMessage().getString();
    }
}
