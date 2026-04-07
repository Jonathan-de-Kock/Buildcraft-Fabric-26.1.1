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

        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
            .modifyOutputEvent(buildcraft.core.BCCoreCreativeTab.TAB_KEY).register(output -> {
                output.accept(BCBuildersBlocks.quarry);
                output.accept(BCBuildersBlocks.filler);
                output.accept(BCBuildersBlocks.builder);
                output.accept(BCBuildersBlocks.architectTable);
                output.accept(BCBuildersBlocks.electronicLibrary);
            });
    }
}
