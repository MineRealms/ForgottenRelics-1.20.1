package dev.tc4port.forgottenrelics.registry;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * 1.7.10 used ad-hoc {@code DamageSource} subclasses registered in
 * {@code DamageRegistryHandler}. 1.20.1 makes damage types data driven; these
 * keys map onto the {@code data/forgottenrelics/damage_type/*.json} files and
 * preserve the original death message ids.
 */
public final class FRDamageTypes {

    public static final ResourceKey<DamageType> TRUE_DAMAGE = key("true_damage");
    public static final ResourceKey<DamageType> TRUE_DAMAGE_UNDEFINED = key("true_damage_undefined");
    public static final ResourceKey<DamageType> OBLIVION = key("oblivion");
    public static final ResourceKey<DamageType> FATE = key("fate");
    public static final ResourceKey<DamageType> FORGOTTEN_LIGHTNING = key("forgotten_lightning");
    public static final ResourceKey<DamageType> DARK_MATTER = key("dark_matter");
    public static final ResourceKey<DamageType> FORGOTTEN_MAGIC = key("forgotten_magic");
    public static final ResourceKey<DamageType> SUPERPOSITION = key("superposition");

    private FRDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ForgottenRelics.id(path));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity direct, @Nullable Entity causing) {
        Holder<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
        return new DamageSource(holder, direct, causing);
    }

    public static DamageSource trueDamage(Level level, @Nullable Entity attacker) {
        return source(level, TRUE_DAMAGE, attacker, attacker);
    }

    public static DamageSource oblivion(Level level) {
        return source(level, OBLIVION, null, null);
    }

    public static DamageSource fate(Level level, @Nullable Entity attacker) {
        return source(level, FATE, attacker, attacker);
    }
}
