package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.item.base.CurioRelicItem;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.forgottenrelics.registry.FRItems;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.forgottenrelics.common.FRCasting;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Dormant Nebulous Core. Counts down while six-channel vis is available, then
 * turns back into a Nebulous Core.
 */
public class ItemDormantArcanum extends CurioRelicItem {

    private static final VisCost UPKEEP = VisCost.ofCentivis(Map.of(
            VisChannel.AER, 3,
            VisChannel.IGNIS, 3,
            VisChannel.TERRA, 3,
            VisChannel.AQUA, 3,
            VisChannel.ORDO, 3,
            VisChannel.PERDITIO, 3));

    public ItemDormantArcanum(Properties properties) {
        super(properties.stacksTo(1), "ItemDormantArcanum");
    }

    @Override
    public void onWornTick(ItemStack stack, Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        if (!ItemStatePlatform.has(stack, FRItemState.LIFETIME)) {
            return;
        }

        int lifetime = ItemStatePlatform.getOrDefault(stack, FRItemState.LIFETIME, 0);
        if (lifetime > 0) {
            if (FRCasting.pay(player, UPKEEP)) {
                ItemStatePlatform.set(stack, FRItemState.LIFETIME, lifetime - 1);
            }
        } else {
            CuriosApi.getCuriosHelper().setEquippedCurio(player, "necklace", 0, new ItemStack(FRItems.ARCANUM.get()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(tr("item.ItemDormantArcanum1.lore")));
        if (ItemStatePlatform.has(stack, FRItemState.LIFETIME)) {
            int lifetime = ItemStatePlatform.getOrDefault(stack, FRItemState.LIFETIME, 0);
            tooltip.add(emptyLine());
            tooltip.add(Component.translatable("item.FRCode6.lore")
                    .append(Component.literal(String.valueOf((lifetime * 2) / 100.0D)))
                    .append(Component.translatable("item.ItemDormantArcanum2.lore")));
        }
    }
}
