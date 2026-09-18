package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.common.CastingCooldowns;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.EntityMotionPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Void Grimoire. Seizes a pointed creature, suspends it in the air and, after
 * a hundred ticks of channeling, casts it into the void - players are merely
 * flung beyond the world border, and loudly complain about it in chat.
 */
public class ItemVoidGrimoire extends RelicItem implements ForgottenGear.Warping {

    private static final int USE_DURATION = 100;
    private static final double RANGE = 64.0D;
    private static final Map<UUID, LivingEntity> TARGETS = new HashMap<>();

    public ItemVoidGrimoire(Properties properties) {
        super(properties.stacksTo(1), "ItemVoidGrimoire");
    }

    private static VisCost channelCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.ORDO, (int) (9 * FRConfig.voidGrimoireVisMult()),
                VisChannel.PERDITIO, (int) (16 * FRConfig.voidGrimoireVisMult())));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!FRConfig.voidGrimoireEnabled()) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }
        if (CastingCooldowns.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }

        LivingEntity target = pointedEntity(player);
        TARGETS.put(player.getUUID(), target);
        if (target == null) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (!(living instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity target = TARGETS.get(player.getUUID());
        if (target == null || !target.isAlive()) {
            player.stopUsingItem();
            return;
        }
        if (!ThaumcraftApiHelper.consumeVisFromInventory(player, channelCost(), VisAction.EXECUTE).consumed()) {
            player.stopUsingItem();
            return;
        }

        target.fallDistance = 0.0F;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 100, true, false));
        target.setDeltaMovement(target.getDeltaMovement().x, 0.03D, target.getDeltaMovement().z);
        target.hurtMarked = true;

        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D).add(0.0D, 0.03D, 0.0D);
        if (level instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, center.x, center.y, center.z, 64.0D,
                    new EntityMotionPayload(target.getId(), target.getDeltaMovement().x, target.getDeltaMovement().y, target.getDeltaMovement().z, true));
            FRNetwork.sendNear(serverLevel, center.x, center.y, center.z, 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.VOID, center.x, center.y, center.z, 0, 1.0F, 12));
        }

        if (remainingUseDuration == USE_DURATION) {
            level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 4.0F, 0.75F);
        }

        if (remainingUseDuration == 1) {
            if (level instanceof ServerLevel serverLevel) {
                FRNetwork.sendNear(serverLevel, center.x, center.y, center.z, 128.0D,
                        EffectPayload.at(EffectPayload.EffectType.VOID, center.x, center.y, center.z, 0, 2.0F, 48));
            }
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 4.0F, 0.8F + level.random.nextFloat() * 0.2F);
            overthrow(target, player);
            CastingCooldowns.set(player, 30);
        }
    }

    private void overthrow(LivingEntity entity, ServerPlayer overthrower) {
        double x = (Math.random() - 0.5D) * 20002.0D;
        double z = (Math.random() - 0.5D) * 20002.0D;
        double y = -100000.0D + (Math.random() - 0.5D) * 20002.0D;
        entity.teleportTo(x, y, z);
        if (entity instanceof ServerPlayer victim) {
            Component message = Component.literal(overthrower.getGameProfile().getName() + " ")
                    .append(Component.translatable("message.overthrown1"))
                    .append(Component.literal(" " + victim.getGameProfile().getName() + " "))
                    .append(Component.translatable("message.overthrown3"));
            overthrower.server.getPlayerList().broadcastSystemMessage(message, false);
        } else {
            entity.discard();
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity instanceof Player player) {
            TARGETS.remove(player.getUUID());
        }
    }

    @Nullable
    private static LivingEntity pointedEntity(Player player) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getLookAngle().scale(RANGE));
        AABB box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(RANGE)).inflate(3.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, from, to, box,
                entity -> entity instanceof LivingEntity && entity != player && entity.isAlive());
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
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
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerTick.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FROrdoCost.lore"))
                    .append(Component.literal(String.valueOf((int) (9 * FRConfig.voidGrimoireVisMult()) / 100.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRPerditioCost.lore"))
                    .append(Component.literal(String.valueOf((int) (16 * FRConfig.voidGrimoireVisMult()) / 100.0D))));
            tooltip.add(emptyLine());
            return;
        }
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemVoidGrimoire1.lore"),
                tr("item.ItemVoidGrimoire2.lore"),
                tr("item.ItemVoidGrimoire3.lore"),
                emptyLine(),
                tr("item.ItemVoidGrimoire4.lore"),
                tr("item.ItemVoidGrimoire5.lore"),
                emptyLine(),
                tr("item.ItemVoidGrimoire6.lore")));
        tooltip.add(emptyLine());
    }
}
