package dev.tc4port.forgottenrelics.registry;

import com.mojang.serialization.Codec;
import dev.tc4port.forgottenrelics.ForgottenRelics;
import dev.tc4port.forgottenrelics.data.SupersolidData;
import dev.tc4port.thaumcraft.api.data.ItemStateKey;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;

/**
 * Typed item state fields.
 *
 * <p>Thaumcraft 4R abstracts item state behind {@code ItemStateKey}; on 1.20.1
 * it is backed by stack NBT, on 1.21+ by data components. Using this API keeps
 * the addon source identical across both targets.</p>
 */
public final class FRItemState {

    /** Oblivion Stone contents. */
    public static final ItemStateKey<SupersolidData> SUPERSOLID = new ItemStateKey<>(
            ForgottenRelics.id("supersolid"), SupersolidData.CODEC);

    /** Shared integer cooldown used by several relics (1.7.10 {@code ICooldown} / {@code IFateCooldown}). */
    public static final ItemStateKey<Integer> COOLDOWN = new ItemStateKey<>(
            ForgottenRelics.id("cooldown"), Codec.INT);

    /** Persistent random identifier of a Tome of Broken Fates copy. */
    public static final ItemStateKey<Integer> FATE_ID = new ItemStateKey<>(
            ForgottenRelics.id("fate_id"), Codec.INT);

    /** Damage stored by the Amulet of The Oblivion. */
    public static final ItemStateKey<Float> STORED_DAMAGE = new ItemStateKey<>(
            ForgottenRelics.id("stored_damage"), Codec.FLOAT);

    /** Oblivion Stone operation mode (0 void / 1 deduplicate / 2 emergency slot), plus the source's +100 sneak flag. */
    public static final ItemStateKey<Integer> MODE = new ItemStateKey<>(
            ForgottenRelics.id("mode"), Codec.INT);

    private FRItemState() {
    }

    public static void register(Object bus) {
        ItemStatePlatform.register(bus, java.util.List.of(SUPERSOLID, COOLDOWN, FATE_ID, STORED_DAMAGE, MODE));
    }
}
