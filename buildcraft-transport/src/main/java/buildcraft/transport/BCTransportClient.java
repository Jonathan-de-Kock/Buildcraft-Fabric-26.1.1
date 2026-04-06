package buildcraft.transport;

import net.fabricmc.api.ClientModInitializer;

public class BCTransportClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCTransport.LOGGER.info("BuildCraft Transport client initializing");
    }
}
