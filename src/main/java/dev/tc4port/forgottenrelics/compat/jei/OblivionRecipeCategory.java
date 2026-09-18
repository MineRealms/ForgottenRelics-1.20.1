package dev.tc4port.forgottenrelics.compat.jei;

import dev.tc4port.forgottenrelics.registry.FRItems;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Shows the dynamic Oblivion Stone binding: stone + any item -> charged stone.
 */
public final class OblivionRecipeCategory implements IRecipeCategory<ForgottenRelicsJeiPlugin.OblivionJeiRecipe> {

    private final IDrawable background;
    private final IDrawable icon;

    public OblivionRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 54);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(FRItems.OBLIVION_STONE.get()));
    }

    @Override
    public RecipeType<ForgottenRelicsJeiPlugin.OblivionJeiRecipe> getRecipeType() {
        return ForgottenRelicsJeiPlugin.OBLIVION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("forgottenrelics.jei.oblivion.title");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForgottenRelicsJeiPlugin.OblivionJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(24, 18).addItemStack(new ItemStack(FRItems.OBLIVION_STONE.get()));
        builder.addInputSlot(56, 18).addIngredients(mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                net.minecraft.core.registries.BuiltInRegistries.ITEM.stream().map(ItemStack::new).toList());
        builder.addOutputSlot(96, 18).addItemStack(new ItemStack(FRItems.OBLIVION_STONE.get()));
    }

    @Override
    public void draw(ForgottenRelicsJeiPlugin.OblivionJeiRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                "+", 40, 24, 0x404040, false);
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font,
                "=", 80, 24, 0x404040, false);
    }

    @Override
    public List<Component> getTooltipStrings(ForgottenRelicsJeiPlugin.OblivionJeiRecipe recipe, IRecipeSlotsView slotsView, double mouseX, double mouseY) {
        return List.of();
    }
}
