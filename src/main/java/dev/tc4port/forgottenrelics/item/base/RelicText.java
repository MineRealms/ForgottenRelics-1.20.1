package dev.tc4port.forgottenrelics.item.base;

import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Tooltip helpers usable by every relic base class (including {@code SwordItem}
 * subclasses, which cannot extend {@link RelicItem}).
 */
public final class RelicText {

    private RelicText() {
    }

    public static Component tr(String key) {
        return Component.translatable(key);
    }

    public static Component emptyLine() {
        return Component.translatable("item.FREmpty.lore");
    }

    public static void addShiftTooltip(List<Component> tooltip, List<Component> shiftLines) {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.addAll(shiftLines);
        } else {
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore"));
        }
    }

    public static void addVisCostTooltip(List<Component> tooltip, List<Component> shiftLines) {
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(Component.translatable("item.FRVisPerCast.lore"));
            tooltip.addAll(shiftLines);
            return;
        }
        addShiftTooltip(tooltip, shiftLines);
    }
}
