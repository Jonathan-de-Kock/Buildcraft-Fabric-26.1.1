package buildcraft.robotics;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import buildcraft.robotics.tile.BCRobBlockEntities;

public class BCRobotics implements ModInitializer {
    public static final String MOD_ID = "buildcraftrobotics";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BuildCraft Robotics initializing");
        BCRoboticsBlocks.register();
        BCRobBlockEntities.register();

        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
            .modifyOutputEvent(buildcraft.core.BCCoreCreativeTab.TAB_KEY).register(output -> {
                output.accept(BCRoboticsBlocks.zonePlanner);
            });
    }
}
