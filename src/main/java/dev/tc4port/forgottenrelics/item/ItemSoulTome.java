package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.common.FRCasting;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.entity.EntitySoulEnergy;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.network.LightningPayload;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import dev.tc4port.forgottenrelics.registry.FREntities;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.Vec3;

/**
 * Edict of a Thousand Damned Souls. Drains nearby creatures to heal the
 * caster: every four ticks a victim is struck and a soul mote flies home.
 * Creatures pressed right up against the caster are lashed with lightning
 * instead.
 */
public class ItemSoulTome extends RelicItem implements ForgottenGear.Warping, ForgottenGear.Repairable {

    private static final int MAX_USE = 72000;
    private static final int SEARCH_RANGE = 20;

    public ItemSoulTome(Properties properties) {
        super(properties.stacksTo(1), "ItemSoulTome");
    }

    private static VisCost pulseCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.AER, (int) (20 * FRConfig.soulTomeVisMult()),
                VisChannel.TERRA, (int) (25 * FRConfig.soulTomeVisMult()),
                VisChannel.IGNIS, (int) (35 * FRConfig.soulTomeVisMult()),
                VisChannel.PERDITIO, (int) (50 * FRConfig.soulTomeVisMult())));
    }

    private static VisCost zapCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.IGNIS, (int) (150 * FRConfig.soulTomeVisMult()),
                VisChannel.PERDITIO, (int) (120 * FRConfig.soulTomeVisMult())));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (!(living instanceof ServerPlayer player)) {
            return;
        }

        List<LivingEntity> nearby = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(SEARCH_RANGE), entity -> entity != player && entity.isAlive()));

        if (remainingUseDuration >= MAX_USE - 20) {
            return;
        }

        // Point-blank: lightning lash plus heavy knockback.
        for (LivingEntity victim : nearby) {
            if (victim.distanceTo(player) > 3.0F) {
                continue;
            }
            if (!FRCasting.pay(player, zapCost())) {
                continue;
            }
            Vec3 push = victim.position().subtract(player.position()).normalize().scale(3.0D);
            victim.hurt(FRDamageTypes.source(level, FRDamageTypes.FORGOTTEN_LIGHTNING, player, player),
                    (float) (20.0D + 80.0D * Math.random()));
            victim.setDeltaMovement(push.x, push.y + 1.0D, push.z);
            victim.hurtMarked = true;
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 0.8F);
            if (level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 4; i++) {
                    FRNetwork.sendNear(serverLevel, player.getX(), player.getY(), player.getZ(), 64.0D,
                            new LightningPayload(player.getX(), player.getY() + 1.0D, player.getZ(),
                                    victim.getX(), victim.getBoundingBox().minY + victim.getBbHeight() * 0.5D, victim.getZ(),
                                    0x7FC4FF, 0.075F));
                }
            }
        }

        // Soul drain pulse.
        if (remainingUseDuration % 4 == 0
                && FRCasting.pay(player, pulseCost())) {
            if (!nearby.isEmpty()) {
                LivingEntity victim = nearby.get((int) (Math.random() * nearby.size()));
                float soulDamage = victim.getMaxHealth() / (float) FRConfig.soulTomeDivisor();
                soulDamage = Math.max(1.0F, Math.min(20.0F, soulDamage));
                victim.hurt(FRDamageTypes.source(level, FRDamageTypes.FORGOTTEN_MAGIC, player, player), soulDamage);

                EntitySoulEnergy soul = new EntitySoulEnergy(FREntities.SOUL_ENERGY.get(), level, victim, player);
                Vec3 origin = victim.position().add(0.0D, victim.getBbHeight() * 0.5D, 0.0D).add(player.getLookAngle().scale(1.0D));
                soul.setPos(origin.x, origin.y + 0.5D, origin.z);
                soul.setDeltaMovement(player.getLookAngle().scale(1.25D));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 2.0F, 0.8F + (float) Math.random() * 0.2F);
                level.addFreshEntity(soul);
            }
        }

        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(0.0D, motion.y, 0.0D);
        player.hurtMarked = true;
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
                    .append(Component.literal(String.valueOf((int) (20 * FRConfig.soulTomeVisMult()) / 100.0D * 10.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRTerraCost.lore"))
                    .append(Component.literal(String.valueOf((int) (25 * FRConfig.soulTomeVisMult()) / 100.0D * 10.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRIgnisCost.lore"))
                    .append(Component.literal(String.valueOf((int) (35 * FRConfig.soulTomeVisMult()) / 100.0D * 10.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRPerditioCost.lore"))
                    .append(Component.literal(String.valueOf((int) (50 * FRConfig.soulTomeVisMult()) / 100.0D * 10.0D))));
            tooltip.add(emptyLine());
            return;
        }
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemSoulTome1.lore"),
                tr("item.ItemSoulTome2.lore"),
                tr("item.ItemSoulTome3.lore"),
                emptyLine(),
                tr("item.ItemSoulTome4.lore"),
                tr("item.ItemSoulTome5.lore"),
                emptyLine(),
                tr("item.ItemSoulTome6.lore"),
                tr("item.ItemSoulTome7.lore"),
                tr("item.ItemSoulTome8.lore"),
                emptyLine(),
                tr("item.ItemSoulTome9.lore")));
        tooltip.add(emptyLine());
    }
}
