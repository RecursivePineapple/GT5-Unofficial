package gregtech.api.enums;

import static gregtech.api.enums.GTValues.L;
import static gregtech.api.enums.GTValues.M;

import bartworks.system.material.Werkstoff;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class MaterialStack2 {
    
    public IMaterial material;
    /** 1 unit = {@link GTValues.M} */
    public long amount;

    public MaterialStack2(IMaterial material, long amount) {
        this.material = material;
        this.amount = amount;
    }

    public MaterialStack2(Materials legacy, long amount) {
        material = new LegacyMaterialWrapper(legacy);
        this.amount = amount;
    }

    public MaterialStack2(Werkstoff legacy, long amount) {
        material = new LegacyWerkstoffWrapper(legacy);
        this.amount = amount;
    }

    public static MaterialStack2 litres(IMaterial material, int litres) {
        return new MaterialStack2(material, litres * M / L);
    }

    public static MaterialStack2 litres(Materials legacy, int litres) {
        return new MaterialStack2(legacy, litres * M / L);
    }

    public static MaterialStack2 litres(Werkstoff legacy, int litres) {
        return new MaterialStack2(legacy, litres * M / L);
    }

    public static MaterialStack2 items(IMaterial material, int itemCount) {
        return new MaterialStack2(material, itemCount * M);
    }

    public static MaterialStack2 items(Materials legacy, int itemCount) {
        return new MaterialStack2(legacy, itemCount * M);
    }

    public static MaterialStack2 items(Werkstoff legacy, int itemCount) {
        return new MaterialStack2(legacy, itemCount * M);
    }

    public MaterialStack2 copy(long aAmount) {
        return new MaterialStack2(material, aAmount);
    }

    public MaterialStack2 copy() {
        return new MaterialStack2(material, amount);
    }

    @Override
    public String toString() {
        return String.format("MaterialStack2{material=%s, amount=%d M (%d items, %d L)}", material, amount, getItemCount(OrePrefixes.dust), getFluidLitres());
    }

    public ItemStack toItemStack(OrePrefixes prefix) {
        return material.getItem(prefix, getItemCount(prefix));
    }

    public FluidStack toFluidStack(FluidType fluid) {
        return material.getFluid(fluid, getFluidLitres());
    }

    public int getItemCount(OrePrefixes prefix) {
        return (int) (amount / prefix.mMaterialAmount);
    }

    public int getFluidLitres() {
        return (int) (amount * L / M);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((material == null) ? 0 : material.hashCode());
        result = prime * result + (int) (amount ^ (amount >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MaterialStack2 other = (MaterialStack2) obj;
        if (material == null) {
            if (other.material != null)
                return false;
        } else if (!material.equals(other.material))
            return false;
        if (amount != other.amount)
            return false;
        return true;
    }
}
