package gregtech.api.enums;

import static gregtech.api.enums.GTValues.L;
import static gregtech.api.enums.GTValues.M;

public enum FluidType {
    Fluid(1000),
    Molten(144),
    Gas(1000),
    Plasma(1000);

    public static final FluidType[] VALUES = FluidType.values();

    public final long mLitres, mMaterialAmount;

    private FluidType(long litres) {
        mLitres = litres;
        mMaterialAmount = litres * M / L;
    }
}
