package dev.tc4port.forgottenrelics;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 1.20.1 replacement for the 1.7.10 {@code RelicsConfigHandler}.
 *
 * <p>Every entry mirrors a property of the original {@code relics.cfg}.
 * Names and defaults are preserved so existing server configurations keep
 * working after the port.</p>
 */
public final class FRConfig {

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();

    // Generic Config
    private static final ForgeConfigSpec.BooleanValue FALSE_JUSTICE_ENABLED;
    private static final ForgeConfigSpec.BooleanValue DEIFIC_AMULET_EFFECT_IMMUNITY;
    private static final ForgeConfigSpec.BooleanValue DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS;
    private static final ForgeConfigSpec.BooleanValue DEIFIC_AMULET_INVINCIBILITY;
    private static final ForgeConfigSpec.BooleanValue DARK_SUN_RING_HEAL_LIMIT;
    private static final ForgeConfigSpec.DoubleValue DARK_SUN_RING_DAMAGE_CAP;
    private static final ForgeConfigSpec.DoubleValue DARK_SUN_RING_DEFLECT_CHANCE;
    private static final ForgeConfigSpec.BooleanValue INTERDIMENSIONAL_MIRROR;
    private static final ForgeConfigSpec.DoubleValue ANCIENT_AEGIS_DAMAGE_REDUCTION;
    private static final ForgeConfigSpec.DoubleValue NEBULOUS_CORE_DODGE_CHANCE;
    private static final ForgeConfigSpec.DoubleValue MINING_CHARM_BOOST;
    private static final ForgeConfigSpec.DoubleValue MINING_CHARM_REACH;
    private static final ForgeConfigSpec.DoubleValue ADVANCED_MINING_CHARM_BOOST;
    private static final ForgeConfigSpec.DoubleValue ADVANCED_MINING_CHARM_REACH;
    private static final ForgeConfigSpec.IntValue SHINY_STONE_CHECKRATE;
    private static final ForgeConfigSpec.DoubleValue ARCANUM_GEN_RATE;
    private static final ForgeConfigSpec.DoubleValue SOUL_TOME_DIVISOR;
    private static final ForgeConfigSpec.DoubleValue REVELATION_MODIFIER;
    private static final ForgeConfigSpec.BooleanValue TELEKINESIS_ON_PLAYERS;
    private static final ForgeConfigSpec.BooleanValue ALT_TELEKINESIS_ALGORITHM;
    private static final ForgeConfigSpec.IntValue FATE_TOME_COOLDOWN_MIN;
    private static final ForgeConfigSpec.IntValue FATE_TOME_COOLDOWN_MAX;
    private static final ForgeConfigSpec.IntValue OBLIVION_STONE_SOFT_CAP;
    private static final ForgeConfigSpec.IntValue OBLIVION_STONE_HARD_CAP;
    private static final ForgeConfigSpec.BooleanValue VOID_GRIMOIRE_ENABLED;
    private static final ForgeConfigSpec.BooleanValue UPDATE_NOTIFICATIONS_ENABLED;
    private static final ForgeConfigSpec.BooleanValue MEMES_ENABLED;
    private static final ForgeConfigSpec.IntValue OUTER_LANDS_CHECKRATE;
    private static final ForgeConfigSpec.BooleanValue OUTER_LANDS_ANTI_ABUSE_ENABLED;
    private static final ForgeConfigSpec.DoubleValue GUARDIAN_ANTI_ABUSE_RADIUS;
    private static final ForgeConfigSpec.DoubleValue GUARDIAN_NOTIFICATION_RADIUS;
    private static final ForgeConfigSpec.IntValue RESEARCH_INSPECTION_FREQUENCY;
    private static final ForgeConfigSpec.DoubleValue KNOWLEDGE_CHANCE;

