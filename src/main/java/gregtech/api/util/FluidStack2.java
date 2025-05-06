package gregtech.api.util;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * A fluid stack with a sane {@link #equals(Object)} implementation.
 */
public class FluidStack2 extends FluidStack {

    public FluidStack2(Fluid fluid, int amount) {
        super(fluid, amount);
    }

    public FluidStack2(Fluid fluid, int amount, NBTTagCompound nbt) {
        super(fluid, amount, nbt);
    }

    public FluidStack2(FluidStack stack, int amount) {
        super(stack, amount);
    }

    public static FluidStack2 loadFluidStackFromNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            return null;
        }

        String fluidName = nbt.getString("FluidName");

        if (fluidName == null || FluidRegistry.getFluid(fluidName) == null) {
            return null;
        }

        FluidStack2 stack = new FluidStack2(FluidRegistry.getFluid(fluidName), nbt.getInteger("Amount"));

        if (nbt.hasKey("Tag")) {
            stack.tag = nbt.getCompoundTag("Tag");
        }

        return stack;
    }

    @Override
    public boolean isFluidEqual(FluidStack other) {
        if (other == null) return false;

        if (getFluid() != other.getFluid()) return false;

        if (amount != other.amount) return false;

        if (!Objects.equals(tag, other.tag)) return false;

        return true;
    }
}
