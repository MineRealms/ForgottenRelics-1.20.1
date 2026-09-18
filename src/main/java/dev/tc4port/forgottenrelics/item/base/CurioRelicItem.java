package dev.tc4port.forgottenrelics.item.base;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 1.20.1 replacement for the source mod's {@code ItemBaubleBase} (a copy of
 * Botania's Baubles item). Slot assignment is data driven through the
 * {@code curios:ring} / {@code curios:necklace} item tags.
 */
public class CurioRelicItem extends RelicItem implements ICurioItem {

    public CurioRelicItem(Properties properties, String legacyName) {
        super(properties, legacyName);
    }

    public CurioRelicItem(Properties properties, String legacyName, Rarity rarity) {
        super(properties, legacyName, rarity);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack previousStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player && !player.level().isClientSide()) {
            this.onEquippedOrLoadedIntoWorld(stack, player);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player && !player.level().isClientSide()) {
            this.onUnequipped(stack, player);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player) {
            this.onWornTick(stack, player);
        }
    }

    /** Source-side {@code onEquippedOrLoadedIntoWorld}. */
    public void onEquippedOrLoadedIntoWorld(ItemStack stack, Player player) {
    }

    /** Source-side {@code onUnequipped}. */
    public void onUnequipped(ItemStack stack, Player player) {
    }

    /** Source-side {@code onWornTick}. Always called on both logical sides, like Baubles. */
    public void onWornTick(ItemStack stack, Player player) {
    }
}
