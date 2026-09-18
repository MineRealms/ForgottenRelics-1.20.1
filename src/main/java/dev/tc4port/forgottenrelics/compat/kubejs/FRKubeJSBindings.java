package dev.tc4port.forgottenrelics.compat.kubejs;

import dev.tc4port.forgottenrelics.research.ForgottenKnowledge;
import dev.tc4port.thaumcraft.api.research.ResearchApi;
import dev.tc4port.thaumcraft.api.research.ResearchEntryFlag;
import dev.tc4port.thaumcraft.api.research.ResearchKey;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Script bindings replacing the source {@code mods.forgottenrelics.JusticeHandler}
 * and {@code mods.forgottenrelics.Research} ZenScript classes.
 */
public final class FRKubeJSBindings {

    /** Equivalent of {@code JusticeHandler.addTrigger(researchKey, stack)}. */
    public void addJusticeTrigger(String researchKey, ItemStack stack) {
        ForgottenKnowledge.addTrigger(researchKey, stack);
    }

    /** Equivalent of {@code JusticeHandler.obliterateJusticeTriggers(researchKey)}. */
    public void obliterateJusticeTriggers(String researchKey) {
        ForgottenKnowledge.obliterateTriggers(researchKey);
    }

    /** Removes every custom trigger; call at the top of a reload script before re-adding. */
    public void clearJusticeTriggers() {
        ForgottenKnowledge.clear();
    }

    /** Current forgotten-knowledge map for inspection. */
    public Map<String, List<ItemStack>> forgottenKnowledge() {
        return ForgottenKnowledge.snapshot();
    }

    /** Read-only replacement for {@code Research.isHidden} / {@code isLost}. */
    public boolean isHidden(String researchKey) {
        return hasFlag(researchKey, ResearchEntryFlag.HIDDEN);
    }

    public boolean isLost(String researchKey) {
        return hasFlag(researchKey, ResearchEntryFlag.LOST);
    }

    public int warp(String researchKey) {
        return ResearchApi.warp(ResearchKey.parse(researchKey));
    }

    public boolean isComplete(Player player, String researchKey) {
        return ResearchApi.isComplete(player, ResearchKey.parse(researchKey));
    }

    public boolean isDiscovered(Player player, String researchKey) {
        return ResearchApi.isDiscovered(player, ResearchKey.parse(researchKey));
    }

    private static boolean hasFlag(String researchKey, ResearchEntryFlag flag) {
        return ResearchApi.registry().get(ResearchKey.parse(researchKey)).map(entry -> entry.hasFlag(flag)).orElse(false);
    }
}
