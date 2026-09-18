package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.item.base.CurioRelicItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Ring of Superposition. Every 600 ticks a wearer has a 2.5% chance to swap
 * places with another wearer, dimensions included - as in the source mod this
 * is unapologetically random.
 */
public class ItemSuperpositionRing extends CurioRelicItem {

    private static final int CHECK_INTERVAL = 600;
    private static final double SWAP_CHANCE = 0.025D;

    public ItemSuperpositionRing(Properties properties) {
        super(properties.stacksTo(1), "ItemSuperpositionRing");
    }

    @Override
    public void onWornTick(ItemStack stack, Player player) {
        if (!(player instanceof ServerPlayer self) || player.level().isClientSide()) {
            return;
        }
        if (player.tickCount % CHECK_INTERVAL != 0 || Math.random() > SWAP_CHANCE) {
            return;
        }

        List<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer other : self.server.getPlayerList().getPlayers()) {
            if (other == self) {
                continue;
            }
            if (CuriosApi.getCuriosHelper().findFirstCurio(other, stack.getItem()).isPresent()) {
                candidates.add(other);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        ServerPlayer other = candidates.get((int) (Math.random() * candidates.size()));
        Vec3 selfPos = self.position();
        Vec3 otherPos = other.position();
        ServerLevel selfLevel = (ServerLevel) self.level();
        ServerLevel otherLevel = (ServerLevel) other.level();

        self.teleportTo(otherLevel, otherPos.x, otherPos.y, otherPos.z, self.getYRot(), self.getXRot());
        other.teleportTo(selfLevel, selfPos.x, selfPos.y, selfPos.z, other.getYRot(), other.getXRot());

        playSwapEffects(otherLevel, otherPos);
        playSwapEffects(selfLevel, selfPos);
    }

    private static void playSwapEffects(ServerLevel level, Vec3 position) {
        level.playSound(null, position.x, position.y, position.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                1.0F, (float) (0.8F + Math.random() * 0.2F));
        level.sendParticles(ParticleTypes.PORTAL, position.x, position.y + 1.0D, position.z, 32, 0.4D, 0.7D, 0.4D, 0.05D);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemSuperpositionRing1.lore"),
                tr("item.ItemSuperpositionRing2.lore"),
                tr("item.ItemSuperpositionRing3.lore"),
                emptyLine(),
                tr("item.ItemSuperpositionRing4.lore"),
                tr("item.ItemSuperpositionRing5.lore"),
                emptyLine(),
                CurioTooltips.ring()));
    }
}
