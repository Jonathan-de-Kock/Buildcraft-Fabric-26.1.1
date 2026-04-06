package buildcraft.lib;

import net.fabricmc.api.ClientModInitializer;

import buildcraft.lib.net.BCNetworking;

public class BCLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCLib.LOGGER.info("BuildCraft Lib client initializing");
        BCNetworking.registerClientHandlers();
    }
}
