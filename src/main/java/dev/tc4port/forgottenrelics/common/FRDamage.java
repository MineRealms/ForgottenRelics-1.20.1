package dev.tc4port.forgottenrelics.common;

import dev.tc4port.forgottenrelics.registry.FRDamageTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

/**
 * Damage helpers replacing the source mod's {@code DamageRegistryHandler} and
 * {@code SuperpositionHandler.isDamageTypeAbsolute}.
 */
public final class FRDamage {

    private FRDamage() {
    }

    /**
     * Damage the source mod classified as "absolute": it must never be
     * converted, stored, deflected or split by relics.
     */
    public static boolean isAbsolute(DamageSource source) {
        return source.is(FRDamageTypes.TRUE_DAMAGE)
                || source.is(FRDamageTypes.TRUE_DAMAGE_UNDEFINED)
                || source.is(FRDamageTypes.SUPERPOSITION)
                || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypeTags.BYPASSES_ARMOR);
    }

    /** Source {@code DamageSourceSuperposition}: split damage between ring wearers. */
    public static DamageSource superposition(Level level, net.minecraft.world.entity.Entity attacker) {
        return FRDamageTypes.source(level, FRDamageTypes.SUPERPOSITION, attacker, attacker);
    }
}
