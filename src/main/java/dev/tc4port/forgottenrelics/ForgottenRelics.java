package dev.tc4port.forgottenrelics;

import com.mojang.logging.LogUtils;
import dev.tc4port.forgottenrelics.common.FRJusticeEvents;
import dev.tc4port.forgottenrelics.common.FRResearchCommand;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.registry.FRCreativeTabs;
import dev.tc4port.forgottenrelics.registry.FREntities;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.forgottenrelics.registry.FRItems;
import dev.tc4port.forgottenrelics.registry.FRRecipes;
import dev.tc4port.forgottenrelics.research.ForgottenKnowledge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Forgotten Relics, reconstructed for Minecraft 1.20.1 / Forge.
 *
 * <p>The original 1.7.10 mod was a Thaumcraft 4 addon. This build targets the
 * Thaumcraft 4R reconstruction ({@code dev.tc4port.thaumcraft}) and replaces
 * Baubles with Curios and MineTweaker3 with KubeJS.</p>
 */
@Mod(ForgottenRelics.MOD_ID)
public final class ForgottenRelics {

    public static final String MOD_ID = "forgottenrelics";

    public static final Logger LOG = LogUtils.getLogger();

    public ForgottenRelics() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FRConfig.SPEC);

        FRItemState.register(modBus);
        FRItems.register(modBus);
        FREntities.register(modBus);
        FRRecipes.register(modBus);
        FRCreativeTabs.register(modBus);

        FRNetwork.register();
        ForgottenKnowledge.init();
        MinecraftForge.EVENT_BUS.register(new FRJusticeEvents());
        MinecraftForge.EVENT_BUS.register(new FRResearchCommand());
        MinecraftForge.EVENT_BUS.register(new dev.tc4port.forgottenrelics.common.FRGameplayEvents());
    }

    /** Kept for parity with the source-side package separations. */
    public static net.minecraft.resources.ResourceLocation id(String path) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /** Side guard used by common code that touches client-only registries. */
    public static boolean isClient() {
        return net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT;
    }
}
