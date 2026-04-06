package buildcraft.silicon;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCSilicon implements ModInitializer {
    public static final String MOD_ID = "buildcraftsilicon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BuildCraft Silicon initializing");
    }
}
