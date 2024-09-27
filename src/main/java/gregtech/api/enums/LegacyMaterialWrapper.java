package gregtech.api.enums;

import org.jetbrains.annotations.Nullable;

import gregtech.api.util.GTOreDictUnificator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class LegacyMaterialWrapper implements IMaterial {
    
    public final Materials material;

    public LegacyMaterialWrapper(Materials material) {
        this.material = material;
    }

    @Override
    public int getID() {
        return material.mMetaItemSubID;
    }

    @Override
    public String getName() {
        return material.mName;
    }

    @Override
    public String getDefaultLocalName() {
        return material.mDefaultLocalName;
    }

    @Override
    public short[] getRGBA() {
        return material.mRGBa;
    }

    @Override
    public ItemStack getItem(OrePrefixes prefix, int amount) {
        return GTOreDictUnificator.get(prefix, material, amount);
    }

    @Override
    public FluidStack getFluid(FluidType fluidType, int amount) {
        return switch (fluidType) {
            case Fluid -> material.mFluid == null ? null : new FluidStack(material.mFluid, amount);
            case Gas -> material.mGas == null ? null : new FluidStack(material.mGas, amount);
            case Molten -> material.mStandardMoltenFluid == null ? null : new FluidStack(material.mStandardMoltenFluid, amount);
            case Plasma -> material.mPlasma == null ? null : new FluidStack(material.mPlasma, amount);
        };
    }

    @Override
    public @Nullable Materials getLegacyMaterial() {
        return material;
    }
}
