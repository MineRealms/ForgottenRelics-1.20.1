package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.item.base.FRToolTiers;
import dev.tc4port.forgottenrelics.item.base.RelicText;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Wastelayer. The source release shipped this item unfinished (a placeholder
 * hit hook); behavior is intentionally left identical instead of inventing
 * something new.
 */
public class ItemWastelayer extends SwordItem implements ForgottenGear.Warping, ForgottenGear.Repairable {

    public ItemWastelayer(Properties properties) {
        super(FRToolTiers.PARADOXICAL_STUFF, 3, -2.4F, properties);
    }

    @Override
    public String getDescriptionId() {
        return "item.ItemWastelayer";
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return true;
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 10;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicText.addShiftTooltip(tooltip, List.of(
                RelicText.tr("item.ItemWastelayer1.lore"),
                RelicText.tr("item.ItemWastelayer2.lore"),
                RelicText.tr("item.ItemWastelayer3.lore")));
    }
}
