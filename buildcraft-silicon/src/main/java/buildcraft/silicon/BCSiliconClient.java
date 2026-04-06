package buildcraft.silicon;

import net.fabricmc.api.ClientModInitializer;

public class BCSiliconClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCSilicon.LOGGER.info("BuildCraft Silicon client initializing");
    }
}
