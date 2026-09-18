package dev.tc4port.forgottenrelics.item;

import com.google.common.collect.Multimap;
import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.item.base.CurioAttributeItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

/**
 * Mining Charm. The source mod extended Botania's reach; 1.20.1 Botania has no
 * such API, so the reach bonus becomes a {@code forge:block_reach} attribute
 * modifier (1.7.10 reach distance is measured in blocks, matching ADDITION).
 */
public class ItemMiningCharm extends CurioAttributeItem {

    public ItemMiningCharm(Properties properties) {
        super(properties.stacksTo(1), "ItemMiningCharm", Rarity.UNCOMMON);
    }

    @Override
    protected void fillModifiers(Multimap<Attribute, AttributeModifier> attributes, ItemStack stack) {
        attributes.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(
                modifierId("mining_charm_reach"), "forgottenrelics:mining_charm_reach",
                FRConfig.miningCharmReach(), AttributeModifier.Operation.ADDITION));
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addShiftTooltip(tooltip, List.of(
                Component.translatable("item.ItemMiningCharm1_1.lore")
                        .append(Component.literal(" " + (int) (FRConfig.miningCharmBoost() * 100.0F)))
                        .append(Component.translatable("item.ItemMiningCharm1_2.lore")),
                Component.translatable("item.ItemMiningCharm2_1.lore")
                        .append(Component.literal(" " + FRConfig.miningCharmReach()))
                        .append(Component.translatable("item.ItemMiningCharm2_2.lore")),
                emptyLine(),
                CurioTooltips.ring()));
    }
}
