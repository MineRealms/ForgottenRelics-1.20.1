package dev.tc4port.forgottenrelics.research;

import dev.tc4port.forgottenrelics.FRConfig;
import dev.tc4port.forgottenrelics.ForgottenRelics;
import dev.tc4port.thaumcraft.api.research.ResearchApi;
import dev.tc4port.thaumcraft.api.research.ResearchEntryFlag;
import dev.tc4port.thaumcraft.api.research.ResearchKey;
import dev.tc4port.thaumcraft.api.research.ScanApi;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Runtime registry of "forgotten knowledge": hidden/lost research whose private
 * {@code @key} discovery markers are granted once the player has scanned a
 * configured set of item triggers.
 *
 * <p>In 1.7.10 this map was edited by MineTweaker scripts; here it is edited by
 * KubeJS bindings and by the {@code justiceHandlerOverrides} config entry.</p>
 */
public final class ForgottenKnowledge {

    private static final Map<String, List<ItemStack>> TRIGGERS = new LinkedHashMap<>();

    private ForgottenKnowledge() {
    }

    public static void init() {
        TRIGGERS.clear();
        if (FRConfig.justiceOverridingEnabled()) {
            for (String override : FRConfig.justiceHandlerOverrides()) {
                parseOverride(override);
            }
        }
    }

    public static synchronized Map<String, List<ItemStack>> snapshot() {
        return Map.copyOf(TRIGGERS);
    }

    public static synchronized List<ItemStack> triggers(String researchKey) {
        return TRIGGERS.getOrDefault(researchKey, List.of());
    }

    public static synchronized void addTrigger(String researchKey, ItemStack stack) {
        if (researchKey == null || stack == null || stack.isEmpty()) {
            return;
        }
        TRIGGERS.computeIfAbsent(researchKey, key -> new ArrayList<>()).add(stack.copyWithCount(1));
    }

    public static synchronized void obliterateTriggers(String researchKey) {
        TRIGGERS.remove(researchKey);
    }

    public static synchronized void clear() {
        TRIGGERS.clear();
    }

    /**
     * Grants the source mod's private discovery marker to any research whose
     * triggers have all been scanned.
     */
    public static void grantAvailable(ServerPlayer player) {
        for (Map.Entry<String, List<ItemStack>> entry : snapshot().entrySet()) {
            String researchKey = entry.getKey();
            List<ItemStack> triggers = entry.getValue();
            if (triggers.isEmpty()) {
                continue;
            }

            ResearchKey key;
            try {
                key = ResearchKey.parse(researchKey);
            } catch (IllegalArgumentException invalid) {
                continue;
            }

            if (ResearchApi.isDiscovered(player, key) || ResearchApi.isComplete(player, key)) {
                continue;
            }

            // Only hidden or lost research is eligible, matching the source handler.
            var summary = ResearchApi.registry().get(key);
            if (summary.isPresent()
                    && !summary.get().hasFlag(ResearchEntryFlag.HIDDEN)
                    && !summary.get().hasFlag(ResearchEntryFlag.LOST)) {
                continue;
            }

            boolean unlocked = true;
            for (ItemStack trigger : triggers) {
                if (!ScanApi.isObjectScanned(player, trigger)) {
                    unlocked = false;
                    break;
                }
            }

            if (unlocked && Math.random() <= FRConfig.knowledgeChance()) {
                ResearchApi.discover(player, key);
                player.displayClientMessage(Component.translatable("forgottenrelics.justice.granted", researchKey)
                        .withStyle(ChatFormatting.DARK_PURPLE), false);
                return;
            }
        }
    }

    private static void parseOverride(String override) {
        int open = override.indexOf('[');
        int close = override.indexOf(']', open < 0 ? 0 : open);
        if (open <= 0 || close < 0) {
            ForgottenRelics.LOG.warn("Malformed justice handler override: {}", override);
            return;
        }
        String key = override.substring(0, open).trim();
        String[] entries = override.substring(open + 1, close).split(",");
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            parseStack(trimmed).ifPresent(stack -> addTrigger(key, stack));
        }
    }

    private static java.util.Optional<ItemStack> parseStack(String value) {
        String[] parts = value.split(":");
        if (parts.length < 2) {
            return java.util.Optional.empty();
        }
        net.minecraft.resources.ResourceLocation id =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
        if (item == net.minecraft.world.item.Items.AIR) {
            ForgottenRelics.LOG.warn("Unknown item in justice handler override: {}", value);
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new ItemStack(item));
    }
}
