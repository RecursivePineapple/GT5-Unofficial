package gregtech.api.enums;

import org.jetbrains.annotations.Nullable;

import bartworks.system.material.Werkstoff;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class LegacyWerkstoffWrapper implements IMaterial {

    public final Werkstoff werkstoff;

    public LegacyWerkstoffWrapper(Werkstoff material) {
        this.werkstoff = material;
    }

    @Override
    public int getID() {
        return werkstoff.getmID();
    }

    @Override
    public String getName() {
        return werkstoff.getVarName();
    }

    @Override
    public String getDefaultLocalName() {
        return werkstoff.getDefaultName();
    }

    @Override
    public short[] getRGBA() {
        return werkstoff.getRGBA();
    }

    @Override
    public ItemStack getItem(OrePrefixes prefix, int amount) {
        return werkstoff.get(prefix, amount);
    }

    @Override
    public FluidStack getFluid(FluidType fluidType, int amount) {
        return switch (fluidType) {
            case Fluid -> werkstoff.getFluidOrGas(amount);
            case Gas -> werkstoff.getFluidOrGas(amount);
            case Molten -> werkstoff.getMolten(amount);
            case Plasma -> null;
        };
    }

    @Override
    public @Nullable Materials getLegacyMaterial() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLegacyMaterial'");
    }
    
}
