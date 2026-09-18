package dev.tc4port.forgottenrelics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Client lookups used by clientbound payload handlers. */
@OnlyIn(Dist.CLIENT)
public final class FRClientHooks {

    private FRClientHooks() {
    }

    public static Player localPlayer() {
        return Minecraft.getInstance().player;
    }

    public static Entity localPlayerEntity(int entityId) {
        Player player = localPlayer();
        if (player == null || player.level() == null) {
            return null;
        }
        return player.level().getEntity(entityId);
    }
}
