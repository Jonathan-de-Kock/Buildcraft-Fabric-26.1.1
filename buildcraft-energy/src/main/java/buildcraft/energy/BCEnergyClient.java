package buildcraft.energy;

import net.fabricmc.api.ClientModInitializer;

public class BCEnergyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCEnergy.LOGGER.info("BuildCraft Energy client initializing");
    }
}
