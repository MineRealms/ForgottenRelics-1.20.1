package dev.tc4port.forgottenrelics.recipe;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.data.SupersolidData;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.forgottenrelics.registry.FRItems;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.Level;

/**
 * Oblivion Stone binding recipe: one stone plus exactly one other stack.
 *
 * <p>The source implementation compared numeric item ids and metadata
 * ({@code Item.itemRegistry.getIDForObject(...)}). This port compares tags of
 * stacks; the stone itself stores them through its {@code forgottenrelics:supersolid}
 * item state field.</p>
 */
public class FROblivionRecipe implements CraftingRecipe {

    public static final Serializer SERIALIZER = new Serializer();

    private final net.minecraft.resources.ResourceLocation id;
    private final CraftingBookCategory category;

    public FROblivionRecipe(net.minecraft.resources.ResourceLocation id, CraftingBookCategory category) {
        this.id = id;
        this.category = category;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getId() {
        return this.id;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack stone = null;
        ItemStack ingredient = null;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(FRItems.OBLIVION_STONE.get())) {
                if (stone != null) {
                    return false;
                }
                stone = stack;
            } else {
                if (ingredient != null) {
                    return false;
                }
                ingredient = stack;
            }
        }

        if (stone == null) {
            return false;
        }

        if (ingredient == null) {
            // Emptying call: always valid.
            return true;
        }

        SupersolidData stored = ItemStatePlatform.getOrDefault(stone, FRItemState.SUPERSOLID, SupersolidData.EMPTY);
        if (stored.size() >= FRConfig.oblivionStoneHardCap()) {
            return false;
        }
        return !stored.contains(ingredient);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack stone = null;
        ItemStack ingredient = null;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(FRItems.OBLIVION_STONE.get())) {
                stone = stack;
            } else {
                ingredient = stack;
            }
        }

        if (stone == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = stone.copyWithCount(1);
        SupersolidData stored = ItemStatePlatform.getOrDefault(stone, FRItemState.SUPERSOLID, SupersolidData.EMPTY);
        if (ingredient != null) {
            ItemStatePlatform.set(result, FRItemState.SUPERSOLID, stored.with(ingredient));
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(FRItems.OBLIVION_STONE.get());
    }

    @Override
    public boolean isSpecial() {
        // Dynamic result; the recipe book must not treat it as a visible recipe.
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public static final class Serializer extends net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<FROblivionRecipe> {
        public Serializer() {
            super(FROblivionRecipe::new);
        }
    }
}
