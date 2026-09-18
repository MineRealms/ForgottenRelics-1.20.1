package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.data.SupersolidData;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 * Oblivion Stone.
 *
 * <p>The source mod stored numeric item ids plus metadata and encoded its
 * operation mode in the item's damage value. The port stores actual stacks in
 * a data component and keeps the mode (plus the {@code +100} sneak flag) in
 * its own component. The three operation modes are preserved:
 * <ul>
 *   <li>0 - void every stored item type,</li>
 *   <li>1 - keep only the largest stack of each stored type,</li>
 *   <li>2 - when the inventory is completely full, free one slot per stored type.</li>
 * </ul>
 */
public class ItemOblivionStone extends RelicItem implements ForgottenGear.Warping {

    public ItemOblivionStone(Properties properties) {
        super(properties.stacksTo(1), "ItemOblivionStone");
    }

    private static SupersolidData data(ItemStack stack) {
        return ItemStatePlatform.getOrDefault(stack, FRItemState.SUPERSOLID, SupersolidData.EMPTY);
    }

    private static int mode(ItemStack stack) {
        return ItemStatePlatform.getOrDefault(stack, FRItemState.MODE, 0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int current = mode(stack);

        if (player.isShiftKeyDown()) {
            if (current < 100) {
                ItemStatePlatform.set(stack, FRItemState.MODE, current + 100);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                ItemStatePlatform.set(stack, FRItemState.MODE, current - 100);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 1.0F, 1.3F);
            }
        } else {
            int next;
            if (current == 0 || current == 1 || current == 100 || current == 101) {
                next = current + 1;
            } else if (current == 2 || current == 102) {
                next = current - 2;
            } else {
                next = current;
            }
            ItemStatePlatform.set(stack, FRItemState.MODE, next);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F,
                    0.8F + (float) (Math.random() * 0.2F));
        }

        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player) || entity.tickCount % 10 != 0) {
            return;
        }

        int current = mode(stack);
        if (current >= 100) {
            return;
        }

        SupersolidData stored = data(stack);
        if (stored.size() == 0) {
            return;
        }

        consumeStuff(player, stored, current);
    }

    /** Port of the source {@code consumeStuff}. */
    private static void consumeStuff(Player player, SupersolidData stored, int mode) {
        Map<Integer, ItemStack> candidates = new HashMap<>();
        int filled = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.isEmpty()) {
                continue;
            }
            filled++;
            if (!(candidate.getItem() instanceof ItemOblivionStone)) {
                candidates.put(slot, candidate);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        switch (mode) {
            case 0 -> {
                for (ItemStack target : stored.stored()) {
                    for (Map.Entry<Integer, ItemStack> entry : candidates.entrySet()) {
                        if (matches(target, entry.getValue())) {
                            player.getInventory().setItem(entry.getKey(), ItemStack.EMPTY);
                        }
                    }
                }
            }
            case 1 -> {
                for (ItemStack target : stored.stored()) {
                    Map<Integer, ItemStack> matching = new HashMap<>(candidates);
                    matching.entrySet().removeIf(entry -> !matches(target, entry.getValue()));
                    while (matching.size() > 1) {
                        int slot = smallestStackSlot(matching);
                        player.getInventory().setItem(slot, ItemStack.EMPTY);
                        matching.remove(slot);
                    }
                }
            }
            case 2 -> {
                if (filled >= player.getInventory().getContainerSize()) {
                    for (ItemStack target : stored.stored()) {
                        Map<Integer, ItemStack> matching = new HashMap<>(candidates);
                        matching.entrySet().removeIf(entry -> !matches(target, entry.getValue()));
                        if (!matching.isEmpty()) {
                            player.getInventory().setItem(smallestStackSlot(matching), ItemStack.EMPTY);
                            return;
                        }
                    }
                }
            }
            default -> {
            }
        }
    }

    private static boolean matches(ItemStack stored, ItemStack candidate) {
        return ItemStack.isSameItemSameTags(stored, candidate);
    }

    private static int smallestStackSlot(Map<Integer, ItemStack> matching) {
        int bestSlot = -1;
        int bestSize = Integer.MAX_VALUE;
        for (Map.Entry<Integer, ItemStack> entry : matching.entrySet()) {
            int size = entry.getValue().getCount();
            if (size < bestSize || (size == bestSize && entry.getKey() > bestSlot)) {
                bestSize = size;
                bestSlot = entry.getKey();
            }
        }
        return bestSlot;
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        SupersolidData stored = data(stack);
        int mode = mode(stack) % 100;

        tooltip.add(Component.translatable("item.ItemOblivionStoneMode" + (mode + 1) + ".lore"));
        tooltip.add(emptyLine());

        if (stored.size() > 0) {
            List<Component> names = new ArrayList<>();
            for (ItemStack entry : stored.stored()) {
                names.add(entry.getHoverName());
            }
            tooltip.add(Component.translatable("item.ItemOblivionStoneStored.lore").append(Component.literal(" " + stored.size())));
            tooltip.add(Component.literal(String.join(", ", names.stream().map(Component::getString).toList())));
            if (stored.size() > FRConfig.oblivionStoneSoftCap()) {
                tooltip.add(Component.translatable("item.ItemOblivionStoneOverflow.lore"));
            }
        }
    }
}
