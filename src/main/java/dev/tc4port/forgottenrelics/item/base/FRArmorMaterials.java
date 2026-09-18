package dev.tc4port.forgottenrelics.item.base;

import java.util.Map;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 1.20.1 counterpart of the source {@code RelicsMaterialHandler} armor
 * material: {@code NOBLEGOLD}, durability 40 base, defense {3, 9, 6, 3},
 * enchantability 0.
 */
public final class FRArmorMaterials {

    public static final ArmorMaterial NOBLE_GOLD = new ArmorMaterial() {

        private static final Map<ArmorItem.Type, Integer> DEFENSE = Map.of(
                ArmorItem.Type.HELMET, 3,
                ArmorItem.Type.CHESTPLATE, 8,
                ArmorItem.Type.LEGGINGS, 6,
                ArmorItem.Type.BOOTS, 3);

        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return switch (type) {
                case BOOTS -> 13;
                case LEGGINGS -> 15;
                case CHESTPLATE -> 16;
                case HELMET -> 11;
                default -> 0;
            };
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return DEFENSE.getOrDefault(type, 0);
        }

        @Override
        public int getEnchantmentValue() {
            return 0;
        }

        @Override
        public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_GOLD;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.GOLD_INGOT);
        }

        @Override
        public String getName() {
            return "forgottenrelics_noble_gold";
        }

        @Override
        public float getToughness() {
            return 0.0F;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0F;
        }

    };

    private FRArmorMaterials() {
    }
}
