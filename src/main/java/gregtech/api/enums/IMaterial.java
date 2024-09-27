package gregtech.api.enums;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IMaterial {
    public int getID();

    public String getName();

    public String getDefaultLocalName();
    
    public short[] getRGBA();

    public ItemStack getItem(OrePrefixes prefix, int amount);

    public default ItemStack getItem(OrePrefixes prefix, long amount) {
        return getItem(prefix, (int) amount);
    }

    public FluidStack getFluid(FluidType fluidType, int amount);

    public default FluidStack getFluid(FluidType fluidType, long amount) {
        return getFluid(fluidType, (int) amount);
    }

    public @Nullable Materials getLegacyMaterial();
}
