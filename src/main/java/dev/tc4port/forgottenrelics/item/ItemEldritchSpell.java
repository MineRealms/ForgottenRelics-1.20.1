package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.common.CastingCooldowns;
import dev.tc4port.forgottenrelics.entity.EntityDarkMatterOrb;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.registry.FREntities;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import dev.tc4port.thaumcraft.block.entity.EldritchPortalBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Eldritch Spell. One Entropy-heavy dark matter orb per cast; harsher inside
 * the Outer Lands (which the tooltip reflects).
 */
public class ItemEldritchSpell extends RelicItem implements ForgottenGear.Warping {

    private static final int COOLDOWN_TICKS = 20;

    public ItemEldritchSpell(Properties properties) {
        super(properties.stacksTo(1), "ItemEldritchSpell");
    }

    private static VisCost castCost() {
        return VisCost.ofCentivis(VisChannel.PERDITIO, (int) (400 * FRConfig.eldritchSpellVisMult()));
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
        if (!ThaumcraftApiHelper.consumeVisFromInventory(player, castCost(), VisAction.EXECUTE).consumed()) {
            return InteractionResultHolder.fail(stack);
        }

        Vec3 origin = player.getEyePosition().add(player.getLookAngle().scale(1.0D)).add(0.0D, 0.5D, 0.0D);
        EntityDarkMatterOrb orb = new EntityDarkMatterOrb(FREntities.DARK_MATTER_ORB.get(), level, player);
        orb.setPos(origin.x, origin.y, origin.z);
        orb.setDeltaMovement(player.getLookAngle().scale(1.5D));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.6F, 0.8F + (float) Math.random() * 0.2F);
        level.addFreshEntity(orb);

        CastingCooldowns.set(player, COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public int thaumcraftWarp(ItemStack stack, Optional<Player> wearer) {
        return 4;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean outerLands = level != null && level.dimension().equals(EldritchPortalBlockEntity.OUTER_LANDS);
        double damage = outerLands ? FRConfig.eldritchSpellDamageEx() : FRConfig.eldritchSpellDamage();

        List<Component> lines = new ArrayList<>();
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerCast.lore"));
            tooltip.add(Component.literal(" ").append(Component.translatable("item.FRPerditioCost.lore"))
                    .append(Component.literal(String.valueOf((int) (400 * FRConfig.eldritchSpellVisMult()) / 100.0D))));
            tooltip.add(emptyLine());
            return;
        }
        lines.add(tr("item.ItemEldritchSpell1.lore"));
        lines.add(tr("item.ItemEldritchSpell2.lore"));
        lines.add(tr("item.ItemEldritchSpell3.lore"));
        lines.add(emptyLine());
        lines.add(tr("item.ItemEldritchSpell4.lore"));
        lines.add(Component.translatable("item.ItemEldritchSpell5_1.lore")
                .append(Component.literal(" " + (int) damage + " "))
                .append(Component.translatable("item.ItemEldritchSpell5_2.lore")));
        lines.add(tr("item.ItemEldritchSpell6.lore"));
        lines.add(emptyLine());
        lines.add(tr("item.ItemEldritchSpell7.lore"));
        addShiftTooltip(tooltip, lines);
        tooltip.add(emptyLine());
    }
}
