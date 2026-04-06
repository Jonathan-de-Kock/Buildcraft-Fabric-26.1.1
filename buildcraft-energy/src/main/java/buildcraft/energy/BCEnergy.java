package buildcraft.energy;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCEnergy implements ModInitializer {
    public static final String MOD_ID = "buildcraftenergy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BuildCraft Energy initializing");
    }
}
