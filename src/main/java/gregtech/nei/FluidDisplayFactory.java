package gregtech.nei;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.util.GTUtility;

public interface FluidDisplayFactory {

    ItemStack getFluidDisplay(FluidStack fluid, FluidDisplayStackMode stackMode);

    @Nullable
    FluidStack getFluidFromStack(ItemStack stack);

    /**
     * The default standard GT fluid display (uses {@link gregtech.common.items.ItemFluidDisplay}).
     */
    FluidDisplayFactory STANDARD_FLUID_DISPLAY = new FluidDisplayFactory() {

        @Override
        public ItemStack getFluidDisplay(FluidStack fluid, FluidDisplayStackMode stackMode) {
            return GTUtility.getFluidDisplayStack(fluid, stackMode);
        }

        @Override
        public @org.jetbrains.annotations.Nullable FluidStack getFluidFromStack(ItemStack stack) {
            return GTUtility.getFluidFromDisplayStack(stack);
        }
    };
}
