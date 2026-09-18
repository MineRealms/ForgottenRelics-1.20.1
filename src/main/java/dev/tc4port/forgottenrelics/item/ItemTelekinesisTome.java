package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.common.FRCasting;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.FRAbilities;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.common.CastingCooldowns;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.network.LightningPayload;
import dev.tc4port.forgottenrelics.network.PlayerMotionPayload;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tome of Predestiny. Telekinetically seizes a creature ahead of the caster and
 * steers it through the air; left click zaps it with lightning, sneak-left
 * click hurls it away. The per-player grab state replaces the source mod's
 * HashMap bookkeeping.
 */
public class ItemTelekinesisTome extends RelicItem implements ForgottenGear.Warping, FRAbilities.TelekinesisAbility {

    private static final float RANGE = 3.0F;
    private static final double HOLD_DISTANCE = 7.5D;
    private static final int HOLD_EXPIRE_TICKS = 5;
    private static final int LAUNCH_COOLDOWN_TICKS = 40;
    private static final Map<UUID, GrabState> GRABS = new HashMap<>();

    public ItemTelekinesisTome(Properties properties) {
        super(properties.stacksTo(1), "ItemTelekinesisTome");
    }

    private static VisCost holdCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.AER, (int) (6 * FRConfig.telekinesisTomeVisMult()),
                VisChannel.ORDO, (int) (8 * FRConfig.telekinesisTomeVisMult())));
    }

    private static VisCost zapCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.AER, (int) (80 * FRConfig.telekinesisTomeVisMult()),
                VisChannel.ORDO, (int) (50 * FRConfig.telekinesisTomeVisMult()),
                VisChannel.IGNIS, (int) (200 * FRConfig.telekinesisTomeVisMult())));
    }

    private static VisCost launchCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.AER, (int) (150 * FRConfig.telekinesisTomeVisMult()),
                VisChannel.ORDO, (int) (80 * FRConfig.telekinesisTomeVisMult())));
    }

    // ------------------------------------------------------------------
    // ability entry points (called by the networking layer)
    // ------------------------------------------------------------------

    @Override
    public void onTelekinesisUse(ServerPlayer player, ItemStack stack) {
        GrabState state = state(player);
        if (state.cooldown > 0) {
            return;
        }

        LivingEntity target = existingTarget(player, state.targetId) ;
        if (target == null) {
            target = searchForTarget(player);
        }
        if (target == null || isBlacklisted(target)) {
            return;
        }
        if (!FRCasting.pay(player, holdCost())) {
            return;
        }

        target.fallDistance = 0.0F;
        if (!target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 3, true, false));
        }

        Vec3 hold = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        double heldDistance = player.isShiftKeyDown() ? Math.max(1.0D, state.reDist) : HOLD_DISTANCE;
        hold = hold.add(player.getLookAngle().scale(heldDistance)).add(0.0D, 0.5D, 0.0D);

        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 direction = hold.subtract(center);
        double multiplier = direction.length();
        float power = 0.66666F;
        if (multiplier < 1.5D) {
            power = 0.333333F;
        } else if (multiplier >= 8.0D) {
            power *= (float) (multiplier / 8.0F);
        }
        Vec3 motion = multiplier > 1.0D ? direction.normalize().scale(power) : direction.scale(power);

        applyMotion(target, motion);
        if (player.level() instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, center.x, center.y, center.z, 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.TELEKINESIS, center.x, center.y, center.z, 0, 1.0F, 8));
        }

        state.targetId = target.getId();
        state.dist = HOLD_DISTANCE;
        if (state.reDist < 0.0D) {
            state.reDist = player.distanceTo(target);
        }
        state.expireTicks = HOLD_EXPIRE_TICKS;
    }

    @Override
    public void onTelekinesisAttack(ServerPlayer player, ItemStack stack) {
        GrabState state = state(player);
        if (state.targetId < 0 || CastingCooldowns.isOnCooldown(player)) {
            return;
        }
        LivingEntity target = existingTarget(player, state.targetId);
        if (target == null) {
            return;
        }

        if (player.distanceTo(target) <= 16.0F
                && FRCasting.pay(player, zapCost())) {
            lightningAttack(player, target);
            CastingCooldowns.set(player, 10);
        }

        if (player.isShiftKeyDown()
                && FRCasting.pay(player, launchCost())) {
            Vec3 look = player.getLookAngle();
            applyMotion(target, new Vec3(look.x * 3.0D, look.y * 1.5D, look.z * 3.0D));
            state.reset();
            state.cooldown = LAUNCH_COOLDOWN_TICKS;
        }
    }

    private static void lightningAttack(ServerPlayer player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 from = player.position().add(0.0D, 1.0D, 0.0D);
        Vec3 to = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        for (int i = 0; i < 4; i++) {
            FRNetwork.sendNear(serverLevel, from.x, from.y, from.z, 64.0D,
                    new LightningPayload(from.x, from.y, from.z, to.x, to.y, to.z, 0x7FC4FF, 0.075F));
        }
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 0.8F);
        target.hurt(FRDamageTypes.source(serverLevel, FRDamageTypes.FORGOTTEN_LIGHTNING, player, player),
                (float) (16.0D + 24.0D * Math.random()));
    }

    private static void applyMotion(LivingEntity target, Vec3 motion) {
        target.setDeltaMovement(motion);
        target.hurtMarked = true;
        if (target instanceof ServerPlayer victim) {
            FRNetwork.sendToPlayer(victim, new PlayerMotionPayload(motion.x, motion.y, motion.z));
        }
    }

    private static boolean isBlacklisted(LivingEntity target) {
        return target instanceof Player && !FRConfig.telekinesisOnPlayers();
    }

    @Nullable
    private static LivingEntity existingTarget(ServerPlayer player, int targetId) {
        if (targetId < 0) {
            return null;
        }
        Entity entity = player.level().getEntity(targetId);
        if (!(entity instanceof LivingEntity living) || living == player || !living.isAlive()) {
            return null;
        }
        return withinLookPath(player, living, RANGE + 3.0F) ? living : null;
    }

    @Nullable
    private static LivingEntity searchForTarget(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 base = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        for (int distance = 1; distance < 32; distance++) {
            Vec3 point = base.add(look.scale(distance)).add(0.0D, 0.5D, 0.0D);
            AABB box = new AABB(point.x - RANGE, point.y - RANGE, point.z - RANGE,
                    point.x + RANGE, point.y + RANGE, point.z + RANGE);
            List<LivingEntity> found = player.level().getEntitiesOfClass(LivingEntity.class, box,
                    entity -> entity != player && entity.isAlive());
            if (!found.isEmpty()) {
                return found.get(0);
            }
        }
        return null;
    }

    private static boolean withinLookPath(ServerPlayer player, LivingEntity target, float range) {
        Vec3 look = player.getLookAngle();
        Vec3 point = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        for (int distance = 1; distance < 32; distance++) {
            point = point.add(look);
            AABB box = new AABB(point.x - range, point.y + 0.5D - range, point.z - range,
                    point.x + range, point.y + 0.5D + range, point.z + range);
            if (box.intersects(target.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private static GrabState state(ServerPlayer player) {
        return GRABS.computeIfAbsent(player.getUUID(), key -> new GrabState());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        GrabState state = state(player);
        if (state.expireTicks <= 0 && state.targetId != -1) {
            state.reset();
        }
        if (state.expireTicks > 0) {
            state.expireTicks--;
        }
        if (state.cooldown > 0) {
            state.cooldown--;
        }
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 4;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerTick.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRAerCost.lore"))
                    .append(Component.literal(String.valueOf(round((int) (6 * FRConfig.telekinesisTomeVisMult()) / 100.0D, 2)))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FROrdoCost.lore"))
                    .append(Component.literal(String.valueOf(round((int) (8 * FRConfig.telekinesisTomeVisMult()) / 100.0D, 2)))));
            tooltip.add(emptyLine());
            tooltip.add(tr("item.FRVisPerCast.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRAerCost.lore"))
                    .append(Component.literal(String.valueOf(round(80 / 100.0D * FRConfig.telekinesisTomeVisMult(), 2)))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FROrdoCost.lore"))
                    .append(Component.literal(String.valueOf(round(50 / 100.0D * FRConfig.telekinesisTomeVisMult(), 2)))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRIgnisCost.lore"))
                    .append(Component.literal(String.valueOf(round(200 / 100.0D * FRConfig.telekinesisTomeVisMult(), 2)))));
            tooltip.add(emptyLine());
            return;
        }
        addShiftTooltip(tooltip, new ArrayList<>(List.of(
                tr("item.ItemTelekinesisTome1.lore"),
                tr("item.ItemTelekinesisTome2.lore"),
                tr("item.ItemTelekinesisTome3.lore"),
                emptyLine(),
                tr("item.ItemTelekinesisTome4.lore"),
                tr("item.ItemTelekinesisTome5.lore"),
                tr("item.ItemTelekinesisTome6.lore"),
                emptyLine(),
                tr("item.ItemTelekinesisTome7.lore"),
                tr("item.ItemTelekinesisTome8.lore"),
                tr("item.ItemTelekinesisTome9.lore"),
                emptyLine(),
                tr("item.ItemTelekinesisTome10.lore"),
                emptyLine(),
                Component.translatable("item.ItemTelekinesisTome11_1.lore")
                        .append(Component.literal(" " + (int) FRConfig.telekinesisTomeDamageMIN()
                                + "-" + (int) FRConfig.telekinesisTomeDamageMAX() + " "))
                        .append(Component.translatable("item.ItemTelekinesisTome11_2.lore")))));
        tooltip.add(emptyLine());
    }

    private static double round(double value, int places) {
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    /** Per-player grab bookkeeping, replacing the source mod's HashMap fields. */
    private static final class GrabState {
        int expireTicks;
        int cooldown;
        int targetId = -1;
        double dist = -1.0D;
        double reDist = -1.0D;

        void reset() {
            this.targetId = -1;
            this.dist = -1.0D;
            this.reDist = -1.0D;
        }
    }

}
