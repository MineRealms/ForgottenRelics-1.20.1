package dev.tc4port.forgottenrelics.item.base;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Shared behavior of the source mod's {@code Item} subclasses.
 *
 * <p>Translation keys are intentionally kept in the original
 * {@code item.<LegacyName>.name} form so the untouched en_US/ru_RU lang files
 * from the 1.7.10 release keep working.</p>
 */
public class RelicItem extends Item {

    private final String legacyName;
    private final Rarity rarity;

    public RelicItem(Properties properties, String legacyName) {
        this(properties, legacyName, Rarity.EPIC);
    }

    public RelicItem(Properties properties, String legacyName, Rarity rarity) {
        super(properties);
        this.legacyName = legacyName;
        this.rarity = rarity;
    }

    public String legacyName() {
        return this.legacyName;
    }

    @Override
    public String getDescriptionId() {
        return "item." + this.legacyName;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return this.rarity;
    }

    /** Shift-gated tooltip in the style of the source mod. */
    protected static void addShiftTooltip(List<Component> tooltip, List<Component> shiftLines) {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.addAll(shiftLines);
        } else {
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore"));
        }
    }

    protected static void addVisCostTooltip(List<Component> tooltip, List<Component> shiftLines) {
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(Component.translatable("item.FRVisPerCast.lore"));
            tooltip.addAll(shiftLines);
            return;
        }
        addShiftTooltip(tooltip, shiftLines);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
    }

    protected static Component tr(String key) {
        return Component.translatable(key);
    }

    protected static Component emptyLine() {
        return Component.translatable("item.FREmpty.lore");
    }
}
