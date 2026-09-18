package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.common.FRCasting;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.common.CastingCooldowns;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.util.List;
import java.util.Map;
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
 * Runic Stone. A one-shot rain dispeller; hold the charge for a second
 * (60 ticks, like the source mod's bow action) and pay 25 vis per primal.
 */
public class ItemWeatherStone extends RelicItem {

    private static final int USE_DURATION = 60;
    private static final int COOLDOWN_TICKS = 100;

    public ItemWeatherStone(Properties properties) {
        super(properties.stacksTo(1), "ItemWeatherStone");
    }

    public static int visCost() {
        return (int) (2500 * FRConfig.weatherStoneVisMult());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isRaining() && !CastingCooldowns.isOnCooldown(player)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(livingEntity instanceof Player player) || remainingUseDuration != 1) {
            return;
        }
        if (!level.isRaining() || CastingCooldowns.isOnCooldown(player)) {
            return;
        }

        int cost = visCost();
        VisCost vis = VisCost.ofCentivis(Map.of(
                VisChannel.AER, cost,
                VisChannel.TERRA, cost,
                VisChannel.AQUA, cost));
        if (!FRCasting.pay(player, vis)) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1.0D, player.getZ(), 48, 0.8D, 1.0D, 0.8D, 0.15D);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.8F + (float) (Math.random() * 0.2F));
            int clearTime = 24000 + (int) (Math.random() * 976000);
            serverLevel.setWeatherParameters(clearTime, 0, false, false);
        }

        CastingCooldowns.set(player, COOLDOWN_TICKS);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemWeatherStone1.lore"),
                emptyLine(),
                Component.translatable("item.ItemWeatherStone2_1.lore")
                        .append(Component.literal(" " + (visCost() / 100) + " "))
                        .append(Component.translatable("item.ItemWeatherStone2_2.lore")),
                tr("item.ItemWeatherStone3.lore")));
    }
}
