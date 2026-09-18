package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.common.WandSupport;
import dev.tc4port.forgottenrelics.item.base.CurioRelicItem;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.forgottenrelics.registry.FRItems;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Nebulous Core. Slowly trickle-charges carried wands, occasionally teleports
 * its wearer, and very rarely burns out into a Dormant Nebulous Core.
 */
public class ItemArcanum extends CurioRelicItem implements ForgottenGear.VisDiscount {

    public ItemArcanum(Properties properties) {
        super(properties.stacksTo(1), "ItemArcanum");
    }

    @Override
    public void onWornTick(ItemStack stack, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide()) {
            return;
        }

        if (Math.random() <= 0.025D * FRConfig.arcanumGenRate()) {
            List<ItemStack> wands = WandSupport.wandSearch(player);
            if (!wands.isEmpty()) {
                VisChannel channel = VisChannel.values()[(int) (Math.random() * VisChannel.values().length)];
                WandSupport.chargeRandomWand(serverPlayer, wands, channel, 1);
            }
        } else if (Math.random() <= 0.000208D) {
            for (int attempt = 0; attempt < 33; attempt++) {
                double dx = (Math.random() - 0.5D) * 2.0D * 32.0D;
                double dy = (Math.random() - 0.5D) * 2.0D * 32.0D;
                double dz = (Math.random() - 0.5D) * 2.0D * 32.0D;
                if (player.randomTeleport(player.getX() + dx, player.getY() + dy, player.getZ() + dz, true)) {
                    break;
                }
            }
        } else if (Math.random() <= 0.000027D) {
            ItemStack dormant = new ItemStack(FRItems.DORMANT_ARCANUM.get());
            ItemStatePlatform.set(dormant, FRItemState.LIFETIME,
                    (int) ((1200 + Math.random() * 6000) * FRConfig.dormantArcanumVisMult()));
            CuriosApi.getCuriosHelper().setEquippedCurio(player, "necklace", 0, dormant);
        }
    }

    @Override
    public int thaumcraftVisDiscount(ItemStack stack, Player wearer, VisChannel channel) {
        return 35;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemArcanum1.lore")
                .append(Component.literal(" " + this.thaumcraftVisDiscount(stack, null, VisChannel.PERDITIO) + "%")));
        tooltip.add(emptyLine());
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemArcanum2.lore"),
                tr("item.ItemArcanum3.lore"),
                tr("item.ItemArcanum4.lore"),
                tr("item.ItemArcanum5.lore"),
                tr("item.ItemArcanum6.lore"),
                emptyLine(),
                tr("item.ItemArcanum7.lore"),
                tr("item.ItemArcanum8.lore"),
                tr("item.ItemArcanum9.lore"),
                emptyLine(),
                CurioTooltips.necklace()));
    }
}
