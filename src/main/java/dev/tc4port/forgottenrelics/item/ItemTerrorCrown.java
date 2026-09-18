package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.item.base.FRArmorMaterials;
import dev.tc4port.forgottenrelics.item.base.RelicText;
import dev.tc4port.thaumcraft.api.item.AuraRevealingGear;
import dev.tc4port.thaumcraft.api.item.GogglesOverlayGear;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.mana.ManaItemHandler;

/**
 * Terror Crown. A golden helmet that is deliberately unenchantable, repairs
 * itself with Botania mana, reveals aura and goggles information, and
 * terrorizes whatever the wearer looks at.
 */
public class ItemTerrorCrown extends ArmorItem implements ForgottenGear.Warping, ForgottenGear.Repairable,
        GogglesOverlayGear, AuraRevealingGear {

    private static final int MANA_PER_DURABILITY = 200;
    private static final double GAZE_RANGE = 32.0D;

    public ItemTerrorCrown(Properties properties) {
        super(FRArmorMaterials.NOBLE_GOLD, ArmorItem.Type.HELMET, properties);
    }

    @Override
    public String getDescriptionId() {
        return "item.ItemTerrorCrown.name";
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (level.isClientSide()) {
            return;
        }

        cryHavoc(player, 24.0D);

        LivingEntity target = pointedEntity(player);
        if (target != null) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 2, true, true));
            if (!target.hasEffect(MobEffects.WITHER)) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true));
            }
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2, true, true));
        }
    }

    /** Botania Heisei Dream equivalent: turn nearby monsters on each other. */
    private static void cryHavoc(Player player, double range) {
        List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(range),
                mob -> mob.isAlive());
        if (mobs.size() <= 1) {
            return;
        }
        for (Mob mob : mobs) {
            Mob victim = mobs.get(player.getRandom().nextInt(mobs.size()));
            if (victim != mob) {
                mob.setTarget(victim);
            }
        }
    }

    private static LivingEntity pointedEntity(Player player) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getLookAngle().scale(GAZE_RANGE));
        AABB box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(GAZE_RANGE)).inflate(3.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, from, to, box,
                entity -> entity instanceof LivingEntity && entity != player);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) {
            return;
        }

        // The crown rejects enchantments outright, mirroring the source behavior.
        if (stack.hasTag() && stack.getOrCreateTag().contains("Enchantments")) {
            stack.getOrCreateTag().remove("Enchantments");
            stack.getOrCreateTag().remove("ench");
        }

        if (entity instanceof Player player
                && stack.getDamageValue() > 0
                && ManaItemHandler.instance().requestManaExact(stack, player, MANA_PER_DURABILITY, true)) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 3;
    }

    @Override
    public boolean showsThaumcraftGogglesOverlay(ItemStack stack, Player wearer) {
        return true;
    }

    @Override
    public boolean revealsThaumcraftAura(ItemStack stack, Player wearer) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicText.addShiftTooltip(tooltip, List.of(
                RelicText.tr("item.ItemTerrorCrown1.lore"),
                RelicText.tr("item.ItemTerrorCrown2.lore"),
                RelicText.emptyLine(),
                RelicText.tr("item.ItemTerrorCrown3.lore"),
                RelicText.emptyLine(),
                RelicText.tr("item.ItemTerrorCrown4.lore")));
        tooltip.add(RelicText.emptyLine());
    }
}
