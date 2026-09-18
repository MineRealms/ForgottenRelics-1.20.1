package dev.tc4port.forgottenrelics.compat.jei;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import dev.tc4port.forgottenrelics.registry.FRItems;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI integration for the Oblivion Stone binding recipe.
 *
 * <p>Infusion recipes contributed by this mod are data-driven and are already
 * rendered by Thaumcraft 4R's own JEI plugin, so only the special crafting
 * recipe needs a dedicated category here.</p>
 */
@JeiPlugin
public final class ForgottenRelicsJeiPlugin implements IModPlugin {

    public static final RecipeType<OblivionJeiRecipe> OBLIVION_TYPE =
            RecipeType.create(ForgottenRelics.MOD_ID, "oblivion_stone", OblivionJeiRecipe.class);

    private static final ResourceLocation UID = ForgottenRelics.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new OblivionRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(OBLIVION_TYPE, List.of(new OblivionJeiRecipe()));
        registration.addIngredientInfo(new ItemStack(FRItems.OBLIVION_STONE.get()), mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                Component.translatable("forgottenrelics.jei.oblivion.info"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(FRItems.OBLIVION_STONE.get()), OBLIVION_TYPE);
    }

    /** Marker record; the actual behavior is dynamic and cannot be enumerated. */
    public record OblivionJeiRecipe() {
    }
}
