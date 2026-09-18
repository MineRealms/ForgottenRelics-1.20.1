package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.common.CastingCooldowns;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.network.EffectPayload;
import dev.tc4port.forgottenrelics.network.FRNetwork;
import dev.tc4port.forgottenrelics.registry.FRItemState;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

/**
 * Dimensional Mirror. Sneak-use to engrave a location, then hold to travel
 * back to it - across dimensions unless the admin disabled that.
 */
public class ItemDimensionalMirror extends RelicItem {

    private static final int USE_DURATION = 80;

    public ItemDimensionalMirror(Properties properties) {
        super(properties.stacksTo(1), "ItemDimensionalMirror");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean written = ItemStatePlatform.has(stack, FRItemState.STORED_POS);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                BlockPos pos = player.blockPosition();
                ItemStatePlatform.set(stack, FRItemState.STORED_POS, pos);
                ItemStatePlatform.set(stack, FRItemState.STORED_DIMENSION, level.dimension().location());
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDER_CHEST_CLOSE, SoundSource.PLAYERS, 1.0F, 2.0F);
            }
            player.swing(hand, true);
            return InteractionResultHolder.success(stack);
        }

        if (!written) {
            return InteractionResultHolder.pass(stack);
        }

        ResourceLocation storedDim = ItemStatePlatform.getOrDefault(stack, FRItemState.STORED_DIMENSION,
                level.dimension().location());
        if (!FRConfig.interdimensionalMirror() && !storedDim.equals(level.dimension().location())) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.dimension().equals(Level.END) && !storedDim.equals(Level.END.location())) {
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

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1.0D, player.getZ(), 4, 1.5D, 1.5D, 1.5D, 1.5D);
        }

        if (remainingUseDuration != 1 || CastingCooldowns.isOnCooldown(player)) {
            return;
        }

        BlockPos stored = ItemStatePlatform.getOrDefault(stack, FRItemState.STORED_POS, null);
        ResourceLocation storedDim = ItemStatePlatform.getOrDefault(stack, FRItemState.STORED_DIMENSION,
                level.dimension().location());
        if (stored == null) {
            return;
        }

        teleportBurst(level, player.position().x, player.position().y + 1.0D, player.position().z);

        ResourceKey<Level> dimensionKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, storedDim);
        ServerLevel targetLevel = player.server.getLevel(dimensionKey);
        if (targetLevel != null) {
            player.teleportTo(targetLevel, stored.getX() + 0.5D, stored.getY() + 0.5D, stored.getZ() + 0.5D,
                    player.getYRot(), player.getXRot());
        } else {
            player.teleportTo(stored.getX() + 0.5D, stored.getY() + 0.5D, stored.getZ() + 0.5D);
        }

        level.playSound(null, stored.getX() + 0.5D, stored.getY() + 0.5D, stored.getZ() + 0.5D,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F + (float) Math.random() * 0.2F);
        CastingCooldowns.set(player, 40);
    }

    private static void teleportBurst(Level level, double x, double y, double z) {
        if (level instanceof ServerLevel serverLevel) {
            FRNetwork.sendNear(serverLevel, x, y, z, 64.0D,
                    EffectPayload.at(EffectPayload.EffectType.BURST, x, y, z, 0, 1.25F, 24));
        }
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
    public boolean isFoil(ItemStack stack) {
        return ItemStatePlatform.has(stack, FRItemState.STORED_POS);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemDimensionalMirror1.lore"),
                tr("item.ItemDimensionalMirror2.lore"),
                tr("item.ItemDimensionalMirror3.lore"),
                emptyLine(),
                tr("item.ItemDimensionalMirror4.lore")));

        BlockPos stored = ItemStatePlatform.getOrDefault(stack, FRItemState.STORED_POS, null);
        if (stored != null) {
            ResourceLocation dimension = ItemStatePlatform.getOrDefault(stack, FRItemState.STORED_DIMENSION,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"));
            tooltip.add(emptyLine());
            tooltip.add(tr("item.MirrorLoc.lore"));
            tooltip.add(emptyLine());
            tooltip.add(Component.translatable("item.MirrorX.lore").append(Component.literal(String.valueOf(stored.getX()))));
            tooltip.add(Component.translatable("item.MirrorY.lore").append(Component.literal(String.valueOf(stored.getY()))));
            tooltip.add(Component.translatable("item.MirrorZ.lore").append(Component.literal(String.valueOf(stored.getZ()))));
            tooltip.add(emptyLine());
            tooltip.add(Component.translatable("item.MirrorDimension.lore").append(Component.literal(dimension.toString())));
        }
    }
}
