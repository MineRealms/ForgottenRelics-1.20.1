package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
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
 * The Paradox. Melee damage is replaced by a random gamble split between the
 * target and the wielder, exactly as in the source mod.
 */
public class ItemParadox extends SwordItem implements ForgottenGear.Warping, ForgottenGear.Repairable {

    public ItemParadox(Properties properties) {
        super(FRToolTiers.PARADOXICAL_STUFF, 3, -2.4F, properties);
    }

    @Override
    public String getDescriptionId() {
        return "item.ItemParadox";
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        double cap = FRConfig.paradoxDamageCap();
        double gamble = Math.random() * cap;
        entity.hurt(player.damageSources().playerAttack(player), (float) gamble);
        player.hurt(player.damageSources().playerAttack(player), (float) (cap - gamble));
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && stack.isDamaged() && entity.tickCount % 20 == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 8;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicText.addShiftTooltip(tooltip, List.of(
                RelicText.tr("item.ItemParadox1.lore"),
                RelicText.tr("item.ItemParadox2.lore"),
                RelicText.tr("item.ItemParadox3.lore"),
                RelicText.tr("item.ItemParadox4.lore"),
                RelicText.tr("item.ItemParadox5.lore")));
        tooltip.add(RelicText.emptyLine());
        tooltip.add(Component.translatable("item.ItemParadoxDamage_1.lore")
                .append(Component.literal(String.valueOf((int) FRConfig.paradoxDamageCap())))
                .append(Component.translatable("item.ItemParadoxDamage_2.lore")));
    }
}