    // Damage Values
    private static final ForgeConfigSpec.DoubleValue DAMAGE_APOTHEOSIS_DIRECT;
    private static final ForgeConfigSpec.DoubleValue DAMAGE_APOTHEOSIS_IMPACT;
    private static final ForgeConfigSpec.DoubleValue DAMAGE_LUNAR_FLARE_DIRECT;
    private static final ForgeConfigSpec.DoubleValue DAMAGE_LUNAR_FLARE_IMPACT;
    private static final ForgeConfigSpec.DoubleValue DAMAGE_THUNDERPEAL_DIRECT;
    private static final ForgeConfigSpec.DoubleValue DAMAGE_THUNDERPEAL_BOLT;
    private static final ForgeConfigSpec.DoubleValue PARADOX_DAMAGE_CAP;
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_TOME_DAMAGE_MIN;
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_TOME_DAMAGE_MAX;
    private static final ForgeConfigSpec.DoubleValue NUCLEAR_FURY_DAMAGE_MIN;
    private static final ForgeConfigSpec.DoubleValue NUCLEAR_FURY_DAMAGE_MAX;
    private static final ForgeConfigSpec.DoubleValue CRIMSON_SPELL_DAMAGE_MIN;
    private static final ForgeConfigSpec.DoubleValue CRIMSON_SPELL_DAMAGE_MAX;
    private static final ForgeConfigSpec.DoubleValue CHAOS_TOME_DAMAGE_CAP;
    private static final ForgeConfigSpec.DoubleValue ELDRITCH_SPELL_DAMAGE;
    private static final ForgeConfigSpec.DoubleValue ELDRITCH_SPELL_DAMAGE_EX;

    // Vis Costs
    private static final ForgeConfigSpec.DoubleValue APOTHEOSIS_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue CHAOS_TOME_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue CRIMSON_SPELL_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue DEIFIC_AMULET_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue DISCORD_TOME_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue DORMANT_ARCANUM_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue ELDRITCH_SPELL_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue FATE_TOME_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue LUNAR_FLARES_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue NUCLEAR_FURY_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue OBLIVION_AMULET_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue SOUL_TOME_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_TOME_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue WEATHER_STONE_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue THUNDERPEAL_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue OVERTHROWER_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue VOID_GRIMOIRE_VIS_MULT;
    private static final ForgeConfigSpec.DoubleValue OBELISK_DRAINER_VIS_MULT;

    // Thaumcraft Overrides
    private static final ForgeConfigSpec.IntValue NOTIFICATION_DELAY;
    private static final ForgeConfigSpec.IntValue RUNIC_RECHARGE_SPEED;
    private static final ForgeConfigSpec.IntValue RUNIC_RECHARGE_DELAY;
    private static final ForgeConfigSpec.IntValue RUNIC_COST;

    // Justice Handler Overrides
    private static final ForgeConfigSpec.BooleanValue JUSTICE_OVERRIDING_ENABLED;
    private static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> JUSTICE_HANDLER_OVERRIDES;

