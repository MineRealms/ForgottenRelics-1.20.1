package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * False Justice. The True Damage conversion itself is handled by the damage
 * event hook ({@code FRJusticeEvents}); this item only exposes warp and the
 * source tooltips.
 */
public class ItemFalseJustice extends RelicItem implements ForgottenGear.Warping {

    public ItemFalseJustice(Properties properties) {
        super(properties.stacksTo(1), "ItemFalseJustice");
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 4;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemFalseJustice1.lore"),
                tr("item.ItemFalseJustice2.lore"),
                tr("item.ItemFalseJustice3.lore")));
        tooltip.add(emptyLine());
    }
}
