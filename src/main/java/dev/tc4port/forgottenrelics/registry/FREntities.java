package dev.tc4port.forgottenrelics.registry;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import dev.tc4port.forgottenrelics.entity.EntityCrimsonOrb;
import dev.tc4port.forgottenrelics.entity.EntityDarkMatterOrb;
import dev.tc4port.forgottenrelics.entity.EntityShinyEnergy;
import dev.tc4port.forgottenrelics.entity.EntitySoulEnergy;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Entity registry. Registry paths use the original 1.7.10 entity names in
 * snake_case.
 */
public final class FREntities {

    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ForgottenRelics.MOD_ID);

    public static final RegistryObject<EntityType<EntityShinyEnergy>> SHINY_ENERGY =
            REGISTRY.register("shiny_energy", () -> EntityType.Builder
                    .<EntityShinyEnergy>of(EntityShinyEnergy::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(6)
                    .updateInterval(10)
                    .build(ForgottenRelics.id("shiny_energy").toString()));

    public static final RegistryObject<EntityType<EntitySoulEnergy>> SOUL_ENERGY =
            REGISTRY.register("soul_energy", () -> EntityType.Builder
                    .<EntitySoulEnergy>of(EntitySoulEnergy::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(6)
                    .updateInterval(10)
                    .build(ForgottenRelics.id("soul_energy").toString()));

    public static final RegistryObject<EntityType<EntityDarkMatterOrb>> DARK_MATTER_ORB =
            REGISTRY.register("dark_matter_orb", () -> EntityType.Builder
                    .<EntityDarkMatterOrb>of(EntityDarkMatterOrb::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build(ForgottenRelics.id("dark_matter_orb").toString()));

    public static final RegistryObject<EntityType<EntityCrimsonOrb>> CRIMSON_ORB =
            REGISTRY.register("crimson_orb", () -> EntityType.Builder
                    .<EntityCrimsonOrb>of(EntityCrimsonOrb::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build(ForgottenRelics.id("crimson_orb").toString()));

    private FREntities() {
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
