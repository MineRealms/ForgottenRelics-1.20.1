package dev.tc4port.forgottenrelics.registry;

import dev.tc4port.forgottenrelics.ForgottenRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class FRCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> REGISTRY =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForgottenRelics.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = REGISTRY.register("forgotten_relics",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tabForgottenRelics"))
                    .icon(() -> new ItemStack(FRItems.FALSE_JUSTICE.get()))
                    .displayItems((parameters, output) -> FRItems.REGISTRY.getEntries().forEach(entry -> output.accept(entry.get())))
                    .build());

    private FRCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
