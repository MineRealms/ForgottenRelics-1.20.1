package dev.tc4port.forgottenrelics.data;

import com.mojang.serialization.Codec;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Contents of an Oblivion Stone.
 *
 * <p>TC-era NBT kept two parallel {@code int[]} arrays of numeric item ids and
 * metadata. In the port the same information is one canonical stack per stored
 * item type, which also survives component-sensitive identities such as
 * enchanted books.</p>
 */
public record SupersolidData(List<ItemStack> stored) {

    public static final SupersolidData EMPTY = new SupersolidData(List.of());

    public static final Codec<SupersolidData> CODEC =
            ItemStatePlatform.OPTIONAL_STACK_CODEC.listOf().xmap(SupersolidData::new, SupersolidData::stored);

    public SupersolidData {
        stored = List.copyOf(stored);
    }

    public int size() {
        return this.stored.size();
    }

    public boolean contains(ItemStack candidate) {
        for (ItemStack stack : this.stored) {
            if (ItemStack.isSameItemSameTags(stack, candidate)) {
                return true;
            }
        }
        return false;
    }

    public SupersolidData with(ItemStack added) {
        List<ItemStack> next = new ArrayList<>(this.stored);
        next.add(added.copyWithCount(1));
        return new SupersolidData(next);
    }

    /** Identity signature used for the config-driven caps. */
    public List<ResourceLocation> itemIds() {
        List<ResourceLocation> ids = new ArrayList<>(this.stored.size());
        this.stored.forEach(stack -> ids.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())));
        return ids;
    }
}
