package buildcraft.core;

import net.fabricmc.api.ClientModInitializer;

public class BCCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCCore.LOGGER.info("BuildCraft Core client initializing");
    }
}
