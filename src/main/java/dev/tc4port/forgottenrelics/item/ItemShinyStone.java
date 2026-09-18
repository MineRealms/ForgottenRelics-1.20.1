package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.data.ShinyStoneState;
import dev.tc4port.forgottenrelics.item.base.CurioRelicItem;
import dev.tc4port.forgottenrelics.entity.EntityShinyEnergy;
import dev.tc4port.forgottenrelics.registry.FREntities;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Shiny Stone. Standing perfectly still charges the stone; the longer the
 * wearer stays motionless, the faster it heals and the fewer particles it
 * emits (the source mod's own little joke).
 */
public class ItemShinyStone extends CurioRelicItem {

    public ItemShinyStone(Properties properties) {
        super(properties.stacksTo(1), "ItemShinyStone");
    }

    @Override
    public void onWornTick(ItemStack stack, Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        int checkRate = Math.max(1, FRConfig.shinyStoneCheckrate());
        ShinyStoneState state = ItemStatePlatform.getOrDefault(stack, FRItemState.SHINY_STONE, ShinyStoneState.EMPTY);

        if (player.tickCount % checkRate == 0) {
            boolean motionless = player.getX() == state.lastX() && player.getY() == state.lastY() && player.getZ() == state.lastZ();

            if (motionless) {
                int charge = state.charge();
                int healRate = 1;
                int particles = 3;
                if (charge >= 40) {
                    healRate = 2;
                    particles = 2;
                }
                if (charge >= 80) {
                    healRate = 3;
                    particles = 1;
                }
                if (charge >= 200) {
                    healRate = 4;
                    particles = 0;
                }
                state = new ShinyStoneState(charge + 4, healRate, state.lastX(), state.lastY(), state.lastZ());
                if (player.level() instanceof ServerLevel serverLevel) {
                    // Source parity: one mote per unused particle tier, homing back onto the wearer.
                    for (int mote = particles; mote <= 3; mote++) {
                        EntityShinyEnergy energy = new EntityShinyEnergy(FREntities.SHINY_ENERGY.get(), serverLevel, player, player,
                                player.getX(), player.getY(), player.getZ());
                        Vec3 offset = new Vec3((Math.random() - 0.5D) * 3.0D, (Math.random() - 0.5D) * 3.0D, (Math.random() - 0.5D) * 3.0D);
                        Vec3 position = player.position().add(offset);
                        energy.setPos(position.x, position.y + 0.5D, position.z);
                        energy.setDeltaMovement(offset.normalize().scale(-0.1D));
                        serverLevel.addFreshEntity(energy);
                    }
                }
            } else {
                state = state.withCharge(0).withHealRate(0);
            }

            state = state.at(player.getX(), player.getY(), player.getZ());
            ItemStatePlatform.set(stack, FRItemState.SHINY_STONE, state);
        }

        int healRate = state.healRate();
        int healCheckRate = Math.max(1, (int) (checkRate / 4.0D));
        if (healRate == 1 && player.tickCount % (10 * healCheckRate) == 0) {
            player.heal(1.0F);
        } else if (healRate == 2 && player.tickCount % (5 * healCheckRate) == 0) {
            player.heal(1.0F);
        } else if (healRate == 3 && player.tickCount % (2 * healCheckRate) == 0) {
            player.heal(1.0F);
        } else if (healRate == 4 && player.tickCount % (1 * healCheckRate) == 0) {
            player.heal(1.0F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemShinyStone1.lore"),
                tr("item.ItemShinyStone2.lore"),
                emptyLine(),
                CurioTooltips.necklace()));
    }
}
