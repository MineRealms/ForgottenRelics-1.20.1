package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Ghastly Skull. Reduces the holder to a single heart; the source mod's debug
 * print block is deliberately not ported. Effects fall back to vanilla
 * particles because 1.7.10's client FX classes have no direct counterpart.
 */
public class ItemGhastlySkull extends RelicItem implements ForgottenGear.Warping {

    public ItemGhastlySkull(Properties properties) {
        super(properties.stacksTo(1), "ItemGhastlySkull");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            player.setHealth(1.0F);
            Vec3 look = player.getLookAngle().scale(1.6D);
            Vec3 origin = player.getEyePosition();
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH,
                        origin.x + look.x, origin.y + look.y, origin.z + look.z,
                        24, 0.35D, 0.35D, 0.35D, 0.02D);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        origin.x + look.x, origin.y + look.y, origin.z + look.z,
                        12, 0.25D, 0.25D, 0.25D, 0.01D);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemGhastlySkull1.lore"),
                tr("item.ItemGhastlySkull2.lore")));
        tooltip.add(emptyLine());
    }
}
