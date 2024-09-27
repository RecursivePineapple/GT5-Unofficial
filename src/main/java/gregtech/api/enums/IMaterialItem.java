package gregtech.api.enums;

import net.minecraft.item.ItemStack;

public interface IMaterialItem {
    
    public IMaterial getMaterial(ItemStack stack);
    
    public OrePrefixes getPrefix(ItemStack stack);

}
