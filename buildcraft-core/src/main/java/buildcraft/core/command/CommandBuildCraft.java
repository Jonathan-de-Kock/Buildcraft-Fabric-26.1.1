/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class CommandBuildCraft {
    private CommandBuildCraft() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("buildcraft")
                .then(Commands.literal("version")
                    .executes(ctx -> {
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("BuildCraft 9.0.0 for MC 26.1.1 (Fabric)"), false);
                        return 1;
                    })
                )
                .then(Commands.literal("help")
                    .executes(ctx -> {
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("BuildCraft - Extending Minecraft with pipes, engines, and automation"), false);
                        return 1;
                    })
                )
        );
    }
}
