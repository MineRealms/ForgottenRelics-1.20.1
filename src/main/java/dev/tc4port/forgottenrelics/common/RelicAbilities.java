package dev.tc4port.forgottenrelics.common;

import dev.tc4port.forgottenrelics.api.FRAbilities;
import dev.tc4port.forgottenrelics.registry.FRItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Server-side ability dispatch for relic activations. Items opt in through
 * {@link FRAbilities}; nothing here references client classes.
 */
public final class RelicAbilities {

    private RelicAbilities() {
    }

    /** Discord Ring key: requires the ring and an "overthrower" tome in hand. */
    public static void triggerDiscordRing(ServerPlayer player) {
        if (CuriosApi.getCuriosHelper().findFirstCurio(player, FRItems.DISCORD_RING.get()).isEmpty()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty() && held.getItem() instanceof FRAbilities.DiscordRingAbility ability) {
            ability.onDiscordRingKey(player, held);
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() instanceof FRAbilities.DiscordRingAbility ability) {
            ability.onDiscordRingKey(player, offhand);
        }
    }

    /** Telekinesis Tome "use" key. */
    public static void triggerTelekinesisUse(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty() && held.getItem() instanceof FRAbilities.TelekinesisAbility ability) {
            ability.onTelekinesisUse(player, held);
        }
    }

    /** Telekinesis Tome left-click. */
    public static void triggerTelekinesisAttack(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty() && held.getItem() instanceof FRAbilities.TelekinesisAbility ability) {
            ability.onTelekinesisAttack(player, held);
        }
    }
}
