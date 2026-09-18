package dev.tc4port.forgottenrelics.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Ability contracts implemented by relic items. Keeping them as interfaces
 * lets the networking layer dispatch without referencing concrete items (and
 * therefore without class-loading anything on the wrong side).
 */
public final class FRAbilities {

    private FRAbilities() {
    }

    /** Implemented by the Tome of Discord (activated by the Discord Ring key). */
    public interface DiscordRingAbility {
        void onDiscordRingKey(ServerPlayer player, ItemStack stack);
    }

    /** Implemented by the Tome of Predestiny (telekinesis controls). */
    public interface TelekinesisAbility {
        void onTelekinesisUse(ServerPlayer player, ItemStack stack);

        void onTelekinesisAttack(ServerPlayer player, ItemStack stack);
    }
}
