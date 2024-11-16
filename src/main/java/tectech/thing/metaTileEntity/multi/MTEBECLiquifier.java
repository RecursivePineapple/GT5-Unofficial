package tectech.thing.metaTileEntity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofHint;
import static gregtech.api.casing.Casings.AdvancedFusionCoilII;
import static gregtech.api.casing.Casings.AdvancedMolecularCasing;
import static gregtech.api.casing.Casings.MolecularCasing;
import static gregtech.api.enums.GTValues.V;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTUtility.filterValidMTEs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.MetaTileEntityIDs;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.ResultMissingItem;
import gregtech.api.util.FluidStack2;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.IntFraction;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReason;
import gtPlusPlus.core.material.Material;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryGrid;
import tectech.mechanics.boseEinsteinCondensate.BECInventory;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;
import tectech.thing.CustomItemList;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyMulti;
import tectech.thing.metaTileEntity.multi.base.MTEBECMultiblockBase;
import tectech.thing.metaTileEntity.multi.structures.BECStructureDefinitions;

public class MTEBECLiquifier extends MTEBECMultiblockBase<MTEBECLiquifier> {
    
    private int realEfficiency = 0;
    private boolean running = false;

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
            .addOtherStructurePart(BECHatches.Hatch.getDisplayName(), "The marked locations (1 or 2)", 2)
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

    private int getProcessingTime() {
        return 1 * SECONDS;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);

        if (aTick % 20 == 10) {
            if (!aBaseMetaTileEntity.isActive()) {
                realEfficiency = Math.max(0, realEfficiency - 500);
            }
            mEfficiency = realEfficiency;
        }
    }

    @Override
    public void stopMachine(@Nonnull ShutDownReason reason) {
        super.stopMachine(reason);
        mEfficiency = realEfficiency;
        running = false;
    }

    @Override
    protected @NotNull CheckRecipeResult checkProcessing_EM() {
        mMaxProgresstime = 0;
        useLongPower = true;
        lEUt = 0;

        if (network == null) {
            running = false;
            return CheckRecipeResultRegistry.NO_RECIPE;
        }

        long euPerLitre = 512;

        long euQuota = 0;

        int baseProcessingTime = getProcessingTime();

        for(MTEHatch hatch : filterValidMTEs(getExoticAndNormalEnergyHatchList())) {
            if (hatch instanceof MTEHatchEnergyMulti multi) {
                euQuota += V[multi.mTier] * multi.Amperes * baseProcessingTime;
            }
        }

        IntFraction efficiency = new IntFraction(Math.max(100, realEfficiency), 10_000).reduce();

        euQuota = efficiency.apply(euQuota);

        long startQuota = euQuota;

        Object2LongArrayMap<Fluid> consumedCondensate = new Object2LongArrayMap<>();

        IntFraction suction = new IntFraction(2, 5).reduce();

        outer: for (BECInventory inv : network.getComponents(BECInventory.class)) {
            for (CondensateStack stack : inv.getContents()) {
                long quotaAvailable = euQuota / euPerLitre;

                if (quotaAvailable <= 0) break outer;
                
                long available = suction.applyCeil(stack.amount);

                long toConsume = Math.min(available, quotaAvailable);

                if (toConsume <= 0) continue;

                Fluid fluid = null;

                if (stack.material instanceof Materials gtMat) {
                    if (fluid == null && gtMat.mStandardMoltenFluid != null) fluid = gtMat.mStandardMoltenFluid;
                    if (fluid == null && gtMat.mFluid != null) fluid = gtMat.mFluid;
                }

                if (stack.material instanceof Material gtppMat) {
                    if (fluid == null && gtppMat.getFluid() != null) fluid = gtppMat.getFluid();
                }

                if (fluid == null) continue;

                if (!running) {
                    if (!depleteInput(ItemList.BEC_Liquid_Pattern.get(1))) {
                        return new ResultMissingItem(ItemList.BEC_Liquid_Pattern.get(1));
                    }
                    running = true;
                }

                euQuota -= toConsume * euPerLitre;
                inv.removeCondensate(Arrays.asList(new CondensateStack(stack.material, toConsume)));
                consumedCondensate.mergeLong(fluid, toConsume, Long::sum);
            }
        }

        if (consumedCondensate.isEmpty()) {
            running = false;
            return CheckRecipeResultRegistry.NO_RECIPE;
        }

        List<FluidStack2> outputs = new ArrayList<>();

        for (var e : consumedCondensate.object2LongEntrySet()) {
            long amount = e.getLongValue();

            while (amount > 0) {
                int consumed = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
                amount -= consumed;
                outputs.add(new FluidStack2(e.getKey(), consumed));
            }
        }

        mOutputFluids = outputs.toArray(new FluidStack2[outputs.size()]);

        mMaxProgresstime = baseProcessingTime;
        lEUt = -(startQuota - euQuota) / baseProcessingTime;
        realEfficiency = Math.min(10_000, realEfficiency + 500);
        mEfficiency = realEfficiency;

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
