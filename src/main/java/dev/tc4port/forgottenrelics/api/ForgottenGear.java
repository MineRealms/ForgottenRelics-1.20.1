package dev.tc4port.forgottenrelics.api;

import dev.tc4port.thaumcraft.api.item.RepairableGear;
import dev.tc4port.thaumcraft.api.item.RunicGear;
import dev.tc4port.thaumcraft.api.item.VisDiscountGear;
import dev.tc4port.thaumcraft.api.item.WarpingGear;

/**
 * Re-exports the Thaumcraft 4R equipment hooks under the names used by the
 * Forgotten Relics port, so item classes do not depend on the raw API layout.
 */
public final class ForgottenGear {

    private ForgottenGear() {
    }

    /** TC4 {@code IWarpingGear}. */
    public interface Warping extends WarpingGear {
    }

    /** TC4 {@code IRunicArmor} marker plus charge callback. */
    public interface Runic extends RunicGear {
    }

    /** TC4 {@code IVisDiscountGear}. */
    public interface VisDiscount extends VisDiscountGear {
    }

    /** TC4 {@code IRepairable}. */
    public interface Repairable extends RepairableGear {
    }
}
