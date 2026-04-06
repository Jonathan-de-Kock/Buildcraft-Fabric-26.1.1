package buildcraft.api.transport.pipe;

import java.util.IdentityHashMap;
import java.util.Map;

/** The central holding class for all pipe related registers and methods.
 *
 * <p>Note: In the Fabric port, Forge Capability fields have been removed. Pipe/pluggable lookups
 * will be handled via Fabric API Lookup in the lib module. */
public final class PipeApi {
    public static IPipeRegistry pipeRegistry;
    public static PipeFlowType flowStructure;
    public static PipeFlowType flowItems;
    public static PipeFlowType flowFluids;
    public static PipeFlowType flowPower;

    /** The default transfer information used if a pipe definition has not been registered. Note that this is replaced
     * by BuildCraft Transport to config-defined values. */
    public static FluidTransferInfo fluidInfoDefault = new FluidTransferInfo(20, 10);

    /** The default transfer information used if a pipe definition has not been registered. Note that this is replaced
     * by BuildCraft Transport to config-defined values. */
    public static PowerTransferInfo powerInfoDefault = new PowerTransferInfo(2_000_000L, 31_250L, 31_250L, false);

    public static final Map<PipeDefinition, FluidTransferInfo> fluidTransferData = new IdentityHashMap<>();
    public static final Map<PipeDefinition, PowerTransferInfo> powerTransferData = new IdentityHashMap<>();

    public static FluidTransferInfo getFluidTransferInfo(PipeDefinition def) {
        FluidTransferInfo info = fluidTransferData.get(def);
        if (info == null) {
            return fluidInfoDefault;
        } else {
            return info;
        }
    }

    public static PowerTransferInfo getPowerTransferInfo(PipeDefinition def) {
        PowerTransferInfo info = powerTransferData.get(def);
        if (info == null) {
            return powerInfoDefault;
        } else {
            return info;
        }
    }

    public static class FluidTransferInfo {
        /** Controls the maximum amount of fluid that can be transferred around and out of a pipe per tick. Note that
         * this does not affect the flow rate coming into the pipe. */
        public final int transferPerTick;

        /** Controls how long the pipe should delay incoming fluids by. Minimum value is 1, because of the way that
         * fluids are handled internally. This value is multiplied by the fluids viscosity, and divided by 100 to give
         * the actual delay. */
        public final double transferDelayMultiplier;

        public FluidTransferInfo(int transferPerTick, int transferDelay) {
            this.transferPerTick = transferPerTick;
            if (transferDelay <= 0) {
                transferDelay = 1;
            }
            this.transferDelayMultiplier = transferDelay;
        }
    }

    public static class PowerTransferInfo {
        public final long transferPerTick;
        public final long lossPerTick;
        public final long resistancePerTick;
        public final boolean isReceiver;

        public PowerTransferInfo(long transferPerTick, long lossPerTick, long resistancePerTick, boolean isReceiver) {
            if (transferPerTick < 10) {
                transferPerTick = 10;
            }
            this.transferPerTick = transferPerTick;
            this.lossPerTick = lossPerTick;
            this.resistancePerTick = resistancePerTick;
            this.isReceiver = isReceiver;
        }
    }

    private PipeApi() {}
}
