package buildcraft.lib;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import buildcraft.lib.net.BCNetworking;

public class BCLib implements ModInitializer {
    public static final String MOD_ID = "buildcraftlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BuildCraft Lib initializing");
        BCNetworking.registerPayloads();
        BCNetworking.registerServerHandlers();
    }
}
