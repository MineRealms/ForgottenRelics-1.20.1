package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.item.base.CurioRelicItem;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Amulet of The Oblivion. Slowly "burns off" damage stored in the amulet and,
 * alternatively, may inflict one of four negative effects on the wearer.
 * Both outcomes are reproduced from the source mod.
 */
public class ItemOblivionAmulet extends CurioRelicItem implements dev.tc4port.forgottenrelics.api.ForgottenGear.Warping {

    public ItemOblivionAmulet(Properties properties) {
        super(properties.stacksTo(1), "ItemOblivionAmulet");
    }

    @Override
    public void onWornTick(ItemStack stack, Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        float stored = ItemStatePlatform.getOrDefault(stack, FRItemState.STORED_DAMAGE, 0.0F);

        if (stored > 0.0F && Math.random() <= 0.0008D) {
            float rolled = (float) (stored * Math.random());
            if (rolled > 100.0F && Math.random() <= 0.9D) {
                rolled = (float) (100.0D * Math.random());
            }
            ItemStatePlatform.set(stack, FRItemState.STORED_DAMAGE, Math.max(0.0F, stored - rolled));
            player.hurt(FRDamageTypes.oblivion(player.level()), rolled);
        } else if (Math.random() <= 0.0004D) {
            double roll = Math.random();
            MobEffectInstance effect;
            if (roll <= 0.25D) {
                effect = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100 + (int) (Math.random() * 2000), (int) (Math.random() * 3), true, true);
            } else if (roll <= 0.5D) {
                effect = new MobEffectInstance(MobEffects.BLINDNESS, 100 + (int) (Math.random() * 2000), (int) (Math.random() * 3), true, true);
            } else if (roll <= 0.75D) {
                effect = new MobEffectInstance(MobEffects.WEAKNESS, 100 + (int) (Math.random() * 2000), (int) (Math.random() * 3), true, true);
            } else {
                effect = new MobEffectInstance(MobEffects.WITHER, 100 + (int) (Math.random() * 2000), (int) (Math.random() * 3), true, true);
            }
            player.addEffect(effect);
        }
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 4;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemOblivionAmulet1.lore"),
                tr("item.ItemOblivionAmulet2.lore"),
                tr("item.ItemOblivionAmulet3.lore"),
                emptyLine(),
                tr("item.ItemOblivionAmulet4.lore"),
                emptyLine(),
                tr("item.ItemOblivionAmulet5.lore"),
                emptyLine(),
                CurioTooltips.necklace()));
        tooltip.add(emptyLine());
        float stored = ItemStatePlatform.getOrDefault(stack, FRItemState.STORED_DAMAGE, 0.0F);
        if (stored > 0.0F) {
            tooltip.add(Component.translatable("item.ItemOblivionAmuletDamage.lore")
                    .append(Component.literal(" " + Math.round(stored * 100.0F) / 100.0F)));
            tooltip.add(emptyLine());
        }
    }
}
