/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.factory;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.factory.block.BlockChute;
import buildcraft.factory.block.BlockMiningWell;
import buildcraft.factory.block.BlockPump;
import buildcraft.factory.block.BlockTank;
import buildcraft.lib.block.BlockBCBase;
import buildcraft.lib.block.BlockBCTile;
import buildcraft.lib.registry.BCRegistration;

public final class BCFactoryBlocks {
    public static Block tank;
    public static Block pump;
    public static Block floodGate;
    public static Block miningWell;
    public static Block chute;
    public static Block autoWorkbench;

    private BCFactoryBlocks() {}

    public static void register() {
        String modId = BCFactory.MOD_ID;

        BlockBehaviour.Properties tankProps = BlockBehaviour.Properties.of()
            .strength(3.0f, 6.0f)
            .sound(SoundType.GLASS)
            .noOcclusion();

        tank = BCRegistration.registerBlockAndItem(modId, "tank", tankProps, BlockTank::new);

        BlockBehaviour.Properties machineProps = BlockBehaviour.Properties.of()
            .strength(5.0f, 10.0f)
            .sound(SoundType.METAL);

        pump = BCRegistration.registerBlockAndItem(modId, "pump", machineProps, BlockPump::new);
        floodGate = BCRegistration.registerBlockAndItem(modId, "flood_gate", machineProps, BlockBCBase::new);
        miningWell = BCRegistration.registerBlockAndItem(modId, "mining_well", machineProps, BlockMiningWell::new);
        chute = BCRegistration.registerBlockAndItem(modId, "chute", machineProps, BlockChute::new);
        autoWorkbench = BCRegistration.registerBlockAndItem(modId, "auto_workbench", machineProps, BlockBCBase::new);
    }
}
