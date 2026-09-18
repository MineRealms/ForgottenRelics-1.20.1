package dev.tc4port.forgottenrelics.item.base;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 1.20.1 replacement for Botania's {@code ItemBaubleBaseModifier}: applies the
 * declared attribute modifiers while equipped and removes them on unequip.
 */
public abstract class CurioAttributeItem extends CurioRelicItem {

    protected CurioAttributeItem(Properties properties, String legacyName) {
        super(properties, legacyName);
    }

    protected CurioAttributeItem(Properties properties, String legacyName, net.minecraft.world.item.Rarity rarity) {
        super(properties, legacyName, rarity);
    }

    /** Declare the modifiers granted while this curio is equipped. */
    protected void fillModifiers(Multimap<Attribute, AttributeModifier> attributes, ItemStack stack) {
    }

    @Override
    public void onEquippedOrLoadedIntoWorld(ItemStack stack, Player player) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        this.fillModifiers(modifiers, stack);
        modifiers.forEach((attribute, modifier) -> {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null && instance.getModifier(modifier.getId()) == null) {
                instance.addTransientModifier(modifier);
            }
        });
    }

    @Override
    public void onUnequipped(ItemStack stack, Player player) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        this.fillModifiers(modifiers, stack);
        modifiers.forEach((attribute, modifier) -> {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(modifier.getId());
            }
        });
    }

    /** Stable per-item modifier UUID mirroring the source's bauble UUID scheme. */
    protected static UUID modifierId(String key) {
        return UUID.nameUUIDFromBytes(("forgottenrelics:" + key).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
