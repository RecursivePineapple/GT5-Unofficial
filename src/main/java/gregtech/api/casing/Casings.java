package gregtech.api.casing;

import java.util.function.Supplier;

import net.minecraft.block.Block;

import gregtech.api.GregTechAPI;
import gtPlusPlus.core.block.ModBlocks;
import tectech.thing.block.BlockQuantumGlass;
import tectech.thing.casing.BlockGTCasingsTT;
import tectech.thing.casing.TTCasingsContainer;

public enum Casings implements ICasing {

    // spotless:off
    // I know these look weird, but I think it makes it easier to read

    ThermalProcessingCasing
        (() -> ModBlocks.blockCasings2Misc, 0, 80),
    HastelloyNSealantBlock
        (() -> ModBlocks.blockCasings2Misc, 1, 80),
    HastelloyXStructuralBlock
        (() -> ModBlocks.blockCasings2Misc, 2, 80),
    IncoloyDSFluidContainmentBlock
        (() -> ModBlocks.blockCasings2Misc, 3, 80),
    // for some reason, a couple of these casings don't register their textures
    // instead, block casings 5 uses these indices
    // why? idk, blame alk or something
    WashPlantCasing
        (() -> ModBlocks.blockCasings2Misc, 4, -1),
    IndustrialSieveCasing
        (() -> ModBlocks.blockCasings2Misc, 5, 80),
    LargeSieveGrate
        (() -> ModBlocks.blockCasings2Misc, 6, 80),
    VanadiumRedoxPowerCellEV
        (() -> ModBlocks.blockCasings2Misc, 7, 80),
    SubStationExternalCasing
        (() -> ModBlocks.blockCasings2Misc, 8, 80),
    CyclotronCoil
        (() -> ModBlocks.blockCasings2Misc, 9, 80),
    CyclotronOuterCasing
        (() -> ModBlocks.blockCasings2Misc, 10, -1),
    ThermalContainmentCasing
        (() -> ModBlocks.blockCasings2Misc, 11, -1),
    BulkProductionFrame
        (() -> ModBlocks.blockCasings2Misc, 12, -1),
    CuttingFactoryFrame
        (() -> ModBlocks.blockCasings2Misc, 13, 80),
    SterileFarmCasing
        (() -> ModBlocks.blockCasings2Misc, 15, 80),

    TinItemPipeCasing
        (() -> GregTechAPI.sBlockCasings11, 0, 2112),
    BrassItemPipeCasing
        (() -> GregTechAPI.sBlockCasings11, 1, 2112),
    ElectrumItemPipeCasing
        (() -> GregTechAPI.sBlockCasings11, 2, 2112),
    PlatinumItemPipeCasing
        (() -> GregTechAPI.sBlockCasings11, 3, 2112),
    OsmiumItemPipeCasing
        (() -> GregTechAPI.sBlockCasings11, 4, 2112),
    QuantiumItemPipeCasing
        (() -> GregTechAPI.sBlockCasings11, 5, 2112),
    FluxedElectrumItemPipeCasing
        (() -> GregTechAPI.sBlockCasings11, 6, 2112),
    BlackPlutoniumItemPipeCasing
        (() -> GregTechAPI.sBlockCasings11, 7, 2112),
    SuperconductivePlasmaEnergyConduit
        (() -> GregTechAPI.sBlockCasings11, 8, 2112),
    ElectromagneticallyIsolatedCasing
        (() -> GregTechAPI.sBlockCasings11, 9, 2112),
    FineStructureConstantManipulator
        (() -> GregTechAPI.sBlockCasings11, 10, 2112),

    ChemicalGradeGlass
        (() -> GregTechAPI.sBlockGlass1, 0, 2048),
    ElectronPermeableNeutroniumCoatedGlass
        (() -> GregTechAPI.sBlockGlass1, 1, 2048),
    OmniPurposeInfinityFusedGlass
        (() -> GregTechAPI.sBlockGlass1, 2, 2048),
    NonPhotonicMatterExclusionGlass
        (() -> GregTechAPI.sBlockGlass1, 3, 2048),
    HawkingRadiationRealignmentFocus
        (() -> GregTechAPI.sBlockGlass1, 4, 2048),
    ElectromagneticWaveguide
        (() -> GregTechAPI.sBlockGlass1, 5, 2048),

    FusionMachineCasingMKIV
        (() -> ModBlocks.blockCasings6Misc, 0, 116),
    AdvancedFusionCoilII
        (() -> ModBlocks.blockCasings6Misc, 1, 116),

    QuantumGlass
        (() -> BlockQuantumGlass.INSTANCE, 0, -1),

    HighPowerCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 0, BlockGTCasingsTT.texturePage),
    ComputerCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 1, BlockGTCasingsTT.texturePage),
    ComputerHeatVent
        (() -> TTCasingsContainer.sBlockCasingsTT, 2, BlockGTCasingsTT.texturePage),
    AdvancedComputerCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 3, BlockGTCasingsTT.texturePage),
    MolecularCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 4, BlockGTCasingsTT.texturePage),
    AdvancedMolecularCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 5, BlockGTCasingsTT.texturePage),
    ContainmentFieldGenerator
        (() -> TTCasingsContainer.sBlockCasingsTT, 6, BlockGTCasingsTT.texturePage),
    MolecularCoil
        (() -> TTCasingsContainer.sBlockCasingsTT, 7, BlockGTCasingsTT.texturePage),
    HollowCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 8, BlockGTCasingsTT.texturePage),
    SpacetimeAlteringCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 9, BlockGTCasingsTT.texturePage),
    TeleportationCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 10, BlockGTCasingsTT.texturePage),
    DimensionalBridgeGenerator
        (() -> TTCasingsContainer.sBlockCasingsTT, 11, BlockGTCasingsTT.texturePage),
    UltimateMolecularCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 12, BlockGTCasingsTT.texturePage),
    UltimateAdvancedMolecularCasing
        (() -> TTCasingsContainer.sBlockCasingsTT, 13, BlockGTCasingsTT.texturePage),
    UltimateContainmentFieldGenerator
        (() -> TTCasingsContainer.sBlockCasingsTT, 14, BlockGTCasingsTT.texturePage),
    ;
    // spotless:on

    public final Supplier<Block> blockGetter;
    public final int meta;
    public final int textureOffset;

    private Casings(Supplier<Block> blockGetter, int meta, int textureOffset) {
        this.blockGetter = blockGetter;
        this.meta = meta;
        this.textureOffset = textureOffset;
    }

    @Override
    public Block getBlock() {
        return blockGetter.get();
    }

    @Override
    public int getMeta() {
        return meta;
    }

    @Override
    public int getTextureId() {
        if (textureOffset == -1) {
            throw new UnsupportedOperationException(
                "Casing " + name() + " does not have a casing texture; The result of getTextureId() is undefined.");
        }

        return textureOffset + meta;
    }
}
