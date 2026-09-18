package dev.tc4port.forgottenrelics.item;

import com.google.common.collect.Multimap;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.item.base.CurioAttributeItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

/** Ethereal Mining Charm, the stronger sibling of the Mining Charm. */
public class ItemAdvancedMiningCharm extends CurioAttributeItem {

    public ItemAdvancedMiningCharm(Properties properties) {
        super(properties.stacksTo(1), "ItemAdvancedMiningCharm");
    }

    @Override
    protected void fillModifiers(Multimap<Attribute, AttributeModifier> attributes, ItemStack stack) {
        attributes.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(
                modifierId("advanced_mining_charm_reach"), "forgottenrelics:advanced_mining_charm_reach",
                FRConfig.advancedMiningCharmReach(), AttributeModifier.Operation.ADDITION));
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                Component.translatable("item.ItemAdvancedMiningCharm1_1.lore")
                        .append(Component.literal(" " + (int) (FRConfig.advancedMiningCharmBoost() * 100.0F)))
                        .append(Component.translatable("item.ItemAdvancedMiningCharm1_2.lore")),
                Component.translatable("item.ItemAdvancedMiningCharm2_1.lore")
                        .append(Component.literal(" " + FRConfig.advancedMiningCharmReach()))
                        .append(Component.translatable("item.ItemAdvancedMiningCharm2_2.lore")),
                emptyLine(),
                CurioTooltips.ring()));
    }
}
