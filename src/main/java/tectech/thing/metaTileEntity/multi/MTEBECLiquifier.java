package tectech.thing.metaTileEntity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofHint;
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
import tectech.thing.CustomItemList;
import tectech.thing.metaTileEntity.multi.base.MTEBECMultiblockBase;
import tectech.thing.metaTileEntity.multi.structures.BECStructureDefinitions;

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
        return BECStructureDefinitions.BEC_LIQUIFIER;
    }

    @Override
    public IStructureDefinition<MTEBECLiquifier> compile(String[][] definition) {
        structure.addCasing('A', MolecularCasing);
        structure.addCasing('B', AdvancedMolecularCasing);
        structure.addCasing('C', AdvancedFusionCoilII);
        structure.addCasing('D', AdvancedMolecularCasing);

        return StructureDefinition.<MTEBECLiquifier>builder()
            .addShape(STRUCTURE_PIECE_MAIN, definition)
            .addElement('A', MolecularCasing.asElement())
            .addElement('B', AdvancedMolecularCasing.asElement())
            .addElement('C', AdvancedFusionCoilII.asElement())
            .addElement('D', ofChain(
                    ofHint(3),
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
        tt.addMachineType("Bose-Einstein Condensate Liquification Array")
            .addInfo("Liquifies fancy atoms")
            .beginStructureBlock(structure.size.x, structure.size.y, structure.size.z, false)
            .addController("Front Center (bottom layer)")
            .pipe(tt2 -> {
                structure.addCasingInfoRange(tt2, MolecularCasing);
                structure.addCasingInfoExact(tt2, AdvancedMolecularCasing);
                structure.addCasingInfoExact(tt2, AdvancedFusionCoilII);
            })
            .addInputBus("Any " + MolecularCasing.getLocalizedName() + " in the outer ring", 1)
            .addOutputHatch("Any " + MolecularCasing.getLocalizedName() + " in the outer ring", 1)
            .addEnergyHatch("Any " + MolecularCasing.getLocalizedName() + " in the outer ring", 1)
            .addOtherStructurePart(CustomItemList.becConnectorHatch.get(1).getDisplayName(), "The marked locations (1 or 2)", 2)
            .addOtherStructurePart(CustomItemList.BECpipeBlock.get(1).getDisplayName(), "The marked locations (0-2, optional)", 3)
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
