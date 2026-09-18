package dev.tc4port.forgottenrelics.common;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.item.ItemFateTome;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.network.NotificationPayload;
import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.forgottenrelics.registry.FRItems;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import dev.tc4port.thaumcraft.block.entity.EldritchPortalBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Port of the source {@code RelicsEventHandler}.
 *
 * <p>Not carried over: the Botania "relic king" achievement trigger (1.20.1
 * Botania no longer ships that achievement) and the Gaia Guardian water-arena
 * anti-abuse check (the entity was reworked upstream). Everything else keeps
 * the source behavior, including the intentional randomness.</p>
 */
public class FRGameplayEvents {

    // ------------------------------------------------------------------
    // tick
    // ------------------------------------------------------------------

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Outer Lands anti-abuse: teleport back to the overworld when the
        // player escapes the maze shell.
        if (FRConfig.outerLandsAntiAbuseEnabled()
                && player.tickCount % FRConfig.outerLandsCheckrate() == 0
                && player.level().dimension().equals(EldritchPortalBlockEntity.OUTER_LANDS)) {
            if (!rayHits(player, 24.0D) && !rayHits(player, -24.0D)) {
                ServerLevel overworld = player.server.overworld();
                if (overworld != null) {
                    player.teleportTo(overworld, player.getX(), 100.0D, player.getZ(), player.getYRot(), player.getXRot());
                }
            }
        }
    }

    private static boolean rayHits(ServerPlayer player, double verticalOffset) {
        var from = player.getEyePosition();
        var to = from.add(0.0D, verticalOffset, 0.0D);
        var hit = player.level().clip(new net.minecraft.world.level.ClipContext(
                from, to, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        return hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS;
    }

    // ------------------------------------------------------------------
    // mining charms
    // ------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        float boost = 1.0F;
        if (hasCurio(player, FRItems.ADVANCED_MINING_CHARM.get())) {
            boost += (float) FRConfig.advancedMiningCharmBoost();
        }
        if (hasCurio(player, FRItems.MINING_CHARM.get())) {
            boost += (float) FRConfig.miningCharmBoost();
        }
        event.setNewSpeed(event.getNewSpeed() * boost);
    }

    // ------------------------------------------------------------------
    // attack (pre-damage, cancellable)
    // ------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        // Chaos Core: redirect damage dealt BY its carrier.
        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && !event.isCanceled()
                && ItemSearch.hasItem(attacker, FRItems.CHAOS_CORE.get())
                && Math.random() < 0.45D) {
            LivingEntity victim = event.getEntity();
            List<Entity> nearby = nearbyEntities(victim, 16.0D);
            if (!nearby.isEmpty()) {
                float redirected = event.getAmount() * (float) (Math.random() * 2.0D);
                if (Math.random() < 0.15D) {
                    attacker.hurt(event.getSource(), redirected);
                } else {
                    nearby.get((int) (Math.random() * nearby.size())).hurt(event.getSource(), redirected);
                }
                event.setCanceled(true);
            }
        }

        if (!(event.getEntity() instanceof ServerPlayer player) || event.isCanceled()) {
            return;
        }

        // Chaos Core: redirect damage dealt TO its carrier.
        if (Math.random() < 0.42D && ItemSearch.hasItem(player, FRItems.CHAOS_CORE.get())) {
            List<Entity> nearby = nearbyEntities(player, 16.0D);
            if (!nearby.isEmpty()) {
                float redirected = event.getAmount() * (float) (Math.random() * 2.0D);
                nearby.get((int) (Math.random() * nearby.size())).hurt(event.getSource(), redirected);
                event.setCanceled(true);
                return;
            }
        }

        // Nebulous Core: dodge via random teleport.
        if (hasCurio(player, FRItems.ARCANUM.get())
                && Math.random() < FRConfig.nebulousCoreDodgeChance()
                && !FRDamage.isAbsolute(event.getSource())) {
            for (int attempt = 0; attempt < 33; attempt++) {
                double dx = (Math.random() - 0.5D) * 2.0D * 16.0D;
                double dy = (Math.random() - 0.5D) * 2.0D * 16.0D;
                double dz = (Math.random() - 0.5D) * 2.0D * 16.0D;
                if (player.randomTeleport(player.getX() + dx, player.getY() + dy, player.getZ() + dz, true)) {
                    player.invulnerableTime = 20;
                    event.setCanceled(true);
                    break;
                }
            }
        }

        if (event.isCanceled()) {
            return;
        }

        // Ring of The Seven Suns: fire damage heals instead.
        if (hasCurio(player, FRItems.DARK_SUN_RING.get()) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            if (!FRConfig.darkSunRingHealLimit() || player.invulnerableTime == 0) {
                player.heal(event.getAmount());
                player.invulnerableTime = 20;
            }
            event.setCanceled(true);
            return;
        }

        // Ring of The Seven Suns: deflect the blow back at its source.
        if (hasCurio(player, FRItems.DARK_SUN_RING.get())
                && event.getSource().getEntity() != null
                && Math.random() <= FRConfig.darkSunRingDeflectChance()) {
            if (player.invulnerableTime == 0) {
                player.invulnerableTime = 20;
                event.getSource().getEntity().hurt(event.getSource(), event.getAmount());
                event.setCanceled(true);
            }
        }
    }

    // ------------------------------------------------------------------
    // hurt (post-armor)
    // ------------------------------------------------------------------

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        // False Justice: incoming damage is doubled and converted to true damage.
        if (event.getEntity() instanceof ServerPlayer player
                && !event.isCanceled()
                && ItemSearch.hasItem(player, FRItems.FALSE_JUSTICE.get())
                && !FRDamage.isAbsolute(event.getSource())) {
            float amount = event.getAmount() * 2.0F;
            event.setCanceled(true);
            if (event.getSource().getEntity() == null) {
                player.hurt(FRDamageTypes.source(player.level(), FRDamageTypes.TRUE_DAMAGE_UNDEFINED, null, null), amount);
            } else {
                player.hurt(FRDamageTypes.trueDamage(player.level(), event.getSource().getEntity()), amount);
            }
            return;
        }

        // False Justice: outgoing damage is doubled and converted to true damage.
        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && !event.isCanceled()
                && ItemSearch.hasItem(attacker, FRItems.FALSE_JUSTICE.get())
                && !FRDamage.isAbsolute(event.getSource())) {
            float amount = event.getAmount() * 2.0F;
            event.setCanceled(true);
            event.getEntity().hurt(FRDamageTypes.trueDamage(attacker.level(), attacker), amount);
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player) || event.isCanceled()) {
            return;
        }

        // Chaos Core: random damage scaling.
        if (ItemSearch.hasItem(player, FRItems.CHAOS_CORE.get())) {
            event.setAmount(event.getAmount() * (float) (Math.random() * 2.0D));
        }

        // Ring of The Seven Suns: hard damage cap.
        if (event.getAmount() > 100.0F
                && !FRDamage.isAbsolute(event.getSource())
                && hasCurio(player, FRItems.DARK_SUN_RING.get())) {
            FRNetwork.sendToPlayer(player, new NotificationPayload(NotificationPayload.OVERDAMAGE_BLOCK));
            event.setCanceled(true);
            return;
        }

        // Ring of The Seven Suns: random incoming damage amplification.
        if (hasCurio(player, FRItems.DARK_SUN_RING.get())
                && Math.random() <= 0.25D
                && !FRDamage.isAbsolute(event.getSource())) {
            event.setAmount(event.getAmount() + event.getAmount() * (float) Math.random());
        }

        // Ancient Aegis: a nearby wearer soaks part of the blow.
        if (!hasCurio(player, FRItems.ANCIENT_AEGIS.get())) {
            ServerPlayer aegisOwner = findPlayerWithCurio(player, 32.0D, FRItems.ANCIENT_AEGIS.get());
            if (aegisOwner != null) {
                aegisOwner.hurt(event.getSource(), event.getAmount() * 0.4F);
                event.setAmount(event.getAmount() * 0.6F);
            }
        }

        // Ancient Aegis: flat reduction.
        if (hasCurio(player, FRItems.ANCIENT_AEGIS.get()) && !FRDamage.isAbsolute(event.getSource())) {
            event.setAmount(event.getAmount() * (1.0F - (float) FRConfig.ancientAegisDamageReduction()));
        }

        // Ring of Superposition: split damage among other wearers.
        if (!event.getSource().is(FRDamageTypes.SUPERPOSITION)
                && hasCurio(player, FRItems.SUPERPOSITION_RING.get())) {
            List<ServerPlayer> others = findPlayersWithCurio(player, FRItems.SUPERPOSITION_RING.get());
            others.remove(player);
            if (!others.isEmpty()) {
                double percent = 0.12D + Math.random() * 0.62D;
                float splitAmount = (float) (event.getAmount() * percent);
                var splitSource = FRDamage.superposition(player.level(), event.getSource().getEntity());
                for (ServerPlayer other : others) {
                    other.hurt(splitSource, splitAmount / others.size());
                }
                event.setAmount(event.getAmount() - splitAmount);
            }
        }

        // Amulet of The Oblivion: store the blow for a price in vis.
        if (hasCurio(player, FRItems.OBLIVION_AMULET.get()) && !FRDamage.isAbsolute(event.getSource())) {
            int cost = (int) (event.getAmount() * 8.0F * FRConfig.oblivionAmuletVisMult());
            var vis = dev.tc4port.thaumcraft.api.aspect.VisCost.ofCentivis(java.util.Map.of(
                    VisChannel.IGNIS, Math.max(1, cost),
                    VisChannel.PERDITIO, Math.max(1, cost)));
            if (dev.tc4port.thaumcraft.api.ThaumcraftApiHelper.consumeVisFromInventory(player, vis, VisAction.EXECUTE).consumed()) {
                CuriosApi.getCuriosHelper().findFirstCurio(player, FRItems.OBLIVION_AMULET.get()).ifPresent(result -> {
                    ItemStack amulet = result.stack();
                    float stored = ItemStatePlatform.getOrDefault(amulet, FRItemState.STORED_DAMAGE, 0.0F);
                    ItemStatePlatform.set(amulet, FRItemState.STORED_DAMAGE, stored + event.getAmount());
                });
                event.setCanceled(true);
            }
        }
    }

    // ------------------------------------------------------------------
    // death prevention
    // ------------------------------------------------------------------

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        // False Justice double-kill guard.
        if (event.getEntity() instanceof ServerPlayer player
                && !event.getSource().is(FRDamageTypes.TRUE_DAMAGE)
                && !event.getSource().is(FRDamageTypes.TRUE_DAMAGE_UNDEFINED)
                && ItemSearch.hasItem(player, FRItems.FALSE_JUSTICE.get())) {
            event.setCanceled(true);
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Omega Core: flat refusal to die.
        if (ItemSearch.hasItem(player, FRItems.OMEGA_CORE.get())) {
            event.setCanceled(true);
            player.setHealth(1.0F);
            return;
        }

        // Tome of Broken Fates: pay vis, heal fully, random blessing or curse.
        ItemStack fateTome = ItemSearch.findFirst(player, FRItems.FATE_TOME.get());
        if (fateTome == null) {
            return;
        }
        int cooldown = ItemStatePlatform.getOrDefault(fateTome, FRItemState.COOLDOWN, 0);
        if (cooldown > 0) {
            return;
        }
        if (!dev.tc4port.thaumcraft.api.ThaumcraftApiHelper.consumeVisFromInventory(player,
                ItemFateTome.deathPreventionCost(), VisAction.EXECUTE).consumed()) {
            return;
        }

        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());

        int minCooldown = FRConfig.fateTomeCooldownMIN() * 20;
        int maxCooldown = FRConfig.fateTomeCooldownMAX() * 20;
        if (FRConfig.fateTomeCooldownMAX() != 0) {
            ItemStatePlatform.set(fateTome, FRItemState.COOLDOWN,
                    minCooldown + (int) (Math.random() * Math.max(0, maxCooldown - minCooldown)));
        }

        if (Math.random() <= 0.75D) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1000, 0, false, true));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 1, false, true));
        }

        FREffects.burst(player.level(), player.getX(), player.getY() + 1.0D, player.getZ(), 1.5F);
        player.level().playSound(null, player.getX(), player.getY() + 1.0D, player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static boolean hasCurio(Player player, net.minecraft.world.item.Item item) {
        return CuriosApi.getCuriosHelper().findFirstCurio(player, item).isPresent();
    }

    private static List<Entity> nearbyEntities(LivingEntity center, double range) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : center.level().getEntities(center, new AABB(center.blockPosition()).inflate(range))) {
            if (entity.isAlive() && entity != center) {
                result.add(entity);
            }
        }
        return result;
    }

    private static ServerPlayer findPlayerWithCurio(Player searcher, double range, net.minecraft.world.item.Item item) {
        List<ServerPlayer> matches = findPlayersWithCurio(searcher, item);
        ServerPlayer nearest = null;
        double best = range * range;
        for (ServerPlayer candidate : matches) {
            if (candidate == searcher || candidate.level() != searcher.level()) {
                continue;
            }
            double distance = candidate.distanceToSqr(searcher);
            if (distance <= best) {
                best = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static List<ServerPlayer> findPlayersWithCurio(Player searcher, net.minecraft.world.item.Item item) {
        List<ServerPlayer> result = new ArrayList<>();
        if (!(searcher.level() instanceof ServerLevel serverLevel)) {
            return result;
        }
        for (ServerPlayer candidate : serverLevel.players()) {
            if (CuriosApi.getCuriosHelper().findFirstCurio(candidate, item).isPresent()) {
                result.add(candidate);
            }
        }
        return result;
    }
}
