package dev.tc4port.forgottenrelics.registry;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import dev.tc4port.forgottenrelics.item.ItemAdvancedMiningCharm;
import dev.tc4port.forgottenrelics.item.ItemAncientAegis;
import dev.tc4port.forgottenrelics.item.ItemApotheosis;
import dev.tc4port.forgottenrelics.item.ItemArcanum;
import dev.tc4port.forgottenrelics.item.ItemChaosCore;
import dev.tc4port.forgottenrelics.item.ItemChaosTome;
import dev.tc4port.forgottenrelics.item.ItemCrimsonSpell;
import dev.tc4port.forgottenrelics.item.ItemDarkSunRing;
import dev.tc4port.forgottenrelics.item.ItemDeificAmulet;
import dev.tc4port.forgottenrelics.item.ItemDimensionalMirror;
import dev.tc4port.forgottenrelics.item.ItemDiscordRing;
import dev.tc4port.forgottenrelics.item.ItemEldritchSpell;
import dev.tc4port.forgottenrelics.item.ItemDormantArcanum;
import dev.tc4port.forgottenrelics.item.ItemFalseJustice;
import dev.tc4port.forgottenrelics.item.ItemFateTome;
import dev.tc4port.forgottenrelics.item.ItemGhastlySkull;
import dev.tc4port.forgottenrelics.item.ItemLunarFlares;
import dev.tc4port.forgottenrelics.item.ItemMiningCharm;
import dev.tc4port.forgottenrelics.item.ItemObeliskDrainer;
import dev.tc4port.forgottenrelics.item.ItemMissileTome;
import dev.tc4port.forgottenrelics.item.ItemOblivionAmulet;
import dev.tc4port.forgottenrelics.item.ItemOblivionStone;
import dev.tc4port.forgottenrelics.item.ItemOmegaCore;
import dev.tc4port.forgottenrelics.item.ItemOverthrower;
import dev.tc4port.forgottenrelics.item.ItemParadox;
import dev.tc4port.forgottenrelics.item.ItemShinyStone;
import dev.tc4port.forgottenrelics.item.ItemSoulTome;
import dev.tc4port.forgottenrelics.item.ItemSuperpositionRing;
import dev.tc4port.forgottenrelics.item.ItemTelekinesisTome;
import dev.tc4port.forgottenrelics.item.ItemTeleportationTome;
import dev.tc4port.forgottenrelics.item.ItemTerrorCrown;
import dev.tc4port.forgottenrelics.item.ItemThunderpeal;
import dev.tc4port.forgottenrelics.item.ItemVoidGrimoire;
import dev.tc4port.forgottenrelics.item.ItemWastelayer;
import dev.tc4port.forgottenrelics.item.ItemWeatherStone;
import dev.tc4port.forgottenrelics.item.ItemXPTome;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Item registry. Registry paths correspond to the original 1.7.10 unlocalized
 * names in snake_case; every item keeps its original translation key through
 * {@code RelicItem#getDescriptionId()}.
 */
public final class FRItems {

    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, ForgottenRelics.MOD_ID);

    public static final RegistryObject<Item> FALSE_JUSTICE =
            REGISTRY.register("false_justice", () -> new ItemFalseJustice(new Item.Properties()));

    public static final RegistryObject<Item> PARADOX =
            REGISTRY.register("paradox", () -> new ItemParadox(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DEIFIC_AMULET =
            REGISTRY.register("deific_amulet", () -> new ItemDeificAmulet(new Item.Properties()));

    public static final RegistryObject<Item> OBLIVION_AMULET =
            REGISTRY.register("oblivion_amulet", () -> new ItemOblivionAmulet(new Item.Properties()));

    public static final RegistryObject<Item> FATE_TOME =
            REGISTRY.register("fate_tome", () -> new ItemFateTome(new Item.Properties()));

    public static final RegistryObject<Item> CHAOS_CORE =
            REGISTRY.register("chaos_core", () -> new ItemChaosCore(new Item.Properties()));

    public static final RegistryObject<Item> OMEGA_CORE =
            REGISTRY.register("omega_core", () -> new ItemOmegaCore(new Item.Properties()));

    public static final RegistryObject<Item> GHASTLY_SKULL =
            REGISTRY.register("ghastly_skull", () -> new ItemGhastlySkull(new Item.Properties()));

    public static final RegistryObject<Item> WASTELAYER =
            REGISTRY.register("wastelayer", () -> new ItemWastelayer(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> OBLIVION_STONE =
            REGISTRY.register("oblivion_stone", () -> new ItemOblivionStone(new Item.Properties()));

    public static final RegistryObject<Item> DARK_SUN_RING =
            REGISTRY.register("dark_sun_ring", () -> new ItemDarkSunRing(new Item.Properties()));

    public static final RegistryObject<Item> SUPERPOSITION_RING =
            REGISTRY.register("superposition_ring", () -> new ItemSuperpositionRing(new Item.Properties()));

    public static final RegistryObject<Item> MINING_CHARM =
            REGISTRY.register("mining_charm", () -> new ItemMiningCharm(new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_MINING_CHARM =
            REGISTRY.register("advanced_mining_charm", () -> new ItemAdvancedMiningCharm(new Item.Properties()));

    public static final RegistryObject<Item> ANCIENT_AEGIS =
            REGISTRY.register("ancient_aegis", () -> new ItemAncientAegis(new Item.Properties()));

    public static final RegistryObject<Item> ARCANUM =
            REGISTRY.register("arcanum", () -> new ItemArcanum(new Item.Properties()));

    public static final RegistryObject<Item> DORMANT_ARCANUM =
            REGISTRY.register("dormant_arcanum", () -> new ItemDormantArcanum(new Item.Properties()));

    public static final RegistryObject<Item> SHINY_STONE =
            REGISTRY.register("shiny_stone", () -> new ItemShinyStone(new Item.Properties()));

    public static final RegistryObject<Item> DISCORD_RING =
            REGISTRY.register("discord_ring", () -> new ItemDiscordRing(new Item.Properties()));

    public static final RegistryObject<Item> XP_TOME =
            REGISTRY.register("xp_tome", () -> new ItemXPTome(new Item.Properties()));

    public static final RegistryObject<Item> WEATHER_STONE =
            REGISTRY.register("weather_stone", () -> new ItemWeatherStone(new Item.Properties()));

    public static final RegistryObject<Item> MISSILE_TOME =
            REGISTRY.register("missile_tome", () -> new ItemMissileTome(new Item.Properties()));

    public static final RegistryObject<Item> THUNDERPEAL =
            REGISTRY.register("thunderpeal", () -> new ItemThunderpeal(new Item.Properties()));

    public static final RegistryObject<Item> CRIMSON_SPELL =
            REGISTRY.register("crimson_spell", () -> new ItemCrimsonSpell(new Item.Properties()));

    public static final RegistryObject<Item> ELDRITCH_SPELL =
            REGISTRY.register("eldritch_spell", () -> new ItemEldritchSpell(new Item.Properties()));

    public static final RegistryObject<Item> LUNAR_FLARES =
            REGISTRY.register("lunar_flares", () -> new ItemLunarFlares(new Item.Properties()));

    public static final RegistryObject<Item> CHAOS_TOME =
            REGISTRY.register("chaos_tome", () -> new ItemChaosTome(new Item.Properties()));

    public static final RegistryObject<Item> TELEPORTATION_TOME =
            REGISTRY.register("teleportation_tome", () -> new ItemTeleportationTome(new Item.Properties()));

    public static final RegistryObject<Item> OBELISK_DRAINER =
            REGISTRY.register("obelisk_drainer", () -> new ItemObeliskDrainer(new Item.Properties()));

    public static final RegistryObject<Item> TERROR_CROWN =
            REGISTRY.register("terror_crown", () -> new ItemTerrorCrown(new Item.Properties().durability(1000)));

    public static final RegistryObject<Item> DIMENSIONAL_MIRROR =
            REGISTRY.register("dimensional_mirror", () -> new ItemDimensionalMirror(new Item.Properties()));

    public static final RegistryObject<Item> SOUL_TOME =
            REGISTRY.register("soul_tome", () -> new ItemSoulTome(new Item.Properties()));

    public static final RegistryObject<Item> VOID_GRIMOIRE =
            REGISTRY.register("void_grimoire", () -> new ItemVoidGrimoire(new Item.Properties()));

    public static final RegistryObject<Item> OVERTHROWER =
            REGISTRY.register("overthrower", () -> new ItemOverthrower(new Item.Properties()));

    public static final RegistryObject<Item> APOTHEOSIS =
            REGISTRY.register("apotheosis", () -> new ItemApotheosis(new Item.Properties()));

    public static final RegistryObject<Item> TELEKINESIS_TOME =
            REGISTRY.register("telekinesis_tome", () -> new ItemTelekinesisTome(new Item.Properties()));

    private FRItems() {
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
