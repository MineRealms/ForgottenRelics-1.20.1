package dev.tc4port.forgottenrelics.item;

import net.minecraft.network.chat.Component;

/** Shared Curios slot tooltip helpers replacing Botania's {@code botaniamisc} lines. */
public final class CurioTooltips {

    private CurioTooltips() {
    }

    public static Component necklace() {
        return Component.translatable("curios.slot.necklace");
    }

    public static Component ring() {
        return Component.translatable("curios.slot.ring");
    }

    public static Component belt() {
        return Component.translatable("curios.slot.belt");
    }

    public static Component charm() {
        return Component.translatable("curios.slot.charm");
    }
}
