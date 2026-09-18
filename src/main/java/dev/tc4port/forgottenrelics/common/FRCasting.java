package dev.tc4port.forgottenrelics.common;

import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Vis payment with player feedback.
 *
 * <p>The 1.7.10 relics failed silently when no vis source was available; on a
 * fresh 1.20.1 world that reads as "the item does nothing". This helper keeps
 * the source's payment contract but shows a throttled action-bar hint when a
 * cast cannot be paid for.</p>
 */
public final class FRCasting {

    private static final long NOTIFY_INTERVAL_MS = 1500L;
    private static final Map<UUID, Long> LAST_NOTIFIED = new ConcurrentHashMap<>();

    private FRCasting() {
    }

    /** Returns true when the cost was consumed, notifying the caster otherwise. */
    public static boolean pay(Player player, VisCost cost) {
        boolean consumed = ThaumcraftApiHelper.consumeVisFromInventory(player, cost, VisAction.EXECUTE).consumed();
        if (!consumed && player instanceof ServerPlayer serverPlayer) {
            long now = System.currentTimeMillis();
            Long last = LAST_NOTIFIED.get(serverPlayer.getUUID());
            if (last == null || now - last >= NOTIFY_INTERVAL_MS) {
                LAST_NOTIFIED.put(serverPlayer.getUUID(), now);
                serverPlayer.displayClientMessage(
                        Component.translatable("forgottenrelics.message.no_vis").withStyle(ChatFormatting.RED), true);
            }
        }
        return consumed;
    }
}
