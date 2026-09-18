package dev.tc4port.forgottenrelics.registry;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import dev.tc4port.forgottenrelics.recipe.FROblivionRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class FRRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ForgottenRelics.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> OBLIVION_CRAFTING =
            REGISTRY.register("oblivion_crafting", () -> FROblivionRecipe.SERIALIZER);

    private FRRecipes() {
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
