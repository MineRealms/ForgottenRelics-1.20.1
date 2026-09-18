package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.common.ExperienceMath;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * XP Tome. Stores experience in configurable five-point steps, either
 * absorbing from the holder or extracting back into them.
 */
public class ItemXPTome extends RelicItem {

    public static final int XP_PORTION = 5;

    public ItemXPTome(Properties properties) {
        super(properties.stacksTo(1), "ItemXPTome");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            boolean absorption = ItemStatePlatform.getOrDefault(stack, FRItemState.ABSORPTION, true);
            ItemStatePlatform.set(stack, FRItemState.ABSORPTION, !absorption);
        } else {
            boolean active = ItemStatePlatform.getOrDefault(stack, FRItemState.ACTIVE, false);
            ItemStatePlatform.set(stack, FRItemState.ACTIVE, !active);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 0.4F + (float) (Math.random() * 0.1F));
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (!ItemStatePlatform.getOrDefault(stack, FRItemState.ACTIVE, false)) {
            return;
        }

        boolean absorption = ItemStatePlatform.getOrDefault(stack, FRItemState.ABSORPTION, true);
        int stored = ItemStatePlatform.getOrDefault(stack, FRItemState.XP_STORED, 0);

        if (absorption) {
            int available = ExperienceMath.getPlayerXp(player);
            int moved = Math.min(XP_PORTION, available);
            if (moved > 0) {
                ExperienceMath.drainPlayerXp(player, moved);
                ItemStatePlatform.set(stack, FRItemState.XP_STORED, stored + moved);
                player.containerMenu.broadcastChanges();
            }
        } else if (stored > 0) {
            int moved = Math.min(XP_PORTION, stored);
            ItemStatePlatform.set(stack, FRItemState.XP_STORED, stored - moved);
            ExperienceMath.addPlayerXp(player, moved);
            player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ItemStatePlatform.getOrDefault(stack, FRItemState.ACTIVE, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean active = ItemStatePlatform.getOrDefault(stack, FRItemState.ACTIVE, false);
        boolean absorption = ItemStatePlatform.getOrDefault(stack, FRItemState.ABSORPTION, true);
        Component mode = active
                ? tr(absorption ? "item.ItemXPTomeAbsorption.lore" : "item.ItemXPTomeExtraction.lore")
                : tr("item.ItemXPTomeDeactivated.lore");

        addShiftTooltip(tooltip, List.of(
                tr("item.ItemXPTome1.lore"),
                emptyLine(),
                tr("item.ItemXPTome2.lore"),
                tr("item.ItemXPTome3.lore"),
                tr("item.ItemXPTome4.lore"),
                emptyLine(),
                tr("item.ItemXPTome5.lore"),
                tr("item.ItemXPTome6.lore"),
                emptyLine(),
                tr("item.ItemXPTome7.lore"),
                tr("item.ItemXPTome8.lore"),
                tr("item.ItemXPTome9.lore")));

        int stored = ItemStatePlatform.getOrDefault(stack, FRItemState.XP_STORED, 0);
        tooltip.add(emptyLine());
        tooltip.add(Component.translatable("item.ItemXPTomeMode.lore").append(Component.literal(" ")).append(mode));
        tooltip.add(emptyLine());
        tooltip.add(tr("item.ItemXPTomeExp.lore"));
        tooltip.add(Component.translatable("item.FRCode6.lore")
                .append(Component.literal(stored + " "))
                .append(Component.translatable("item.ItemXPTomeUnits.lore"))
                .append(Component.literal(" " + ExperienceMath.levelForTotalXp(stored) + " "))
                .append(Component.translatable("item.ItemXPTomeLevels.lore")));
    }
}
