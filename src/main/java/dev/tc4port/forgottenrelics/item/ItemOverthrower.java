package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.common.FRCasting;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.ForgottenRelics;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.network.EffectPayload;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
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
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Overthrower. Seizes a pointed creature and, after a hundred and fifty ticks
 * of channeling, hurls it into the Nether at a random safe landing spot.
 * Players survive the trip; the announcement is public.
 */
public class ItemOverthrower extends RelicItem implements ForgottenGear.Warping {

    private static final int USE_DURATION = 150;
    private static final double RANGE = 64.0D;
    private static final Map<UUID, LivingEntity> TARGETS = new HashMap<>();

    public ItemOverthrower(Properties properties) {
        super(properties.stacksTo(1), "ItemOverthrower");
    }

    private static VisCost channelCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.IGNIS, (int) (8 * FRConfig.overthrowerVisMult()),
                VisChannel.ORDO, (int) (5 * FRConfig.overthrowerVisMult()),
                VisChannel.PERDITIO, (int) (5 * FRConfig.overthrowerVisMult())));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.dimension().equals(Level.NETHER)) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.consume(stack);
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
        if (target == null || !target.isAlive()
                || !FRCasting.pay(player, channelCost())) {
            player.stopUsingItem();
            return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, true, false));

        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        if (level instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, center.x, center.y, center.z, 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.BANISHMENT, center.x, center.y, center.z, 0xFF1A00, 0.3F, 8));
        }

        if (remainingUseDuration % 10 == 0 && remainingUseDuration != USE_DURATION) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.33F, 2.0F);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.33F, 2.0F);
        }

        if (remainingUseDuration == 1) {
            boolean succeeded = overthrow(target, player);
            if (!succeeded && !(target instanceof ServerPlayer)) {
                target.discard();
            }
            if (level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 3; i++) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                    if (bolt != null) {
                        bolt.moveTo(center.x - 0.5D, center.y - target.getBbHeight() * 0.5D, center.z - 0.5D);
                        serverLevel.addFreshEntity(bolt);
                    }
                }
                FRNetwork.sendNear(serverLevel, center.x, center.y, center.z, 128.0D,
                        EffectPayload.at(EffectPayload.EffectType.INFERNAL, center.x, center.y, center.z, 0, 2.0F, 128));
            }
        }
    }

    private boolean overthrow(LivingEntity target, ServerPlayer overthrower) {
        ServerLevel nether = overthrower.server.getLevel(Level.NETHER);
        if (nether == null) {
            return false;
        }

        int x = (int) ((Math.random() - 0.5D) * 20002.0D);
        int z = (int) ((Math.random() - 0.5D) * 20002.0D);
        int y = findLandingY(nether, x, z);
        if (y < 0) {
            return false;
        }

        if (target instanceof ServerPlayer victim) {
            victim.teleportTo(nether, x + 0.5D, y + 0.5D, z + 0.5D, victim.getYRot(), victim.getXRot());
            Component message = Component.literal(overthrower.getGameProfile().getName() + " ")
                    .append(Component.translatable("message.overthrown1"))
                    .append(Component.literal(" " + victim.getGameProfile().getName() + " "))
                    .append(Component.translatable("message.overthrown2"));
            overthrower.server.getPlayerList().broadcastSystemMessage(message, false);
            ForgottenRelics.LOG.info("{} has overthrown {} into the Nether.",
                    overthrower.getGameProfile().getName(), victim.getGameProfile().getName());
        } else {
            target.teleportTo(nether, x + 0.5D, y + 0.5D, z + 0.5D, java.util.Set.of(), target.getYRot(), target.getXRot());
            scatterFire(nether, x, y, z);
        }
        return true;
    }

    private static int findLandingY(ServerLevel nether, int x, int z) {
        for (int candidate = 124; candidate > 0; candidate--) {
            BlockPos floor = new BlockPos(x, candidate - 1, z);
            BlockPos feet = new BlockPos(x, candidate, z);
            BlockPos head = new BlockPos(x, candidate + 1, z);
            boolean solidFloor = !nether.getBlockState(floor).getCollisionShape(nether, floor, CollisionContext.empty()).isEmpty();
            boolean freeFeet = nether.getBlockState(feet).getCollisionShape(nether, feet, CollisionContext.empty()).isEmpty();
            boolean freeHead = nether.getBlockState(head).getCollisionShape(nether, head, CollisionContext.empty()).isEmpty();
            if (solidFloor && freeFeet && freeHead) {
                return candidate;
            }
        }
        return -1;
    }

    private static void scatterFire(ServerLevel nether, int x, int y, int z) {
        for (int i = 0; i < 12; i++) {
            int xx = x + nether.random.nextInt(4) - nether.random.nextInt(4);
            int zz = z + nether.random.nextInt(4) - nether.random.nextInt(4);
            int yy = y + 4;
            while (yy > y - 4 && nether.isEmptyBlock(new BlockPos(xx, yy, zz))) {
                yy--;
            }
            BlockPos firePos = new BlockPos(xx, yy + 1, zz);
            if (nether.isEmptyBlock(firePos) && !nether.isEmptyBlock(new BlockPos(xx, yy, zz))
                    && nether.getBlockState(firePos).canBeReplaced()) {
                nether.setBlockAndUpdate(firePos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
            }
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
        return 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerTick.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRIgnisCost.lore"))
                    .append(Component.literal(String.valueOf((int) (8 * FRConfig.overthrowerVisMult()) / 100.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FROrdoCost.lore"))
                    .append(Component.literal(String.valueOf((int) (5 * FRConfig.overthrowerVisMult()) / 100.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRPerditioCost.lore"))
                    .append(Component.literal(String.valueOf((int) (5 * FRConfig.overthrowerVisMult()) / 100.0D))));
            tooltip.add(emptyLine());
            return;
        }
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemOverthrower1.lore"),
                tr("item.ItemOverthrower2.lore"),
                emptyLine(),
                tr("item.ItemOverthrower3.lore"),
                tr("item.ItemOverthrower4.lore"),
                emptyLine(),
                tr("item.ItemOverthrower5.lore"),
                tr("item.ItemOverthrower6.lore"),
                tr("item.ItemOverthrower7.lore")));
        tooltip.add(emptyLine());
    }
}
