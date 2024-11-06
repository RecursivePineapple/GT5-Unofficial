package gregtech.api.casing;

import java.util.function.Supplier;

import gtPlusPlus.core.block.ModBlocks;
import net.minecraft.block.Block;
import tectech.thing.casing.BlockGTCasingsTT;
import tectech.thing.casing.TTCasingsContainer;

public enum Casings implements ICasing {

    FusionMachineCasingMKIV
        (() -> ModBlocks.blockCasings6Misc, 0, 116),
    AdvancedFusionCoilII
        (() -> ModBlocks.blockCasings6Misc, 1, 116),

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
        return textureOffset + meta;
    }
}
