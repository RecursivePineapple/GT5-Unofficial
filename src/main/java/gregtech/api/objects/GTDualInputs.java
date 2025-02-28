package gregtech.api.objects;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Represents the items stored in an AE pattern.
 * Used to pre-determine the patterns that a CRIB pattern slot can craft.
 */
public class GTDualInputs {

    public ItemStack[] inputItems;
    public FluidStack[] inputFluid;
}
