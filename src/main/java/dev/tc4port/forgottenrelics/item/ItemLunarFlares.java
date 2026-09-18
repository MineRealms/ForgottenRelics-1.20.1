package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.entity.EntityLunarFlare;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.registry.FREntities;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Tome of Lunar Flares. Hold to call down flares onto the pointed block, one
 * every other tick, from twenty-four blocks above.
 */
public class ItemLunarFlares extends RelicItem implements ForgottenGear.Warping {

    private static final int MAX_USE = 72000;

    public ItemLunarFlares(Properties properties) {
        super(properties.stacksTo(1), "ItemLunarFlares");
    }

    private static VisCost shotCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.AER, (int) (35 * FRConfig.lunarFlaresVisMult()),
                VisChannel.IGNIS, (int) (50 * FRConfig.lunarFlaresVisMult()),
                VisChannel.ORDO, (int) (65 * FRConfig.lunarFlaresVisMult())));
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

        net.minecraft.world.phys.HitResult hit = player.pick(128.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }
        if (!ThaumcraftApiHelper.consumeVisFromInventory(player, shotCost(), VisAction.EXECUTE).consumed()) {
            return;
        }

        BlockPos target = blockHit.getBlockPos();
        double x = target.getX() + (Math.random() - 0.5D) * 12.0D;
        double y = target.getY() + 24.0D + (Math.random() - 0.5D) * 12.0D;
        double z = target.getZ() + 0.5D + (Math.random() - 0.5D) * 12.0D;

        EntityLunarFlare flare = new EntityLunarFlare(FREntities.LUNAR_FLARE.get(), level, player,
                target.getX(), target.getY(), target.getZ());
        flare.setPos(x, y, z);
        Vec3 motion = new Vec3(target.getX() + 0.5D - x, target.getY() - y, target.getZ() + 0.5D - z).normalize().scale(4.0D);
        flare.setDeltaMovement(motion);
        level.addFreshEntity(flare);

        if (remainingUseDuration % 4 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 2.0F, 1.0F + (float) Math.random() * 0.5F);
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
        return 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerSecond.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRAerCost.lore"))
                    .append(Component.literal(String.valueOf((int) (35 * FRConfig.lunarFlaresVisMult()) / 100.0D * 10.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRIgnisCost.lore"))
                    .append(Component.literal(String.valueOf((int) (50 * FRConfig.lunarFlaresVisMult()) / 100.0D * 10.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FROrdoCost.lore"))
                    .append(Component.literal(String.valueOf((int) (65 * FRConfig.lunarFlaresVisMult()) / 100.0D * 10.0D))));
            tooltip.add(emptyLine());
            return;
        }

        List<Component> lines = new ArrayList<>();
        lines.add(tr("item.ItemLunarFlares1.lore"));
        lines.add(emptyLine());
        lines.add(tr("item.ItemLunarFlares2.lore"));
        lines.add(Component.translatable("item.ItemLunarFlares3_1.lore")
                .append(Component.literal(" " + (int) FRConfig.damageLunarFlareImpact() + " "))
                .append(Component.translatable("item.ItemLunarFlares3_2.lore")));
        lines.add(tr("item.ItemLunarFlares35.lore"));
        lines.add(emptyLine());
        lines.add(Component.translatable("item.ItemLunarFlares4_1.lore")
                .append(Component.literal(" " + (int) FRConfig.damageLunarFlareDirect() + " "))
                .append(Component.translatable("item.ItemLunarFlares4_2.lore")));
        addShiftTooltip(tooltip, lines);
        tooltip.add(emptyLine());
    }
}