    static {
        B.comment("Forgotten Relics port configuration. Mirrors relics.cfg of the 1.7.10 original.").push("generic");
        FALSE_JUSTICE_ENABLED = B.comment("Enables the True Damage conversion of False Justice.").define("falseJusticeEnabled", true);
        DEIFIC_AMULET_EFFECT_IMMUNITY = B.define("deificAmuletEffectImmunity", true);
        DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS = B.define("deificAmuletOnlyNegatesDebuffs", false);
        DEIFIC_AMULET_INVINCIBILITY = B.define("deificAmuletInvincibility", true);
        DARK_SUN_RING_HEAL_LIMIT = B.define("darkSunRingHealLimit", false);
        DARK_SUN_RING_DAMAGE_CAP = B.defineInRange("darkSunRingDamageCap", 100.0D, 0.0D, 32768.0D);
        DARK_SUN_RING_DEFLECT_CHANCE = B.defineInRange("darkSunRingDeflectChance", 0.2D, 0.0D, 1.0D);
        INTERDIMENSIONAL_MIRROR = B.define("interdimensionalMirror", true);
        ANCIENT_AEGIS_DAMAGE_REDUCTION = B.defineInRange("ancientAegisDamageReduction", 0.25D, 0.0D, 1.0D);
        NEBULOUS_CORE_DODGE_CHANCE = B.defineInRange("nebulousCoreDodgeChance", 0.4D, 0.0D, 1.0D);
        MINING_CHARM_BOOST = B.defineInRange("miningCharmBoost", 1.0D, 0.0D, 32000.0D);
        MINING_CHARM_REACH = B.defineInRange("miningCharmReach", 2.0D, 0.0D, 32.0D);
        ADVANCED_MINING_CHARM_BOOST = B.defineInRange("advancedMiningCharmBoost", 3.0D, 0.0D, 32000.0D);
        ADVANCED_MINING_CHARM_REACH = B.defineInRange("advancedMiningCharmReach", 4.0D, 0.0D, 32.0D);
        SHINY_STONE_CHECKRATE = B.defineInRange("shinyStoneCheckrate", 4, 1, 2048);
        ARCANUM_GEN_RATE = B.defineInRange("arcanumGenRate", 1.0D, 0.0D, 32000.0D);
        SOUL_TOME_DIVISOR = B.comment("A divisor applied to damage absorbed by the Edict of a Thousand Damned Souls.").defineInRange("soulTomeDivisor", 10.0D, 0.0D, Double.MAX_VALUE);
        REVELATION_MODIFIER = B.defineInRange("revelationModifier", 1.0D, 0.001D, 32.0D);
        TELEKINESIS_ON_PLAYERS = B.define("telekinesisOnPlayers", true);
        ALT_TELEKINESIS_ALGORITHM = B.define("altTelekinesisAlgorithm", false);
        FATE_TOME_COOLDOWN_MIN = B.defineInRange("fateTomeCooldownMIN", 30, 0, 32768);
        FATE_TOME_COOLDOWN_MAX = B.defineInRange("fateTomeCooldownMAX", 90, 0, 32768);
        OBLIVION_STONE_SOFT_CAP = B.comment("Amount of items the Oblivion Stone can store before it starts consuming extra durability.").defineInRange("oblivionStoneSoftCap", 28, 0, 2048);
        OBLIVION_STONE_HARD_CAP = B.comment("Absolute limit of items the Oblivion Stone can store.").defineInRange("oblivionStoneHardCap", 64, 0, 2048);
        VOID_GRIMOIRE_ENABLED = B.define("voidGrimoireEnabled", true);
        UPDATE_NOTIFICATIONS_ENABLED = B.define("updateNotificationsEnabled", true);
        MEMES_ENABLED = B.define("memesEnabled", false);
        OUTER_LANDS_CHECKRATE = B.defineInRange("outerLandsCheckrate", 20, 1, 1024000);
        OUTER_LANDS_ANTI_ABUSE_ENABLED = B.define("outerLandsAntiAbuseEnabled", true);
        GUARDIAN_ANTI_ABUSE_RADIUS = B.defineInRange("guardianAntiAbuseRadius", 16.0D, 0.0D, 1024.0D);
        GUARDIAN_NOTIFICATION_RADIUS = B.defineInRange("guardianNotificationRadius", 64.0D, -32768.0D, 32768.0D);
        RESEARCH_INSPECTION_FREQUENCY = B.comment("How often (in ticks) the Justice Handler inspects player inventories.").defineInRange("researchInspectionFrequency", 200, 1, 1024000);
        KNOWLEDGE_CHANCE = B.defineInRange("knowledgeChance", 1.0D, 0.0D, 1.0D);
        B.pop();

        B.push("damage_values");
        DAMAGE_APOTHEOSIS_DIRECT = B.defineInRange("damageApotheosisDirect", 100.0D, 0.0D, 32000.0D);
        DAMAGE_APOTHEOSIS_IMPACT = B.defineInRange("damageApotheosisImpact", 75.0D, 0.0D, 32000.0D);
        DAMAGE_LUNAR_FLARE_DIRECT = B.defineInRange("damageLunarFlareDirect", 72.0D, 0.0D, 32000.0D);
        DAMAGE_LUNAR_FLARE_IMPACT = B.defineInRange("damageLunarFlareImpact", 40.0D, 0.0D, 32000.0D);
        DAMAGE_THUNDERPEAL_DIRECT = B.defineInRange("damageThunderpealDirect", 24.0D, 0.0D, 32000.0D);
        DAMAGE_THUNDERPEAL_BOLT = B.defineInRange("damageThunderpealBolt", 16.0D, 0.0D, 32000.0D);
        PARADOX_DAMAGE_CAP = B.comment("Upper bound of the random damage The Paradox deals to target and wielder combined.").defineInRange("paradoxDamageCap", 200.0D, 0.0D, 32000.0D);
        TELEKINESIS_TOME_DAMAGE_MIN = B.defineInRange("telekinesisTomeDamageMIN", 16.0D, 0.0D, 32000.0D);
        TELEKINESIS_TOME_DAMAGE_MAX = B.defineInRange("telekinesisTomeDamageMAX", 40.0D, 0.0D, 32000.0D);
        NUCLEAR_FURY_DAMAGE_MIN = B.defineInRange("nuclearFuryDamageMIN", 24.0D, 0.0D, 32000.0D);
        NUCLEAR_FURY_DAMAGE_MAX = B.defineInRange("nuclearFuryDamageMAX", 32.0D, 0.0D, 32000.0D);
        CRIMSON_SPELL_DAMAGE_MIN = B.defineInRange("crimsonSpellDamageMIN", 42.0D, 0.0D, 32000.0D);
        CRIMSON_SPELL_DAMAGE_MAX = B.defineInRange("crimsonSpellDamageMAX", 100.0D, 0.0D, 32000.0D);
        CHAOS_TOME_DAMAGE_CAP = B.defineInRange("chaosTomeDamageCap", 100.0D, 0.0D, 32000.0D);
        ELDRITCH_SPELL_DAMAGE = B.defineInRange("eldritchSpellDamage", 32.5D, 0.0D, 32000.0D);
        ELDRITCH_SPELL_DAMAGE_EX = B.defineInRange("eldritchSpellDamageEx", 100.0D, 0.0D, 32000.0D);
        B.pop();

        B.push("vis_costs");
        APOTHEOSIS_VIS_MULT = B.defineInRange("apotheosisVisMult", 1.0D, 0.0D, 1024.0D);
        CHAOS_TOME_VIS_MULT = B.defineInRange("chaosTomeVisMult", 1.0D, 0.0D, 1024.0D);
        CRIMSON_SPELL_VIS_MULT = B.defineInRange("crimsonSpellVisMult", 1.0D, 0.0D, 1024.0D);
        DEIFIC_AMULET_VIS_MULT = B.defineInRange("deificAmuletVisCost", 1.0D, 0.0D, 1024.0D);
        DISCORD_TOME_VIS_MULT = B.defineInRange("discordTomeVisCost", 1.0D, 0.0D, 1024.0D);
        DORMANT_ARCANUM_VIS_MULT = B.defineInRange("dormantArcanumVisMult", 1.0D, 0.0D, 1024.0D);
        ELDRITCH_SPELL_VIS_MULT = B.defineInRange("eldritchSpellVisCost", 1.0D, 0.0D, 1024.0D);
        FATE_TOME_VIS_MULT = B.defineInRange("fateTomeVisCost", 1.0D, 0.0D, 1024.0D);
        LUNAR_FLARES_VIS_MULT = B.defineInRange("lunarFlaresVisCost", 1.0D, 0.0D, 1024.0D);
        NUCLEAR_FURY_VIS_MULT = B.defineInRange("nuclearFuryVisCost", 1.0D, 0.0D, 1024.0D);
        OBLIVION_AMULET_VIS_MULT = B.defineInRange("oblivionAmuletVisCost", 1.0D, 0.0D, 1024.0D);
        SOUL_TOME_VIS_MULT = B.defineInRange("soulTomeVisCost", 1.0D, 0.0D, 1024.0D);
        TELEKINESIS_TOME_VIS_MULT = B.defineInRange("telekinesisTomeVisCost", 1.0D, 0.0D, 1024.0D);
        WEATHER_STONE_VIS_MULT = B.defineInRange("weatherStoneVisCost", 1.0D, 0.0D, 1024.0D);
        THUNDERPEAL_VIS_MULT = B.defineInRange("thunderpealVisCost", 1.0D, 0.0D, 1024.0D);
        OVERTHROWER_VIS_MULT = B.defineInRange("overthrowerVisCost", 1.0D, 0.0D, 1024.0D);
        VOID_GRIMOIRE_VIS_MULT = B.defineInRange("voidGrimoireVisMult", 1.0D, 0.0D, 1024.0D);
        OBELISK_DRAINER_VIS_MULT = B.defineInRange("obeliskDrainerVisGen", 1.0D, 0.0D, 32000.0D);
        B.pop();

        B.push("thaumcraft_overrides");
        NOTIFICATION_DELAY = B.defineInRange("notificationDelay", 2000, 0, 32768);
        RUNIC_RECHARGE_SPEED = B.defineInRange("runicRechargeSpeed", 750, 0, 32768);
        RUNIC_RECHARGE_DELAY = B.defineInRange("runicRechargeDelay", 40, 0, 32768);
        RUNIC_COST = B.defineInRange("runicCost", 10, 0, 32768);
        B.pop();

        B.push("justice_handler_overrides");
        JUSTICE_OVERRIDING_ENABLED = B.comment("Allows the justiceHandlerOverrides list to replace item triggers for forgotten knowledge research.").define("justiceOverridingEnabled", false);
        JUSTICE_HANDLER_OVERRIDES = B.comment("Format: ResearchKey[modid:itemname, modid:itemname, ...]").defineListAllowEmpty("justiceHandlerOverrides", () -> java.util.List.of(
                "EldritchSpell[thaumcraft:eldritch_object]",
                "AdvancedMiningCharm[botania:mana_resource, forgottenrelics:mining_charm, minecraft:diamond_pickaxe]",
                "TerrorCrown[minecraft:ender_eye, minecraft:nether_star, minecraft:golden_helmet]"
        ), o -> o instanceof String);
        B.pop();

        SPEC = B.build();
    }

