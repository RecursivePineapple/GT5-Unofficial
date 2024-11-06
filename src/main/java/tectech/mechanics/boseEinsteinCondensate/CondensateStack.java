package tectech.mechanics.boseEinsteinCondensate;

import org.jetbrains.annotations.Nullable;

import gregtech.api.enums.Materials;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CondensateStack {
    
    public Materials material;
    /** Amount in litres. */
    public long amount;

    public CondensateStack() {

    }

    public CondensateStack(Materials material, long amount) {
        this.material = material;
        this.amount = amount;
    }

    public @Nullable CondensateStack fromFluid(FluidStack fluid) {
        return null;
    }

    public @Nullable CondensateStack fromStack(ItemStack stack) {
        return null;
    }
}
