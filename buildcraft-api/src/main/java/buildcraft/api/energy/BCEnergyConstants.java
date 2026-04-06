package buildcraft.api.energy;

/** Constants for converting between BuildCraft's legacy MJ energy system and the
 * Team Reborn Energy system used in Fabric.
 *
 * <p>1 MJ = 250 Energy units (E).
 * <ul>
 *   <li>Stone engine: 1 MJ/t = 250 E/t</li>
 *   <li>Iron engine max: 60 MJ/t = 15,000 E/t</li>
 * </ul>
 */
public final class BCEnergyConstants {
    /** Conversion factor: 1 MJ = 250 Energy units. */
    public static final long MJ_TO_ENERGY = 250L;

    /** Alias for readability: the energy equivalent of 1 MJ. */
    public static final long ONE_MJ = 250L;

    /** The energy equivalent of 1 micro-MJ (the base unit in the old MJ API was micro-MJ). */
    public static final long ONE_MICRO_MJ = 1L;

    /** 1 full MJ expressed in micro-MJ (for legacy compatibility calculations). */
    public static final long MJ = 1_000_000L;

    private BCEnergyConstants() {}

    /** Convert a micro-MJ value to Team Reborn Energy units.
     * @param microMj the energy amount in micro-MJ
     * @return the equivalent in TR Energy units */
    public static long microMjToEnergy(long microMj) {
        return microMj * MJ_TO_ENERGY / MJ;
    }

    /** Convert a TR Energy amount back to micro-MJ.
     * @param energy the energy amount in TR Energy units
     * @return the equivalent in micro-MJ */
    public static long energyToMicroMj(long energy) {
        return energy * MJ / MJ_TO_ENERGY;
    }
}
