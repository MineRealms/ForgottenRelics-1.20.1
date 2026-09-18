package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.wand.WandApi;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Omega Core. Trickle-charges every wand carried by the owner with one unit of
 * each primal vis every tick, matching the source behavior.
 */
public class ItemOmegaCore extends RelicItem {

    public ItemOmegaCore(Properties properties) {
        super(properties.stacksTo(1), "ItemOmegaCore");
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.isEmpty() || candidate == stack || WandApi.view(candidate).isEmpty()) {
                continue;
            }
            for (VisChannel channel : VisChannel.values()) {
                WandApi.insert(player, candidate, VisCost.ofWholeVis(channel, 1), VisAction.EXECUTE);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.OmegaCore1.lore"),
                tr("item.OmegaCore2.lore")));
    }
}
