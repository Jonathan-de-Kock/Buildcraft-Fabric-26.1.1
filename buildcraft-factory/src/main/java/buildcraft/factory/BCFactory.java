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
        BCFactoryBlocks.register();
        BCFactoryBlockEntities.register();
        BCFactoryItems.register();

        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
            .modifyOutputEvent(buildcraft.core.BCCoreCreativeTab.TAB_KEY).register(output -> {
                output.accept(BCFactoryBlocks.tank);
                output.accept(BCFactoryBlocks.pump);
                output.accept(BCFactoryBlocks.floodGate);
                output.accept(BCFactoryBlocks.miningWell);
                output.accept(BCFactoryBlocks.chute);
                output.accept(BCFactoryBlocks.autoWorkbench);
            });
    }
}