    private FRConfig() {
    }

    public static boolean falseJusticeEnabled() { return FALSE_JUSTICE_ENABLED.get(); }
    public static boolean deificAmuletEffectImmunity() { return DEIFIC_AMULET_EFFECT_IMMUNITY.get(); }
    public static boolean deificAmuletOnlyNegatesDebuffs() { return DEIFIC_AMULET_ONLY_NEGATES_DEBUFFS.get(); }
    public static boolean deificAmuletInvincibility() { return DEIFIC_AMULET_INVINCIBILITY.get(); }
    public static boolean darkSunRingHealLimit() { return DARK_SUN_RING_HEAL_LIMIT.get(); }
    public static double darkSunRingDamageCap() { return DARK_SUN_RING_DAMAGE_CAP.get(); }
    public static double darkSunRingDeflectChance() { return DARK_SUN_RING_DEFLECT_CHANCE.get(); }
    public static boolean interdimensionalMirror() { return INTERDIMENSIONAL_MIRROR.get(); }
    public static double ancientAegisDamageReduction() { return ANCIENT_AEGIS_DAMAGE_REDUCTION.get(); }
    public static double nebulousCoreDodgeChance() { return NEBULOUS_CORE_DODGE_CHANCE.get(); }
    public static double miningCharmBoost() { return MINING_CHARM_BOOST.get(); }
    public static double miningCharmReach() { return MINING_CHARM_REACH.get(); }
    public static double advancedMiningCharmBoost() { return ADVANCED_MINING_CHARM_BOOST.get(); }
    public static double advancedMiningCharmReach() { return ADVANCED_MINING_CHARM_REACH.get(); }
    public static int shinyStoneCheckrate() { return SHINY_STONE_CHECKRATE.get(); }
    public static double arcanumGenRate() { return ARCANUM_GEN_RATE.get(); }
    public static double soulTomeDivisor() { return SOUL_TOME_DIVISOR.get(); }
    public static double revelationModifier() { return REVELATION_MODIFIER.get(); }
    public static boolean telekinesisOnPlayers() { return TELEKINESIS_ON_PLAYERS.get(); }
    public static boolean altTelekinesisAlgorithm() { return ALT_TELEKINESIS_ALGORITHM.get(); }
    public static int fateTomeCooldownMIN() { return FATE_TOME_COOLDOWN_MIN.get(); }
    public static int fateTomeCooldownMAX() { return FATE_TOME_COOLDOWN_MAX.get(); }
    public static int oblivionStoneSoftCap() { return OBLIVION_STONE_SOFT_CAP.get(); }
    public static int oblivionStoneHardCap() { return OBLIVION_STONE_HARD_CAP.get(); }
    public static boolean voidGrimoireEnabled() { return VOID_GRIMOIRE_ENABLED.get(); }
    public static boolean updateNotificationsEnabled() { return UPDATE_NOTIFICATIONS_ENABLED.get(); }
    public static boolean memesEnabled() { return MEMES_ENABLED.get(); }
    public static int outerLandsCheckrate() { return OUTER_LANDS_CHECKRATE.get(); }
    public static boolean outerLandsAntiAbuseEnabled() { return OUTER_LANDS_ANTI_ABUSE_ENABLED.get(); }
    public static double guardianAntiAbuseRadius() { return GUARDIAN_ANTI_ABUSE_RADIUS.get(); }
    public static double guardianNotificationRadius() { return GUARDIAN_NOTIFICATION_RADIUS.get(); }
    public static int researchInspectionFrequency() { return RESEARCH_INSPECTION_FREQUENCY.get(); }
    public static double knowledgeChance() { return KNOWLEDGE_CHANCE.get(); }

