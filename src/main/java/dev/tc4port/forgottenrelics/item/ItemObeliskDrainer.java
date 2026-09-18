package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.common.WandSupport;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.network.LightningPayload;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import dev.tc4port.thaumcraft.block.entity.EldritchObeliskBlockEntity;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

/**
 * Devourer of The Void. Drains a nearby eldritch obelisk: every 30 ticks it
 * hurts the holder a token amount, heals them, feeds them and charges a wand
 * with primal vis while arcs of lightning lash the obelisk.
 */
public class ItemObeliskDrainer extends RelicItem implements ForgottenGear.Warping, ForgottenGear.Repairable {

    private static final int MAX_USE = 72000;
    private static final double RANGE = 16.0D;

    public ItemObeliskDrainer(Properties properties) {
        super(properties.stacksTo(1), "ItemObeliskDrainer");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        BlockPos obelisk = findObelisk(player);
        if (obelisk == null) {
            return InteractionResultHolder.fail(stack);
        }
        ItemStatePlatform.set(stack, FRItemState.DETECTED_POS, obelisk);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(living instanceof ServerPlayer player)) {
            return;
        }
        BlockPos obeliskPos = ItemStatePlatform.getOrDefault(stack, FRItemState.DETECTED_POS, null);
        if (obeliskPos == null) {
            player.stopUsingItem();
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(obeliskPos);
        if (!(blockEntity instanceof EldritchObeliskBlockEntity) || player.position().distanceTo(Vec3.atCenterOf(obeliskPos)) > RANGE) {
            player.stopUsingItem();
            return;
        }

        if (remainingUseDuration != MAX_USE && remainingUseDuration % 30 == 0) {
            drainTick(player, obeliskPos);
        }
        if (Math.random() <= 0.175D && level instanceof ServerLevel serverLevel) {
            Vec3 origin = Vec3.atCenterOf(obeliskPos).add(0.0D, 2.5D, 0.0D);
            Vec3 target = origin.add((Math.random() - 0.5D) * 4.0D, (Math.random() - 0.5D) * 4.0D, (Math.random() - 0.5D) * 4.0D);
            FRNetwork.sendNear(serverLevel, origin.x, origin.y, origin.z, 64.0D,
                    new LightningPayload(origin.x, origin.y, origin.z, target.x, target.y, target.z, 0x9A3CFF, 0.05F));
        }
    }

    private void drainTick(ServerPlayer player, BlockPos obeliskPos) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 obelisk = Vec3.atCenterOf(obeliskPos).add(0.0D, 2.5D, 0.0D);
        Vec3 playerCenter = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        Vec3 middle = playerCenter.add(obelisk.subtract(playerCenter).scale(0.5D));
        for (int i = 0; i < 4; i++) {
            FRNetwork.sendNear(serverLevel, obelisk.x, obelisk.y, obelisk.z, 64.0D,
                    new LightningPayload(obelisk.x, obelisk.y, obelisk.z, middle.x, middle.y, middle.z, 0x9A3CFF, 0.075F));
        }
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 0.8F);

        player.hurt(player.damageSources().generic(), 0.01F);
        player.heal(4.0F);
        player.getFoodData().eat(2, 1.0F);

        VisChannel channel = VisChannel.values()[(int) (Math.random() * VisChannel.values().length)];
        int amount = (int) ((5.0D + Math.random() * 15.0D) * 1.5D * FRConfig.obeliskDrainerVisMult());
        WandSupport.chargeRandomWand(player, WandSupport.wandSearch(player), channel, Math.max(1, amount / 100));

        FRNetwork.sendToPlayer(player, EffectPayload.at(EffectPayload.EffectType.SPARKLE,
                obelisk.x, obelisk.y, obelisk.z, 0xAA55FF, 1.0F, 16));
    }

    private static BlockPos findObelisk(Player player) {
        BlockPos center = player.blockPosition();
        for (int chunkX = -1; chunkX <= 1; chunkX++) {
            for (int chunkZ = -1; chunkZ <= 1; chunkZ++) {
                LevelChunk chunk = player.level().getChunk(center.getX() / 16 + chunkX, center.getZ() / 16 + chunkZ);
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof EldritchObeliskBlockEntity
                            && blockEntity.getBlockPos().distSqr(center) <= RANGE * RANGE) {
                        return blockEntity.getBlockPos();
                    }
                }
            }
        }
        return null;
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
        return 4;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemObeliskDrainer1.lore"),
                tr("item.ItemObeliskDrainer2.lore"),
                emptyLine(),
                tr("item.ItemObeliskDrainer3.lore"),
                tr("item.ItemObeliskDrainer4.lore"),
                tr("item.ItemObeliskDrainer5.lore")));
        tooltip.add(emptyLine());
    }
}
