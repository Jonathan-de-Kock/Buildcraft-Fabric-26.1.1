/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.tile.BlockEntityPipeHolder;

/**
 * Item representing a pipe. When used on a block face, places a pipe holder block
 * and initializes it with the correct pipe definition.
 */
public class ItemPipe extends Item {
    private final PipeDefinition definition;

    public ItemPipe(Properties properties, PipeDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    public PipeDefinition getDefinition() {
        return definition;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Check if the target position is free
        if (!level.getBlockState(pos).isAir()) {
            return InteractionResult.FAIL;
        }

        // Place the pipe holder block
        BlockState pipeState = BCTransportBlocks.pipeHolder.defaultBlockState();
        level.setBlock(pos, pipeState, 3);

        // Initialize the pipe
        if (level.getBlockEntity(pos) instanceof BlockEntityPipeHolder holder) {
            holder.initPipe(definition);
            if (context.getPlayer() != null) {
                holder.onPlacedBy(context.getPlayer(), context.getItemInHand());
            }
        }

        // Consume the item
        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
