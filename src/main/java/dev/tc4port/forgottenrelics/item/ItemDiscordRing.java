package dev.tc4port.forgottenrelics.item;

import dev.tc4port.forgottenrelics.item.base.CurioRelicItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Discord Ring. The ability itself is driven by the client keybind and the
 * networking layer; this class contributes the wearable behavior and tooltip.
 */
public class ItemDiscordRing extends CurioRelicItem {

    public ItemDiscordRing(Properties properties) {
        super(properties.stacksTo(1), "ItemDiscordRing");
    }

    @Override
    public void onWornTick(ItemStack stack, Player player) {
        // Source parity: no passive effect. The keybind drives the ability.
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String keyName = "???";
        if (level != null && level.isClientSide()) {
            try {
                keyName = dev.tc4port.forgottenrelics.client.FRClientKeys.discordKeyName();
            } catch (RuntimeException | LinkageError ignored) {
                // Key mappings are not available this early; keep the placeholder.
            }
        }

        addShiftTooltip(tooltip, List.of(
                tr("item.ItemDiscordRing1.lore"),
                tr("item.ItemDiscordRing2.lore"),
                tr("item.ItemDiscordRing3.lore"),
                emptyLine(),
                Component.translatable("item.ItemDiscordRing4.lore")
                        .append(Component.translatable("item.FRCode6.lore"))
                        .append(Component.literal(keyName)),
                emptyLine(),
                CurioTooltips.ring()));
    }
}
