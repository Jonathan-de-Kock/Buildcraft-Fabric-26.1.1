package buildcraft.lib;

import net.fabricmc.api.ClientModInitializer;

public class BCLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCLib.LOGGER.info("BuildCraft Lib client initializing");
    }
}
