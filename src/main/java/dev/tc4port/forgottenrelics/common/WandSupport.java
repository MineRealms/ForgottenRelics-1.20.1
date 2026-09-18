package dev.tc4port.forgottenrelics.common;

import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.wand.WandApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Wand helpers replacing the source {@code SuperpositionHandler.wandSearch} /
 * {@code getRandomValidWand}.
 */
public final class WandSupport {

    private WandSupport() {
    }

    /** Every wand carried in the player's main inventory. */
    public static List<ItemStack> wandSearch(Player player) {
        List<ItemStack> wands = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (!candidate.isEmpty() && WandApi.view(candidate).isPresent()) {
                wands.add(candidate);
            }
        }
        return wands;
    }

    /**
     * Charges one random wand that can actually accept the given channel.
     * Returns true when any wand was charged. Source parity: only one wand is
     * touched per attempt.
     */
    public static boolean chargeRandomWand(ServerPlayer player, List<ItemStack> wands, VisChannel channel, int wholeVis) {
        List<ItemStack> shuffled = new ArrayList<>(wands);
        Collections.shuffle(shuffled);
        VisCost cost = VisCost.ofWholeVis(channel, wholeVis);
        for (ItemStack wand : shuffled) {
            if (WandApi.insert(player, wand, cost, VisAction.EXECUTE).mutated()) {
                return true;
            }
        }
        return false;
    }
}
