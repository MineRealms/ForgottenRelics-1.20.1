package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.entity.EntityBabylonWeaponSS;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.registry.FREntities;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
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
 * Apotheosis. Summons a fan of Babylon weapons around the caster, one every
 * other tick, each of which hangs for a moment before seeking the caster's
 * gaze.
 */
public class ItemApotheosis extends RelicItem implements ForgottenGear.Warping {

    private static final int MAX_USE = 72000;
    private static final int SPAWN_TRIES = 100;
    private static final double MIN_SEPARATION = 2.0D;

    public ItemApotheosis(Properties properties) {
        super(properties.stacksTo(1), "ItemApotheosis");
    }

    private static VisCost summonCost() {
        return VisCost.ofCentivis(Map.of(
                VisChannel.TERRA, (int) (30 * FRConfig.apotheosisVisMult()),
                VisChannel.IGNIS, (int) (60 * FRConfig.apotheosisVisMult()),
                VisChannel.ORDO, (int) (50 * FRConfig.apotheosisVisMult()),
                VisChannel.PERDITIO, (int) (75 * FRConfig.apotheosisVisMult())));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(living instanceof Player player)) {
            return;
        }
        if (remainingUseDuration == MAX_USE || remainingUseDuration % 2 != 0) {
            return;
        }
        if (!ThaumcraftApiHelper.consumeVisFromInventory(player, summonCost(), VisAction.EXECUTE).consumed()) {
            return;
        }
        spawnWeapon(player);
    }

    private static void spawnWeapon(Player player) {
        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        Vec3 look = new Vec3(player.getLookAngle().x, 0.0D, player.getLookAngle().z).normalize();
        if (look.lengthSqr() < 0.0001D) {
            look = new Vec3(0.0D, 0.0D, 1.0D);
        }

        Vec3 placement = null;
        Vec3 fanBase = look.scale(2.0D + Math.random() * 10.0D);
        for (int attempt = 0; attempt < SPAWN_TRIES; attempt++) {
            double angle = (Math.random() >= 0.5D ? 80.0D : -80.0D);
            Vec3 rotated = rotateY(fanBase, angle).scale(1.0D + Math.random());
            rotated = rotated.add(look.scale(Math.random()));
            double y = center.y - 0.5D + Math.random() * 8.0D;
            Vec3 candidate = new Vec3(center.x + rotated.x, y, center.z + rotated.z);

            boolean overlap = !player.level().getEntitiesOfClass(EntityBabylonWeaponSS.class,
                    new net.minecraft.world.phys.AABB(candidate, candidate).inflate(MIN_SEPARATION)).isEmpty();
            if (!overlap) {
                placement = candidate;
                break;
            }
        }
        if (placement == null) {
            return;
        }

        EntityBabylonWeaponSS weapon = new EntityBabylonWeaponSS(FREntities.BABYLON_WEAPON.get(), player.level(), player);
        weapon.setPos(placement.x, placement.y, placement.z);
        weapon.setYRot(player.getYHeadRot());
        weapon.setVariety(player.getRandom().nextInt(12));
        weapon.setDelay(0);
        weapon.setRotation(-player.getYHeadRot() + 180.0F);
        player.level().playSound(null, placement.x, placement.y, placement.z,
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F + player.getRandom().nextFloat() * 3.0F);
        player.level().addFreshEntity(weapon);
    }

    private static Vec3 rotateY(Vec3 vector, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(vector.x * cos + vector.z * sin, vector.y, -vector.x * sin + vector.z * cos);
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
        return 5;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerSecond.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRTerraCost.lore"))
                    .append(Component.literal(String.valueOf((int) (30 * FRConfig.apotheosisVisMult()) / 100.0D * 10.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRIgnisCost.lore"))
                    .append(Component.literal(String.valueOf((int) (60 * FRConfig.apotheosisVisMult()) / 100.0D * 10.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FROrdoCost.lore"))
                    .append(Component.literal(String.valueOf((int) (50 * FRConfig.apotheosisVisMult()) / 100.0D * 10.0D))));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRPerditioCost.lore"))
                    .append(Component.literal(String.valueOf((int) (75 * FRConfig.apotheosisVisMult()) / 100.0D * 10.0D))));
            tooltip.add(emptyLine());
            return;
        }
        addShiftTooltip(tooltip, List.of(
                tr("item.ItemApotheosis1.lore"),
                emptyLine(),
                tr("item.ItemApotheosis2.lore"),
                tr("item.ItemApotheosis3.lore"),
                emptyLine(),
                Component.translatable("item.ItemApotheosis4_1.lore")
                        .append(Component.literal(" " + (int) FRConfig.damageApotheosisDirect() + " "))
                        .append(Component.translatable("item.ItemApotheosis4_2.lore")),
                Component.translatable("item.ItemApotheosis5_1.lore")
                        .append(Component.literal(" " + (int) FRConfig.damageApotheosisImpact() + " "))
                        .append(Component.translatable("item.ItemApotheosis5_2.lore")),
                tr("item.ItemApotheosis6.lore")));
        tooltip.add(emptyLine());
    }
}
