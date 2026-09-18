package dev.tc4port.forgottenrelics.client;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import dev.tc4port.forgottenrelics.registry.FREntities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Entity renderer registration for all projectile entities. */
@Mod.EventBusSubscriber(modid = ForgottenRelics.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FREntityRenderers {

    private FREntityRenderers() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FREntities.SHINY_ENERGY.get(),
                context -> new SimpleItemEntityRenderer<>(context, new ItemStack(Items.AMETHYST_SHARD), 0.6F, false));
        event.registerEntityRenderer(FREntities.SOUL_ENERGY.get(),
                context -> new SimpleItemEntityRenderer<>(context, new ItemStack(Items.GHAST_TEAR), 0.6F, false));
        event.registerEntityRenderer(FREntities.DARK_MATTER_ORB.get(),
                context -> new SimpleItemEntityRenderer<>(context, new ItemStack(Items.ENDER_EYE), 0.9F, true));
        event.registerEntityRenderer(FREntities.CRIMSON_ORB.get(),
                context -> new SimpleItemEntityRenderer<>(context, new ItemStack(Items.FIRE_CHARGE), 1.1F, true));
        event.registerEntityRenderer(FREntities.CHAOTIC_ORB.get(),
                context -> new SimpleItemEntityRenderer<>(context, new ItemStack(Items.ENDER_PEARL), 0.8F, true));
        event.registerEntityRenderer(FREntities.LUNAR_FLARE.get(),
                context -> new SimpleItemEntityRenderer<>(context, new ItemStack(Items.SOUL_TORCH), 0.7F, false));
        event.registerEntityRenderer(FREntities.RAGEOUS_MISSILE.get(),
                context -> new SimpleItemEntityRenderer<>(context, new ItemStack(Items.FIREWORK_ROCKET), 0.8F, false));
        event.registerEntityRenderer(FREntities.THUNDERPEAL_ORB.get(),
                context -> new SimpleItemEntityRenderer<>(context, new ItemStack(Items.LIGHTNING_ROD), 0.8F, true));
        event.registerEntityRenderer(FREntities.BABYLON_WEAPON.get(),
                context -> new SimpleItemEntityRenderer<>(context, new ItemStack(Items.NETHERITE_SWORD), 1.6F, true));
    }
}
