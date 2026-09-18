package dev.tc4port.forgottenrelics.common;

import net.minecraft.world.entity.player.Player;

/**
 * Vanilla experience math, replacing Botania's {@code ExperienceHelper}.
 */
public final class ExperienceMath {

    private ExperienceMath() {
    }

    /** Experience points required to advance from {@code level} to the next level. */
    public static int xpToNextLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }

    /** Total experience points represented by a level with no partial progress. */
    public static int totalXpForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5D * level * level - 40.5D * level + 360.0D);
        }
        return (int) (4.5D * level * level - 162.5D * level + 2220.0D);
    }

    /** Level displayed for a raw experience point amount. */
    public static int levelForTotalXp(int total) {
        int level = 0;
        int remaining = total;
        while (remaining >= xpToNextLevel(level)) {
            remaining -= xpToNextLevel(level);
            level++;
        }
        return level;
    }

    /** The player's current total experience points. */
    public static int getPlayerXp(Player player) {
        return player.totalExperience;
    }

    /** Adds experience points through the vanilla pipeline. */
    public static void addPlayerXp(Player player, int amount) {
        if (amount > 0) {
            player.giveExperiencePoints(amount);
        }
    }

    /** Removes experience points, rebuilding the level state exactly. */
    public static void drainPlayerXp(Player player, int amount) {
        if (amount <= 0) {
            return;
        }
        int remaining = Math.max(0, player.totalExperience - amount);
        player.experienceLevel = 0;
        player.experienceProgress = 0.0F;
        player.totalExperience = 0;
        if (remaining > 0) {
            player.giveExperiencePoints(remaining);
        }
    }
}
