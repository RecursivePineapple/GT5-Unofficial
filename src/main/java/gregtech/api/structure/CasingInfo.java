package gregtech.api.structure;

import gregtech.api.casing.ICasing;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;

public class CasingInfo<MTE extends MTEEnhancedMultiBlockBase<?> & IStructureProvider<MTE>> {

    public int definitionCasingCount, maxHatches, dot;
    public ICasing casing;
    public IHatchElement<? super MTE>[] hatches;
}
