package dev.tc4port.forgottenrelics.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

/**
 * KubeJS entry point. Registered through {@code kubejs.plugins.txt}.
 *
 * <p>Replaces the source mod's MineTweaker3 ZenScript classes. Justice-trigger
 * editing is exposed as script bindings; research flag editing is handled by
 * KubeJS itself through {@code ServerEvents.highPriorityData} JSON overrides
 * (an example ships in {@code kubejs_examples/}).</p>
 */
public final class ForgottenRelicsKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("ForgottenRelics", new FRKubeJSBindings());
    }
}
