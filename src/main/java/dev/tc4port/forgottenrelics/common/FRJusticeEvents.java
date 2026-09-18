package dev.tc4port.forgottenrelics.common;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.research.ForgottenKnowledge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Periodic justice-handler sweep, replacing the source side's worker thread. */
public class FRJusticeEvents {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % FRConfig.researchInspectionFrequency() != 0) {
            return;
        }
        ForgottenKnowledge.grantAvailable(player);
    }
}
