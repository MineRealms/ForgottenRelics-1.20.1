package dev.tc4port.forgottenrelics.client;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import dev.tc4port.forgottenrelics.network.DiscordKeybindPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client tick handling for relic keybindings. */
@Mod.EventBusSubscriber(modid = ForgottenRelics.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FRClientEvents {

    private FRClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (FRClientKeys.DISCORD_RING.consumeClick()) {
            FRNetwork.sendToServer(new DiscordKeybindPayload(true));
        }
    }
}
