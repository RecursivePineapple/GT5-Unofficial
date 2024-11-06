package tectech.thing.metaTileEntity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static gregtech.api.casing.Casings.AdvancedFusionCoilII;
import static gregtech.api.casing.Casings.AdvancedMolecularCasing;
import static gregtech.api.casing.Casings.MolecularCasing;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.MetaTileEntityIDs;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.MultiblockTooltipBuilder;
import net.minecraft.nbt.NBTTagCompound;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryGrid;
import tectech.thing.metaTileEntity.multi.base.MTEBECMultiblockBase;
import tectech.thing.metaTileEntity.multi.structures.BECGeneratorStructureDef;

public class MTEBECLiquifier extends MTEBECMultiblockBase<MTEBECLiquifier> {
    
    public MTEBECLiquifier(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEBECLiquifier(MTEBECLiquifier prototype) {
        super(prototype);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEBECLiquifier(this);
    }

    @Override
    public String[][] getDefinition() {
        return BECGeneratorStructureDef.BEC_LIQUIFIER;
    }

    @Override
    public IStructureDefinition<MTEBECLiquifier> compile(String[][] definition) {
        return StructureDefinition.<MTEBECLiquifier>builder()
            .addShape(STRUCTURE_PIECE_MAIN, definition)
            .addElement('A', MolecularCasing.asElement())
            .addElement('B', AdvancedMolecularCasing.asElement())
            .addElement('C', AdvancedFusionCoilII.asElement())
            .addElement('D', ofChain(
                AdvancedMolecularCasing.asElement(),
                lazy(() -> ofBlock(GregTechAPI.sBlockMachines, MetaTileEntityIDs.BoseEinsteinCondensatePipeBlock.ID))))
            .addElement('E', HatchElementBuilder.<MTEBECLiquifier>builder()
                .anyOf(BECHatches.Hatch)
                .casingIndex(AdvancedFusionCoilII.getTextureId())
                .dot(2)
                .buildAndChain(MolecularCasing.asElement()))
            .addElement('1', HatchElementBuilder.<MTEBECLiquifier>builder()
                .anyOf(Energy, ExoticEnergy, OutputHatch, InputBus)
                .casingIndex(MolecularCasing.getTextureId())
                .dot(1)
                .buildAndChain(structure.getCasingAdder('1', MolecularCasing, 16)))
            .build();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();

        // spotless:off
        tt.addMachineType("Bose-Einstein Condensate Storage")
            .addInfo("Liquifies fancy atoms")
            .toolTipFinisher(GTValues.AuthorPineapple);
        // spotless:on

        return tt;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
    }

    @Override
    protected @NotNull CheckRecipeResult checkProcessing_EM() {
        mMaxProgresstime = 20;
        mEfficiency = 10_000;
        
        useLongPower = true;
        lEUt = 0;

        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public void onFirstTick_EM(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick_EM(aBaseMetaTileEntity);

        BECFactoryGrid.INSTANCE.addElement(this);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();

        BECFactoryGrid.INSTANCE.removeElement(this);
    }

}
