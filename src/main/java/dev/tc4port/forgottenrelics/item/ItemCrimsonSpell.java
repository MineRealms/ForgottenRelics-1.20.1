package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.common.FRCasting;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.common.CastingCooldowns;
import dev.tc4port.forgottenrelics.entity.EntityCrimsonOrb;
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
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Crimson Spell. Sweeps forward along the caster's line of sight and launches
 * a homing crimson orb at the first living thing it finds - or at a random
 * visible target within 32 blocks when the sweep comes up empty.
 */
public class ItemCrimsonSpell extends RelicItem implements ForgottenGear.Warping {

    private static final float SEARCH_RANGE = 3.0F;
    private static final int COOLDOWN_TICKS = 30;

    public ItemCrimsonSpell(Properties properties) {
        super(properties.stacksTo(1), "ItemCrimsonSpell");
    }

    private static VisCost castCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.IGNIS, (int) (480 * FRConfig.crimsonSpellVisMult()),
                VisChannel.PERDITIO, (int) (360 * FRConfig.crimsonSpellVisMult())));
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

        LivingEntity target = sweepForTarget(player);
        if (target == null) {
            target = randomVisibleTarget(player);
        }

        if (!FRCasting.pay(player, castCost())) {
            return InteractionResultHolder.fail(stack);
        }

        spawnOrb(level, player, target);
        CastingCooldowns.set(player, COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    @Nullable
    private static LivingEntity sweepForTarget(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 base = new Vec3(player.getX(), player.getY() + player.getBbHeight() * 0.5D, player.getZ());

        for (int distance = 1; distance <= 32; distance++) {
            double tolerance = SEARCH_RANGE;
            if (distance > 10) {
                tolerance += 3.0D;
            }
            if (distance > 20) {
                tolerance += 5.0D;
            }
            Vec3 point = base.add(look.scale(distance));
            AABB box = new AABB(point.x - tolerance, point.y - tolerance, point.z - tolerance,
                    point.x + tolerance, point.y + tolerance, point.z + tolerance);
            List<LivingEntity> found = player.level().getEntitiesOfClass(LivingEntity.class, box, entity -> entity != player && entity.isAlive());
            if (!found.isEmpty()) {
                return found.get(player.level().random.nextInt(found.size()));
            }
        }
        return null;
    }

    @Nullable
    private static LivingEntity randomVisibleTarget(Player player) {
        List<LivingEntity> candidates = new ArrayList<>(player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(32.0D), entity -> entity != player && entity.isAlive()));
        candidates.removeIf(entity -> !player.hasLineOfSight(entity));
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(player.level().random.nextInt(candidates.size()));
    }

    private static void spawnOrb(Level level, Player player, @Nullable LivingEntity target) {
        Vec3 origin = player.getEyePosition().add(player.getLookAngle().scale(1.0D)).add(0.0D, 0.5D, 0.0D);
        EntityCrimsonOrb orb = new EntityCrimsonOrb(FREntities.CRIMSON_ORB.get(), level, player, target, true);
        orb.setPos(origin.x, origin.y, origin.z);
        orb.setDeltaMovement(player.getLookAngle().scale(0.75D));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.6F, 0.8F + (float) Math.random() * 0.2F);
        level.addFreshEntity(orb);
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<Component> lines = new ArrayList<>();
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerCast.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRIgnisCost.lore"))
                    .append(Component.literal(String.valueOf((int) (480 * FRConfig.crimsonSpellVisMult()) / 100.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRPerditioCost.lore"))
                    .append(Component.literal(String.valueOf((int) (360 * FRConfig.crimsonSpellVisMult()) / 100.0D))));
            tooltip.add(emptyLine());
            return;
        }
        lines.add(tr("item.ItemCrimsonSpell1.lore"));
        lines.add(tr("item.ItemCrimsonSpell2.lore"));
        lines.add(tr("item.ItemCrimsonSpell3.lore"));
        lines.add(emptyLine());
        lines.add(tr("item.ItemCrimsonSpell4.lore"));
        lines.add(tr("item.ItemCrimsonSpell5.lore"));
        lines.add(Component.translatable("item.ItemCrimsonSpell6_1.lore")
                .append(Component.literal(" " + (int) FRConfig.crimsonSpellDamageMIN() + "-" + (int) FRConfig.crimsonSpellDamageMAX() + " "))
                .append(Component.translatable("item.ItemCrimsonSpell6_2.lore")));
        addShiftTooltip(tooltip, lines);
        tooltip.add(emptyLine());
    }
}
