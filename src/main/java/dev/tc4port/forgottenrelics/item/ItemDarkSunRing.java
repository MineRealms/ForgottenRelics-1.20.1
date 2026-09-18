package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.item.base.CurioRelicItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Ring of The Seven Suns. The damage cap and deflection stored in the source
 * event handler are handled elsewhere; the wearable part extinguishes the
 * carrier.
 */
public class ItemDarkSunRing extends CurioRelicItem {

    public ItemDarkSunRing(Properties properties) {
        super(properties.stacksTo(1), "ItemDarkSunRing");
    }

    @Override
    public void onWornTick(ItemStack stack, Player player) {
        if (player.isOnFire()) {
            player.clearFire();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemDarkSunRing1.lore"),
                Component.translatable("item.ItemDarkSunRing2_1.lore")
                        .append(Component.literal(" " + (int) FRConfig.darkSunRingDamageCap()))
                        .append(Component.translatable("item.ItemDarkSunRing2_2.lore")),
                tr("item.ItemDarkSunRing3.lore"),
                emptyLine(),
                Component.translatable("item.ItemDarkSunRing4_1.lore")
                        .append(Component.literal(" " + (int) (FRConfig.darkSunRingDeflectChance() * 100.0D)))
                        .append(Component.translatable("item.ItemDarkSunRing4_2.lore")),
                tr("item.ItemDarkSunRing5.lore"),
                emptyLine(),
                tr("item.ItemDarkSunRing6.lore"),
                tr("item.ItemDarkSunRing7.lore"),
                emptyLine(),
                tr("item.ItemDarkSunRing8.lore"),
                tr("item.ItemDarkSunRing9.lore"),
                emptyLine(),
                CurioTooltips.ring()));
    }
}
