package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.api.ForgottenGear;
import dev.tc4port.forgottenrelics.entity.EntityChaoticOrb;
import dev.tc4port.forgottenrelics.item.base.RelicItem;
import dev.tc4port.forgottenrelics.registry.FREntities;
import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.ThaumcraftApiHelper;
import java.util.LinkedHashMap;
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
 * Chaos Tome. Spray-holds orbs of pure chaos; each shot rolls a random price
 * in every single vis channel, and roughly a third of the orbs hunt.
 */
public class ItemChaosTome extends RelicItem implements ForgottenGear.Warping {

    private static final int MAX_USE = 72000;

    public ItemChaosTome(Properties properties) {
        super(properties.stacksTo(1), "ItemChaosTome");
    }

    private static VisCost rollCost() {
        Map<VisChannel, Integer> cost = new LinkedHashMap<>();
        for (VisChannel channel : VisChannel.values()) {
            cost.put(channel, Math.max(1, (int) (100 * FRConfig.chaosTomeVisMult() * Math.random())));
        }
        return VisCost.ofCentivis(cost);
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
        if (!ThaumcraftApiHelper.consumeVisFromInventory(player, rollCost(), VisAction.EXECUTE).consumed()) {
            return;
        }

        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        double x = center.x + (Math.random() - 0.5D) * 3.0D;
        double y = center.y + (Math.random() - 0.5D) * 1.0D;
        double z = center.z + (Math.random() - 0.5D) * 3.0D;

        boolean seeker = Math.random() <= 0.35D;
        EntityChaoticOrb orb = new EntityChaoticOrb(FREntities.CHAOTIC_ORB.get(), level, player, seeker);
        orb.setPos(x, y, z);
        Vec3 motion = new Vec3(x - center.x, y - center.y, z - center.z).scale(0.2D + Math.random() * 0.2D);
        orb.setDeltaMovement(motion);
        level.playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.3F, 0.8F + level.random.nextFloat() * 0.1F);
        level.addFreshEntity(orb);
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
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            tooltip.add(tr("item.FRVisPerSecond.lore"));
            for (VisChannel channel : VisChannel.values()) {
                String key = switch (channel) {
                    case AER -> "item.FRAerCost.lore";
                    case TERRA -> "item.FRTerraCost.lore";
                    case IGNIS -> "item.FRIgnisCost.lore";
                    case AQUA -> "item.FRAquaCost.lore";
                    case ORDO -> "item.FROrdoCost.lore";
                    case PERDITIO -> "item.FRPerditioCost.lore";
                };
                double value = Math.round(100 * FRConfig.chaosTomeVisMult() * Math.random() / 100.0D * 10.0D * 100.0D) / 100.0D;
                tooltip.add(Component.literal(" ").append(Component.translatable(key)).append(Component.literal(String.valueOf(value))));
            }
            tooltip.add(emptyLine());
            return;
        }

        List<Component> lines = new java.util.ArrayList<>();
        lines.add(tr("item.ItemChaosTome1.lore"));
        lines.add(emptyLine());
        lines.add(tr("item.ItemChaosTome2.lore"));
        lines.add(tr("item.ItemChaosTome3.lore"));
        lines.add(emptyLine());
        lines.add(tr("item.ItemChaosTome4.lore"));
        lines.add(emptyLine());
        lines.add(Component.translatable("item.ItemChaosTome5_1.lore")
                .append(Component.literal(" 1-" + (int) FRConfig.chaosTomeDamageCap() + " "))
                .append(Component.translatable("item.ItemChaosTome5_2.lore")));
        lines.add(tr("item.ItemChaosTome6.lore"));
        addShiftTooltip(tooltip, lines);
        tooltip.add(emptyLine());
    }
}
