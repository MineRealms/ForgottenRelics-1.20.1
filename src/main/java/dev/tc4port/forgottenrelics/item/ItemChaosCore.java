package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Chaos Core. Occasionally applies a random effect - usually helpful, but
 * notably never the two instant-effect slots (which the source remapped onto
 * Wither).
 */
public class ItemChaosCore extends RelicItem implements ForgottenGear.Warping {

    /** Legacy {@code Potion.potionTypes} order used by the source mod. */
    private static final List<MobEffect> LEGACY_EFFECTS = List.of(
            MobEffects.MOVEMENT_SPEED,        // 1
            MobEffects.MOVEMENT_SLOWDOWN,     // 2
            MobEffects.DIG_SPEED,             // 3
            MobEffects.DIG_SLOWDOWN,          // 4
            MobEffects.DAMAGE_BOOST,          // 5
            MobEffects.WITHER,                // 6 -> remapped by source
            MobEffects.WITHER,                // 7 -> remapped by source
            MobEffects.JUMP,                  // 8
            MobEffects.CONFUSION,             // 9
            MobEffects.REGENERATION,          // 10
            MobEffects.DAMAGE_RESISTANCE,     // 11
            MobEffects.FIRE_RESISTANCE,       // 12
            MobEffects.WATER_BREATHING,       // 13
            MobEffects.INVISIBILITY,          // 14
            MobEffects.BLINDNESS,             // 15
            MobEffects.NIGHT_VISION,          // 16
            MobEffects.HUNGER,                // 17
            MobEffects.WEAKNESS,              // 18
            MobEffects.POISON,                // 19
            MobEffects.WITHER,                // 20
            MobEffects.HEALTH_BOOST);         // 21

    public ItemChaosCore(Properties properties) {
        super(properties.stacksTo(1), "ItemChaosCore");
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (Math.random() <= 0.000208D) {
            int index = 1 + (int) (Math.random() * 21);
            MobEffect effect = LEGACY_EFFECTS.get(Math.min(index, LEGACY_EFFECTS.size()) - 1);
            living.addEffect(new MobEffectInstance(effect, 100 + (int) (Math.random() * 2400), (int) (Math.random() * 3), false, true));
        }
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemChaosCore1.lore"),
                tr("item.ItemChaosCore2.lore"),
                tr("item.ItemChaosCore3.lore"),
                emptyLine(),
                tr("item.ItemChaosCore4.lore"),
                tr("item.ItemChaosCore5.lore"),
                tr("item.ItemChaosCore6.lore"),
                emptyLine(),
                tr("item.ItemChaosCore7.lore")));
        tooltip.add(emptyLine());
    }
}
