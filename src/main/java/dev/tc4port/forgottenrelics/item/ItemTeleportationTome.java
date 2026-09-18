package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.FRAbilities;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.common.CastingCooldowns;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import dev.tc4port.thaumcraft.block.entity.EldritchPortalBlockEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Tome of Discord. Three modes on right click: blink forward sixteen blocks,
 * swap places with a pointed creature, or jump onto the pointed block. The
 * Discord Ring key uses the same entry point.
 */
public class ItemTeleportationTome extends RelicItem implements ForgottenGear.Warping, FRAbilities.DiscordRingAbility {

    private static final int COOLDOWN_TICKS = 20;
    private static final double RANGE = 128.0D;

    public ItemTeleportationTome(Properties properties) {
        super(properties.stacksTo(1), "ItemTeleportationTome");
    }

    private static VisCost castCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.AER, (int) (160 * FRConfig.discordTomeVisMult()),
                VisChannel.ORDO, (int) (240 * FRConfig.discordTomeVisMult()),
                VisChannel.PERDITIO, (int) (240 * FRConfig.discordTomeVisMult())));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }
        if (level.dimension().equals(EldritchPortalBlockEntity.OUTER_LANDS) || CastingCooldowns.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }

        boolean acted;
        if (player.isShiftKeyDown()) {
            acted = blinkForward(serverPlayer, stack);
        } else {
            Entity pointed = pointedEntity(serverPlayer);
            if (pointed != null) {
                acted = swapWith(serverPlayer, pointed, stack);
            } else {
                acted = jumpToBlock(serverPlayer, stack);
            }
        }
        return acted ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);
    }

    @Override
    public void onDiscordRingKey(ServerPlayer player, ItemStack stack) {
        use(player.level(), player, InteractionHand.MAIN_HAND);
    }

    private boolean blinkForward(ServerPlayer player, ItemStack stack) {
        if (!ThaumcraftApiHelper.consumeVisFromInventory(player, castCost(), VisAction.EXECUTE).consumed()) {
            return false;
        }
        Vec3 from = player.position();
        Vec3 to = from.add(player.getLookAngle().scale(16.0D));
        player.teleportTo(to.x, to.y, to.z);
        afterTeleport(player, from, player.position());
        CastingCooldowns.set(player, COOLDOWN_TICKS);
        return true;
    }

    private boolean swapWith(ServerPlayer player, Entity target, ItemStack stack) {
        if (!ThaumcraftApiHelper.consumeVisFromInventory(player, castCost(), VisAction.EXECUTE).consumed()) {
            return false;
        }
        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        target.teleportTo(playerPos.x, playerPos.y, playerPos.z);
        afterTeleport(player, playerPos, targetPos);
        CastingCooldowns.set(player, COOLDOWN_TICKS);
        return true;
    }

    private boolean jumpToBlock(ServerPlayer player, ItemStack stack) {
        net.minecraft.world.phys.HitResult picked = player.pick(RANGE, 0.0F, false);
        if (!(picked instanceof BlockHitResult hit) || hit.getType() == HitResult.Type.MISS) {
            return false;
        }
        BlockPos base = hit.getBlockPos();
        BlockPos destination = null;
        for (int offset = 0; offset <= 32; offset++) {
            BlockPos candidate = base.above(offset);
            if (!player.level().getBlockState(candidate.below()).getCollisionShape(player.level(), candidate.below()).isEmpty()
                    && player.level().getBlockState(candidate).getCollisionShape(player.level(), candidate).isEmpty()
                    && player.level().getBlockState(candidate.above()).getCollisionShape(player.level(), candidate.above()).isEmpty()) {
                destination = candidate;
                break;
            }
        }
        if (destination == null) {
            return false;
        }
        if (!ThaumcraftApiHelper.consumeVisFromInventory(player, castCost(), VisAction.EXECUTE).consumed()) {
            return false;
        }
        Vec3 from = player.position();
        player.teleportTo(destination.getX() + 0.5D, destination.getY(), destination.getZ() + 0.5D);
        afterTeleport(player, from, player.position());
        CastingCooldowns.set(player, COOLDOWN_TICKS);
        return true;
    }

    @Nullable
    private static Entity pointedEntity(ServerPlayer player) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getLookAngle().scale(RANGE));
        AABB box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(RANGE)).inflate(4.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, from, to, box,
                entity -> entity instanceof LivingEntity && entity != player && entity.isAlive());
        return hit == null ? null : hit.getEntity();
    }

    private static void afterTeleport(ServerPlayer player, Vec3 from, Vec3 to) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverLevel.playSound(null, to.x, to.y, to.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        FRNetwork.sendNear(serverLevel, from.x, from.y, from.z, 128.0D,
                EffectPayload.towards(EffectPayload.EffectType.PORTAL_TRACE, from.x, from.y, from.z, to.x, to.y, to.z, 0, 0.5F, 48));
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerCast.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRAerCost.lore"))
                    .append(Component.literal(String.valueOf((int) (160 * FRConfig.discordTomeVisMult()) / 100.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FROrdoCost.lore"))
                    .append(Component.literal(String.valueOf((int) (240 * FRConfig.discordTomeVisMult()) / 100.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRPerditioCost.lore"))
                    .append(Component.literal(String.valueOf((int) (240 * FRConfig.discordTomeVisMult()) / 100.0D))));
            tooltip.add(emptyLine());
            return;
        }
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemTeleportationTome1.lore"),
                tr("item.ItemTeleportationTome2.lore"),
                tr("item.ItemTeleportationTome3.lore"),
                emptyLine(),
                tr("item.ItemTeleportationTome4.lore"),
                tr("item.ItemTeleportationTome5.lore"),
                tr("item.ItemTeleportationTome6.lore")));
        tooltip.add(emptyLine());
    }
}