    public static double damageApotheosisDirect() { return DAMAGE_APOTHEOSIS_DIRECT.get(); }
    public static double damageApotheosisImpact() { return DAMAGE_APOTHEOSIS_IMPACT.get(); }
    public static double damageLunarFlareDirect() { return DAMAGE_LUNAR_FLARE_DIRECT.get(); }
    public static double damageLunarFlareImpact() { return DAMAGE_LUNAR_FLARE_IMPACT.get(); }
    public static double damageThunderpealDirect() { return DAMAGE_THUNDERPEAL_DIRECT.get(); }
    public static double damageThunderpealBolt() { return DAMAGE_THUNDERPEAL_BOLT.get(); }
    public static double paradoxDamageCap() { return PARADOX_DAMAGE_CAP.get(); }
    public static double telekinesisTomeDamageMIN() { return TELEKINESIS_TOME_DAMAGE_MIN.get(); }
    public static double telekinesisTomeDamageMAX() { return TELEKINESIS_TOME_DAMAGE_MAX.get(); }
    public static double nuclearFuryDamageMIN() { return NUCLEAR_FURY_DAMAGE_MIN.get(); }
    public static double nuclearFuryDamageMAX() { return NUCLEAR_FURY_DAMAGE_MAX.get(); }
    public static double crimsonSpellDamageMIN() { return CRIMSON_SPELL_DAMAGE_MIN.get(); }
    public static double crimsonSpellDamageMAX() { return CRIMSON_SPELL_DAMAGE_MAX.get(); }
    public static double chaosTomeDamageCap() { return CHAOS_TOME_DAMAGE_CAP.get(); }
    public static double eldritchSpellDamage() { return ELDRITCH_SPELL_DAMAGE.get(); }
    public static double eldritchSpellDamageEx() { return ELDRITCH_SPELL_DAMAGE_EX.get(); }

