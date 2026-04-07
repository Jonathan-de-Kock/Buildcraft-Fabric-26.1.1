package buildcraft.builders;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import buildcraft.builders.tile.BCBldBlockEntities;

public class BCBuilders implements ModInitializer {
    public static final String MOD_ID = "buildcraftbuilders";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BuildCraft Builders initializing");
        BCBuildersBlocks.register();
        BCBldBlockEntities.register();
    }
}
