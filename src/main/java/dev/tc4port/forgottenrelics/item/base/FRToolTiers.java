package dev.tc4port.forgottenrelics.item.base;

import java.util.function.Supplier;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 1.20.1 counterpart of the source {@code RelicsMaterialHandler}.
 *
 * <p>{@code PARADOXICALSTUFF} was {@code level 4, uses 3000, efficiency 16,
 * damage -4, enchantability 100}. Modern items add their own base damage, so
 * the {@code -4} becomes a {@code -3} attack damage bonus: a Paradox swings for
 * the same total as the 1.7.10 original (its real damage comes from the
 * gamble in {@code onLeftClickEntity}).</p>
 */
public final class FRToolTiers {

    public static final Tier PARADOXICAL_STUFF = new Tier() {
        @Override
        public int getUses() {
            return 3000;
        }

        @Override
        public float getSpeed() {
            return 16.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return -3.0F;
        }

        @Override
        public int getLevel() {
            return 4;
        }

        @Override
        public int getEnchantmentValue() {
            return 100;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

    private FRToolTiers() {
    }

    @SuppressWarnings("unused")
    private static Tier vanillaFallback() {
        return Tiers.NETHERITE;
    }

    @SuppressWarnings("unused")
    private static Supplier<Tier> supplier() {
        return () -> PARADOXICAL_STUFF;
    }
}