    public static double apotheosisVisMult() { return APOTHEOSIS_VIS_MULT.get(); }
    public static double chaosTomeVisMult() { return CHAOS_TOME_VIS_MULT.get(); }
    public static double crimsonSpellVisMult() { return CRIMSON_SPELL_VIS_MULT.get(); }
    public static double deificAmuletVisMult() { return DEIFIC_AMULET_VIS_MULT.get(); }
    public static double discordTomeVisMult() { return DISCORD_TOME_VIS_MULT.get(); }
    public static double dormantArcanumVisMult() { return DORMANT_ARCANUM_VIS_MULT.get(); }
    public static double eldritchSpellVisMult() { return ELDRITCH_SPELL_VIS_MULT.get(); }
    public static double fateTomeVisMult() { return FATE_TOME_VIS_MULT.get(); }
    public static double lunarFlaresVisMult() { return LUNAR_FLARES_VIS_MULT.get(); }
    public static double nuclearFuryVisMult() { return NUCLEAR_FURY_VIS_MULT.get(); }
    public static double oblivionAmuletVisMult() { return OBLIVION_AMULET_VIS_MULT.get(); }
    public static double soulTomeVisMult() { return SOUL_TOME_VIS_MULT.get(); }
    public static double telekinesisTomeVisMult() { return TELEKINESIS_TOME_VIS_MULT.get(); }
    public static double weatherStoneVisMult() { return WEATHER_STONE_VIS_MULT.get(); }
    public static double thunderpealVisMult() { return THUNDERPEAL_VIS_MULT.get(); }
    public static double overthrowerVisMult() { return OVERTHROWER_VIS_MULT.get(); }
    public static double voidGrimoireVisMult() { return VOID_GRIMOIRE_VIS_MULT.get(); }
    public static double obeliskDrainerVisMult() { return OBELISK_DRAINER_VIS_MULT.get(); }

    public static int notificationDelay() { return NOTIFICATION_DELAY.get(); }
    public static int runicRechargeSpeed() { return RUNIC_RECHARGE_SPEED.get(); }
    public static int runicRechargeDelay() { return RUNIC_RECHARGE_DELAY.get(); }
    public static int runicCost() { return RUNIC_COST.get(); }

    public static boolean justiceOverridingEnabled() { return JUSTICE_OVERRIDING_ENABLED.get(); }
    public static java.util.List<? extends String> justiceHandlerOverrides() { return JUSTICE_HANDLER_OVERRIDES.get(); }
}
