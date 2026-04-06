package buildcraft.robotics;

import net.fabricmc.api.ClientModInitializer;

public class BCRoboticsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCRobotics.LOGGER.info("BuildCraft Robotics client initializing");
    }
}
