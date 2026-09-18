package dev.tc4port.forgottenrelics.item;

import com.google.common.collect.Multimap;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.item.base.CurioAttributeItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Ancient Aegis. Grants full knockback resistance and regenerates one health
 * per second; the source's damage reduction is handled by the damage event
 * hook.
 */
public class ItemAncientAegis extends CurioAttributeItem {

    public ItemAncientAegis(Properties properties) {
        super(properties.stacksTo(1), "ItemAncientAegis");
    }

    @Override
    protected void fillModifiers(Multimap<Attribute, AttributeModifier> attributes, ItemStack stack) {
        attributes.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                modifierId("ancient_aegis_knockback"), "forgottenrelics:ancient_aegis_knockback",
                1.0D, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public void onWornTick(ItemStack stack, Player player) {
        if (!player.level().isClientSide() && player.tickCount % 20 == 0 && player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                Component.translatable("item.ItemAncientAegis1_1.lore")
                        .append(Component.literal(" " + (int) (FRConfig.ancientAegisDamageReduction() * 100.0F)))
                        .append(Component.translatable("item.ItemAncientAegis1_2.lore")),
                tr("item.ItemAncientAegis2.lore"),
                tr("item.ItemAncientAegis3.lore"),
                emptyLine(),
                tr("item.ItemAncientAegis4.lore"),
                tr("item.ItemAncientAegis5.lore"),
                emptyLine(),
                CurioTooltips.belt()));
    }
}
