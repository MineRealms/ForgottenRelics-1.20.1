package dev.tc4port.forgottenrelics.common;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Player inventory search helpers replacing the source
 * {@code SuperpositionHandler.itemSearch}, {@code findFirst} and
 * {@code hasItem} calls.
 */
public final class ItemSearch {

    private ItemSearch() {
    }

    public static boolean hasItem(Player player, Item item) {
        return findFirst(player, item) != null;
    }

    /** All matching stacks in main inventory, offhand and hands. */
    public static List<ItemStack> search(Player player, Item item) {
        List<ItemStack> matches = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                matches.add(stack);
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.is(item)) {
            matches.add(offhand);
        }
        return matches;
    }

    public static ItemStack findFirst(Player player, Item item) {
        List<ItemStack> matches = search(player, item);
        return matches.isEmpty() ? null : matches.get(0);
    }
}
