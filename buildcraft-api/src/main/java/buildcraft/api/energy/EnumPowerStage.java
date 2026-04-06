package buildcraft.api.energy;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

/** Power stage indicator for BuildCraft engines. Duplicated from {@link buildcraft.api.enums.EnumPowerStage}
 * for convenience in the energy package. */
public enum EnumPowerStage implements StringRepresentable {
    BLUE,
    GREEN,
    YELLOW,
    RED,
    OVERHEAT,
    BLACK;

    public static final EnumPowerStage[] VALUES = values();

    private final String modelName = name().toLowerCase(Locale.ROOT);

    public String getModelName() {
        return modelName;
    }

    @Override
    public String getSerializedName() {
        return getModelName();
    }
}
