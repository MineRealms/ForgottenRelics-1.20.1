package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.item.base.CurioRelicItem;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Deific Amulet. Cleanses harmful effects, extinguishes the wearer, refills the
 * air supply for vis and (optionally) grants the source mod's damage
 * resistance window.
 */
public class ItemDeificAmulet extends CurioRelicItem {

    public ItemDeificAmulet(Properties properties) {
        super(properties.stacksTo(1), "ItemDeificAmulet");
    }

    @Override
    public void onWornTick(ItemStack stack, Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        if (FRConfig.deificAmuletEffectImmunity() && !player.getActiveEffects().isEmpty()) {
            if (FRConfig.deificAmuletOnlyNegatesDebuffs()) {
                List<MobEffectInstance> effects = List.copyOf(player.getActiveEffects());
                for (MobEffectInstance effect : effects) {
                    if (!effect.getEffect().isBeneficial()) {
                        player.removeEffect(effect.getEffect());
                    }
                }
            } else {
                player.removeAllEffects();
            }
        }

        if (player.isOnFire()) {
            player.clearFire();
        }

        if (player.getAirSupply() <= 0) {
            int cost = (int) (1000 * FRConfig.deificAmuletVisMult());
            VisCost vis = VisCost.ofCentivis(java.util.Map.of(VisChannel.AER, cost, VisChannel.AQUA, cost));
            if (ThaumcraftApiHelper.consumeVisFromInventory(player, vis, VisAction.EXECUTE).consumed()) {
                player.setAirSupply(300);
            }
        }

        if (FRConfig.deificAmuletInvincibility()) {
            int cooldown = ItemStatePlatform.getOrDefault(stack, FRItemState.COOLDOWN, 0);
            if (cooldown == 0 && player.invulnerableTime > 10) {
                player.invulnerableTime = 40;
                ItemStatePlatform.set(stack, FRItemState.COOLDOWN, 32);
            } else if (cooldown > 0) {
                ItemStatePlatform.set(stack, FRItemState.COOLDOWN, cooldown - 1);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<Component> lines = new java.util.ArrayList<>();
        if (FRConfig.deificAmuletEffectImmunity()) {
            lines.add(tr(FRConfig.deificAmuletOnlyNegatesDebuffs()
                    ? "item.ItemDeificAmulet1_alt.lore"
                    : "item.ItemDeificAmulet1.lore"));
        }
        lines.add(tr("item.ItemDeificAmulet2.lore"));
        if (FRConfig.deificAmuletInvincibility()) {
            lines.add(tr("item.ItemDeificAmulet3.lore"));
        }
        lines.add(emptyLine());
        lines.add(tr("item.ItemDeificAmulet4.lore"));
        lines.add(tr("item.ItemDeificAmulet5.lore"));
        lines.add(emptyLine());
        lines.add(CurioTooltips.necklace());
        addShiftTooltip(tooltip, lines);
    }
}
