package gregtech.common.tileentities.machines;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.objects.GTDualInputs;

/**
 * Represents a sandboxed pattern inventory in a CRIB.
 */
public interface IDualInputInventory {

    boolean isEmpty();

    /** The list of real items stored in this sandboxed inventory. */
    ItemStack[] getItemInputs();

    /** The list of real fluids stored in this sandboxed inventory. */

    FluidStack[] getFluidInputs();

    /** The list of items & fluids that this pattern will provide. */
    GTDualInputs getPatternInputs();

    default boolean shouldBeCached() {
        return true;
    }
}
