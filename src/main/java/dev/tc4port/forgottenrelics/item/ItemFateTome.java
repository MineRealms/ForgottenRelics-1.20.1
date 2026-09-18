package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Tome of Broken Fates. Books keep a persistent per-copy identifier and a
 * cooldown, exactly like the source NBT contract. The actual fate effects are
 * driven by the shared casting systems and land in a later port pass.
 */
public class ItemFateTome extends RelicItem implements ForgottenGear.Warping {

    public ItemFateTome(Properties properties) {
        super(properties.stacksTo(1), "ItemFateTome");
    }

    public static int aerCost() {
        return (int) (10000 * FRConfig.fateTomeVisMult());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player)) {
            return;
        }

        if (!ItemStatePlatform.has(stack, FRItemState.FATE_ID)) {
            ItemStatePlatform.set(stack, FRItemState.FATE_ID, (int) (Math.random() * Integer.MAX_VALUE));
            ItemStatePlatform.set(stack, FRItemState.COOLDOWN, 0);
            return;
        }

        int cooldown = ItemStatePlatform.getOrDefault(stack, FRItemState.COOLDOWN, 0);
        if (cooldown > 0) {
            ItemStatePlatform.set(stack, FRItemState.COOLDOWN, cooldown - 1);
        }
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 7;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addVisCostTooltip(tooltip, List.of(
                tr("item.ItemFateTome1.lore"),
                tr("item.ItemFateTome2.lore"),
                tr("item.ItemFateTome3.lore"),
                tr("item.ItemFateTome4.lore"),
                emptyLine(),
                tr("item.ItemFateTome6.lore"),
                tr("item.ItemFateTome7.lore"),
                emptyLine(),
                tr("item.ItemFateTome8.lore"),
                tr("item.ItemFateTome9.lore")));

        int cooldown = ItemStatePlatform.getOrDefault(stack, FRItemState.COOLDOWN, 0);
        if (cooldown > 0) {
            tooltip.add(emptyLine());
            double seconds = BigDecimal.valueOf(cooldown / 20.0D).setScale(1, RoundingMode.HALF_UP).doubleValue();
            tooltip.add(tr("item.ItemFateTomeCooldown.lore").copy().append(Component.literal(" " + seconds + " ")).append(tr("item.FRSeconds.lore")));
        }
    }
}
