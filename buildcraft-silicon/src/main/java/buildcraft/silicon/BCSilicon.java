package buildcraft.silicon;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import buildcraft.silicon.tile.BCSlnBlockEntities;

public class BCSilicon implements ModInitializer {
    public static final String MOD_ID = "buildcraftsilicon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BuildCraft Silicon initializing");
        BCSiliconBlocks.register();
        BCSlnBlockEntities.register();

        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
            .modifyOutputEvent(buildcraft.core.BCCoreCreativeTab.TAB_KEY).register(output -> {
                output.accept(BCSiliconBlocks.assemblyTable);
                output.accept(BCSiliconBlocks.integrationTable);
                output.accept(BCSiliconBlocks.chargingTable);
                output.accept(BCSiliconBlocks.laser);
            });
    }
}
