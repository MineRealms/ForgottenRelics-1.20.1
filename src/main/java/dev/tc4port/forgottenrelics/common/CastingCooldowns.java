package dev.tc4port.forgottenrelics.common;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;

/**
 * Server-side per-player casting cooldowns, replacing the source mod's
 * {@code Main.castingCooldowns} map. Values are in ticks and count down on the
 * player tick event.
 */
public final class CastingCooldowns {

    private static final Map<UUID, Integer> COOLDOWNS = new ConcurrentHashMap<>();

    private CastingCooldowns() {
    }

    public static boolean isOnCooldown(Player player) {
        return COOLDOWNS.getOrDefault(player.getUUID(), 0) > 0;
    }

    public static int remaining(Player player) {
        return COOLDOWNS.getOrDefault(player.getUUID(), 0);
    }

    public static void set(Player player, int ticks) {
        COOLDOWNS.put(player.getUUID(), Math.max(0, ticks));
    }

    public static void tick(Player player) {
        Integer current = COOLDOWNS.get(player.getUUID());
        if (current == null) {
            return;
        }
        if (current <= 1) {
            COOLDOWNS.remove(player.getUUID());
        } else {
            COOLDOWNS.put(player.getUUID(), current - 1);
        }
    }

    public static void clear(Player player) {
        COOLDOWNS.remove(player.getUUID());
    }
}
