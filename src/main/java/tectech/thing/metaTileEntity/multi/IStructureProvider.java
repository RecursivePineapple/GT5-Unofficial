package tectech.thing.metaTileEntity.multi;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;

public interface IStructureProvider<MTE extends MTEEnhancedMultiBlockBase<?> & IStructureProvider<MTE>> {
    public String[][] getDefinition();

    public IStructureDefinition<MTE> compile(String[][] definition);

    public StructureWrapperInstanceInfo<MTE> getWrapperInstanceInfo();
}