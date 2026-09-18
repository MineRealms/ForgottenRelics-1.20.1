package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.common.FRCasting;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.entity.EntityRageousMissile;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.registry.FREntities;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Nuclear Fury. Held down, it fires a homing missile every other tick while
 * Ignis, Ordo and Perditio vis lasts.
 */
public class ItemMissileTome extends RelicItem implements ForgottenGear.Warping {

    private static final int MAX_USE = 72000;

    public ItemMissileTome(Properties properties) {
        super(properties.stacksTo(1), "ItemMissileTome");
    }

    private static VisCost shotCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.IGNIS, (int) (20 * FRConfig.nuclearFuryVisMult()),
                VisChannel.ORDO, (int) (10 * FRConfig.nuclearFuryVisMult()),
                VisChannel.PERDITIO, (int) (15 * FRConfig.nuclearFuryVisMult())));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(living instanceof Player player)) {
            return;
        }
        if (remainingUseDuration == MAX_USE || remainingUseDuration % 2 != 0) {
            return;
        }
        if (!FRCasting.pay(player, shotCost())) {
            return;
        }

        double x = player.getX() + (Math.random() - 0.5D) * 3.1D;
        double y = player.getY() + 3.8D + (Math.random() - 0.5D) * 3.1D;
        double z = player.getZ() + (Math.random() - 0.5D) * 3.1D;

        EntityRageousMissile missile = new EntityRageousMissile(FREntities.RAGEOUS_MISSILE.get(), level, player, false);
        missile.setPos(x, y, z);
        level.playSound(null, x, y, z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.6F, 0.8F + (float) Math.random() * 0.2F);
        level.addFreshEntity(missile);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 12, 0.2D, 0.2D, 0.2D, 0.02D);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_USE;
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 5;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemMissileTome1.lore"),
                tr("item.ItemMissileTome2.lore"),
                tr("item.ItemMissileTome3.lore"),
                emptyLine(),
                tr("item.ItemMissileTome4.lore"),
                tr("item.ItemMissileTome5.lore"),
                emptyLine(),
                tr("item.ItemMissileTome6.lore"),
                emptyLine(),
                Component.translatable("item.ItemMissileTome7_1.lore")
                        .append(Component.literal(" " + (int) FRConfig.nuclearFuryDamageMIN() + "-" + (int) FRConfig.nuclearFuryDamageMAX() + " "))
                        .append(Component.translatable("item.ItemMissileTome7_2.lore"))));
        tooltip.add(emptyLine());
    }
}
