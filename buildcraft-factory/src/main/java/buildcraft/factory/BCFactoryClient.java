package buildcraft.factory;

import net.fabricmc.api.ClientModInitializer;

public class BCFactoryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCFactory.LOGGER.info("BuildCraft Factory client initializing");
    }
}
