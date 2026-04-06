package buildcraft.builders;

import net.fabricmc.api.ClientModInitializer;

public class BCBuildersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCBuilders.LOGGER.info("BuildCraft Builders client initializing");
    }
}
