package dev.tc4port.forgottenrelics.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Shiny Stone motionless-charge state (source NBT keys {@code Static},
 * {@code HealRate}, {@code LastX/Y/Z}).
 */
public record ShinyStoneState(int charge, int healRate, double lastX, double lastY, double lastZ) {

    public static final ShinyStoneState EMPTY = new ShinyStoneState(0, 0, 0.0D, 0.0D, 0.0D);

    public static final Codec<ShinyStoneState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("charge").orElse(0).forGetter(ShinyStoneState::charge),
            Codec.INT.fieldOf("heal_rate").orElse(0).forGetter(ShinyStoneState::healRate),
            Codec.DOUBLE.fieldOf("last_x").orElse(0.0D).forGetter(ShinyStoneState::lastX),
            Codec.DOUBLE.fieldOf("last_y").orElse(0.0D).forGetter(ShinyStoneState::lastY),
            Codec.DOUBLE.fieldOf("last_z").orElse(0.0D).forGetter(ShinyStoneState::lastZ)
    ).apply(instance, ShinyStoneState::new));

    public ShinyStoneState withCharge(int newCharge) {
        return new ShinyStoneState(newCharge, this.healRate, this.lastX, this.lastY, this.lastZ);
    }

    public ShinyStoneState withHealRate(int newRate) {
        return new ShinyStoneState(this.charge, newRate, this.lastX, this.lastY, this.lastZ);
    }

    public ShinyStoneState at(double x, double y, double z) {
        return new ShinyStoneState(this.charge, this.healRate, x, y, z);
    }
}
