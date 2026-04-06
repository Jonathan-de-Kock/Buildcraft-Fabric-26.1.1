package buildcraft.factory;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCFactory implements ModInitializer {
    public static final String MOD_ID = "buildcraftfactory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BuildCraft Factory initializing");
    }
}
