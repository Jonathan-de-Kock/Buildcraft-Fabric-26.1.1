package buildcraft.core;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import buildcraft.core.tile.BCCoreBlockEntities;

public class BCCore implements ModInitializer {
    public static final String MOD_ID = "buildcraftcore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BuildCraft Core initializing");
        BCCoreBlocks.register();
        BCCoreBlockEntities.register();
        BCCoreItems.register();
        BCCoreCreativeTab.register();
    }
}
