package dev.tc4port.forgottenrelics.common;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.tc4port.forgottenrelics.research.ForgottenKnowledge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * {@code /forgottenknowledge}. The 1.7.10 original only existed as a
 * MineTweaker command; on 1.20.1 it becomes a normal Forge command.
 */
public class FRResearchCommand {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("forgottenknowledge")
                .requires(source -> source.hasPermission(0))
                .executes(context -> {
                    var snapshot = ForgottenKnowledge.snapshot();
                    if (snapshot.isEmpty()) {
                        context.getSource().sendSuccess(() -> Component.literal("No forgotten knowledge is registered."), false);
                        return 0;
                    }
                    context.getSource().sendSuccess(() -> Component.literal("Forgotten knowledge entries: " + snapshot.size()), false);
                    snapshot.forEach((key, triggers) -> {
                        StringBuilder line = new StringBuilder(" - ").append(key).append(": [");
                        for (int index = 0; index < triggers.size(); index++) {
                            if (index > 0) {
                                line.append(", ");
                            }
                            line.append(triggers.get(index).getItem());
                        }
                        line.append(']');
                        context.getSource().sendSuccess(() -> Component.literal(line.toString()), false);
                    });
                    return snapshot.size();
                });
        event.getDispatcher().register(root);
    }
}
