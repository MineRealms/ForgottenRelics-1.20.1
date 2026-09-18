package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.common.FRCasting;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.common.CastingCooldowns;
import dev.tc4port.forgottenrelics.entity.EntityThunderpealOrb;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.registry.FREntities;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Thunderpeal. Hurls a chain-lightning orb; one cast, then a 30-tick cooldown.
 */
public class ItemThunderpeal extends RelicItem {

    private static final int COOLDOWN_TICKS = 30;

    public ItemThunderpeal(Properties properties) {
        super(properties.stacksTo(1), "ItemThunderpeal");
    }

    private static VisCost castCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.AER, (int) (135 * FRConfig.thunderpealVisMult()),
                VisChannel.IGNIS, (int) (85 * FRConfig.thunderpealVisMult())));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        if (CastingCooldowns.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!FRCasting.pay(player, castCost())) {
            return InteractionResultHolder.fail(stack);
        }

        Vec3 origin = player.getEyePosition().add(player.getLookAngle().scale(1.25D)).add(0.0D, 0.5D, 0.0D);
        Vec3 motion = player.getLookAngle().scale(1.5D);

        EntityThunderpealOrb orb = new EntityThunderpealOrb(FREntities.THUNDERPEAL_ORB.get(), level, player);
        orb.setPos(origin.x, origin.y, origin.z);
        orb.setDeltaMovement(motion);
        level.playSound(null, orb.getX(), orb.getY(), orb.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
        level.addFreshEntity(orb);

        CastingCooldowns.set(player, COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        java.util.List<Component> lines = new java.util.ArrayList<>();
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerCast.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRAerCost.lore"))
                    .append(Component.literal(String.valueOf((int) (135 * FRConfig.thunderpealVisMult()) / 100.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRIgnisCost.lore"))
                    .append(Component.literal(String.valueOf((int) (85 * FRConfig.thunderpealVisMult()) / 100.0D))));
            return;
        }
        lines.add(tr("item.ItemThunderpeal1.lore"));
        lines.add(emptyLine());
        lines.add(tr("item.ItemThunderpeal2.lore"));
        lines.add(Component.translatable("item.ItemThunderpeal3_1.lore")
                .append(Component.literal(" " + (int) FRConfig.damageThunderpealDirect() + " "))
                .append(Component.translatable("item.ItemThunderpeal3_2.lore")));
        lines.add(tr("item.ItemThunderpeal4.lore"));
        lines.add(emptyLine());
        lines.add(Component.translatable("item.ItemThunderpeal5_1.lore")
                .append(Component.literal(" " + (int) FRConfig.damageThunderpealBolt() + " "))
                .append(Component.translatable("item.ItemThunderpeal5_2.lore")));
        lines.add(tr("item.ItemThunderpeal6.lore"));
        lines.add(tr("item.ItemThunderpeal7.lore"));
        addShiftTooltip(tooltip, lines);
    }
}
