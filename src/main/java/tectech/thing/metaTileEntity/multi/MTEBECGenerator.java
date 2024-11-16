package tectech.thing.metaTileEntity.multi;

import static gregtech.api.casing.Casings.AdvancedFusionCoilII;
import static gregtech.api.casing.Casings.AdvancedMolecularCasing;
import static gregtech.api.casing.Casings.MolecularCasing;
import static gregtech.api.casing.Casings.QuantumGlass;
import static gregtech.api.enums.GTValues.V;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTUtility.filterValidMTEs;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.util.Vec3Impl;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.SoundResource;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEHatchMultiInput;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeConstants;
import gregtech.api.util.GTUtility;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.client.GTSoundLoop;
import gregtech.client.ISoundLoopAware;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import gregtech.loaders.load.BECRecipeLoader;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryGrid;
import tectech.mechanics.boseEinsteinCondensate.BECInventory;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;
import tectech.recipe.TecTechRecipeMaps;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyMulti;
import tectech.thing.metaTileEntity.multi.base.INameFunction;
import tectech.thing.metaTileEntity.multi.base.IStatusFunction;
import tectech.thing.metaTileEntity.multi.base.LedStatus;
import tectech.thing.metaTileEntity.multi.base.MTEBECMultiblockBase;
import tectech.thing.metaTileEntity.multi.base.Parameters;
import tectech.thing.metaTileEntity.multi.structures.BECStructureDefinitions;

public class MTEBECGenerator extends MTEBECMultiblockBase<MTEBECGenerator> implements ISoundLoopAware {
    
    private List<CondensateStack> mOutputCondensate;

    public MTEBECGenerator(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected MTEBECGenerator(MTEBECGenerator prototype) {
        super(prototype);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEBECGenerator(this);
    }

    @Override
    public String[][] getDefinition() {
        return BECStructureDefinitions.BEC_GENERATOR;
    }

    // spotless:off
    @Override
    public IStructureDefinition<MTEBECGenerator> compile(String[][] definition) {
        structure.addCasing('A', MolecularCasing);
        structure.addCasing('B', AdvancedMolecularCasing);
        structure.addCasing('C', AdvancedFusionCoilII);
        structure.addCasing('D', QuantumGlass);

        return StructureDefinition.<MTEBECGenerator>builder()
            .addShape(STRUCTURE_PIECE_MAIN, definition)
            .addElement('A', MolecularCasing.asElement())
            .addElement('B', AdvancedMolecularCasing.asElement())
            .addElement('C', AdvancedFusionCoilII.asElement())
            .addElement('D', QuantumGlass.asElement())
            .addElement('O', HatchElementBuilder.<MTEBECGenerator>builder()
                    .anyOf(BECHatches.Hatch)
                    .casingIndex(MolecularCasing.getTextureId())
                    .dot(2)
                    .build())
            .addElement('1', HatchElementBuilder.<MTEBECGenerator>builder()
                    .anyOf(InputBus, InputHatch, Energy, ExoticEnergy)
                    .casingIndex(MolecularCasing.getTextureId())
                    .dot(1)
                    .buildAndChain(structure.getCasingAdder('1', MolecularCasing, 16)))
            .build();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();

        // spotless:off
        tt.addMachineType("Bose-Einstein Condensate Generator")
            .addInfo("Makes fancy atoms")
            .beginStructureBlock(structure.size.x, structure.size.y, structure.size.z, false)
            .addController("Front Center")
            .pipe(tt2 -> {
                structure.addCasingInfoRange(tt2, MolecularCasing);
                structure.addCasingInfoExact(tt2, AdvancedMolecularCasing);
                structure.addCasingInfoExact(tt2, AdvancedFusionCoilII);
                structure.addCasingInfoExact(tt2, QuantumGlass);
            })
            .addInputBus("Any " + MolecularCasing.getLocalizedName() + " in the first slice", 1)
            .addInputHatch("Any " + MolecularCasing.getLocalizedName() + " in the first slice", 1)
            .addEnergyHatch("Any " + MolecularCasing.getLocalizedName() + " in the first slice", 1)
            .addOtherStructurePart(BECHatches.Hatch.getDisplayName(), "The centre casing in the last slice", 2)
            .toolTipFinisher(GTValues.AuthorPineapple);
        // spotless:on

        return tt;
    }

    protected Parameters.Group.ParameterIn blockingMode;

    @Override
    protected void parametersInstantiation_EM() {
        INameFunction<MTEBECGenerator> blockingModeName = (base, p) -> StatCollector.translateToLocal("gui.tooltips.appliedenergistics2.InterfaceBlockingMode");
        IStatusFunction<MTEBECGenerator> blockingModeStatus = (base, p) -> LedStatus.fromLimitsInclusiveOuterBoundary(p.get(), 0, 0, 1, 1);
    
        Parameters.Group hatch_0 = parametrization.getGroup(0);
        blockingMode = hatch_0.makeInParameter(0, 0, blockingModeName, blockingModeStatus);
    }

    @Override
    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);

        screenElements.widget(
            TextWidget.dynamicString(this::generateCondensateOutputText)
                .setTextAlignment(Alignment.CenterLeft));
    }

    protected String generateCondensateOutputText() {
        StringBuffer ret = new StringBuffer();

        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        if (mOutputCondensate != null) {
            for (var mat : mOutputCondensate) {
                if (mat == null) continue;
                ret.append(EnumChatFormatting.AQUA)
                    .append(mat.getPreview().getDisplayName())
                    .append(EnumChatFormatting.WHITE)
                    .append(" x ")
                    .append(EnumChatFormatting.GOLD);
                numberFormat.format(mat.amount, ret);
                ret.append(" L")
                    .append(EnumChatFormatting.WHITE);
                double processPerTick = (double) mat.amount / mMaxProgresstime * 20;
                if (processPerTick > 1) {
                    ret.append(" (");
                    numberFormat.format(Math.round(processPerTick * 10) / 10.0, ret);
                    ret.append("L/s)");
                } else {
                    ret.append(" (");
                    numberFormat.format(Math.round(1 / processPerTick * 10) / 10.0, ret);
                    ret.append("s/L)");
                }
                ret.append('\n');
            }
        }
        return ret.toString();
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);

        if (mOutputCondensate != null) {
            aNBT.setTag("condensate", CondensateStack.save(mOutputCondensate));
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);

        mOutputCondensate = null;

        if (aNBT.getTag("condensate") instanceof NBTTagList list) {
            mOutputCondensate = CondensateStack.load(list);
        }
    }

    //#endregion

    private boolean hasOutputSpace() {
        return true;
        // return blockingMode.get() == 0 || mBECOutput.getNetwork().get;
    }

    @Override
    public void outputAfterRecipe_EM() {
        if (mOutputCondensate != null && !mOutputCondensate.isEmpty()) {
            if (network != null) {
                for (BECInventory inv : network.getComponents(BECInventory.class)) {
                    inv.addCondensate(mOutputCondensate);
                }
            }

            mOutputCondensate = null;
        }
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

    @Override
    @Nonnull
    public Collection<RecipeMap<?>> getAvailableRecipeMaps() {
        return Arrays.asList(TecTechRecipeMaps.condensateCreationFromItemRecipes, TecTechRecipeMaps.condensateCreationFromFluidRecipes);
    }

    @Override
    protected SoundResource getActivitySoundLoop() {
        return SoundResource.GT_MACHINES_BEC_GENERATOR;
    }

    @Override
    public void modifySoundLoop(GTSoundLoop loop) {
        Vec3Impl pos = getExtendedFacing().getWorldOffset(new Vec3Impl(0, 0, 10));

        IGregTechTileEntity igte = getBaseMetaTileEntity();

        loop.setPosition(pos.get0() + igte.getXCoord(), pos.get1() + igte.getYCoord(), pos.get2() + igte.getZCoord());
        loop.setVolume(2);
    }

    @Override
    public void onSoundLoopTicked(GTSoundLoop loop) {
        modifySoundLoop(loop);
    }

    private int getProcessingTime() {
        return 1 * SECONDS;
    }

    @Override
    protected @NotNull CheckRecipeResult checkProcessing_EM() {
        if (!hasOutputSpace()) {
            return CheckRecipeResultRegistry.ITEM_OUTPUT_FULL;
        }

        long euQuota = 0;

        int baseProcessingTime = getProcessingTime();

        for(MTEHatch hatch : filterValidMTEs(getExoticAndNormalEnergyHatchList())) {
            if (hatch instanceof MTEHatchEnergyMulti multi) {
                euQuota += V[multi.mTier] * multi.Amperes * baseProcessingTime;
            }
        }

        long startingQuota = euQuota;

        Object2LongOpenHashMap<Object> outputMaterials = new Object2LongOpenHashMap<>();

        for (MTEHatchInputBus inputBus : filterValidMTEs(mInputBusses)) {
            for (int i = inputBus.getSizeInventory() - 1; i >= 0; i--) {
                ItemStack slot = inputBus.getStackInSlot(i);

                if (slot != null) {
                    GTRecipe recipe = TecTechRecipeMaps.condensateCreationFromItemRecipes.findRecipeQuery()
                        .items(slot)
                        .find();

                    if (recipe != null) {
                        CondensateStack[] output = recipe.getMetadata(GTRecipeConstants.CONDENSATE_OUTPUTS);

                        if (output == null) {
                            continue;
                        }

                        long cost = BECRecipeLoader.getRecipeCost(recipe);

                        long quotaRemaining = euQuota / cost;
                        long toRemove = Math.min(Math.min(quotaRemaining, slot.stackSize), Integer.MAX_VALUE);
                        
                        if (toRemove == 0) {
                            continue;
                        }

                        inputBus.decrStackSize(i, (int) toRemove);

                        euQuota -= cost * toRemove;

                        for (CondensateStack stack : output) {
                            long toAdd = toRemove * stack.amount;
                            outputMaterials.merge(stack.material, toAdd, Long::sum);
                        }
                    }
                }
            }
        }

        for (MTEHatchInput inputHatch : filterValidMTEs(mInputHatches)) {
            if (inputHatch instanceof MTEHatchMultiInput multiInputHatch) {
                for (FluidStack tFluid : multiInputHatch.getStoredFluid()) {
                    if (tFluid != null && tFluid.amount > 0) {
                        euQuota = tryDrainFluid(outputMaterials, euQuota, inputHatch, tFluid);
                    }
                }
            } else if (inputHatch instanceof MTEHatchInputME meHatch) {
                for (FluidStack fluidStack : meHatch.getStoredFluids()) {
                    if (fluidStack != null && fluidStack.amount > 0) {
                        euQuota = tryDrainFluid(outputMaterials, euQuota, inputHatch, fluidStack);
                    }
                }
            } else {
                if (inputHatch.getFillableStack() != null && inputHatch.getFillableStack().amount > 0) {
                    euQuota = tryDrainFluid(outputMaterials, euQuota, inputHatch, inputHatch.getFillableStack());
                }
            }
        }

        if (outputMaterials.isEmpty()) {
            mMaxProgresstime = 0;
            mOutputCondensate = null;

            return CheckRecipeResultRegistry.NO_RECIPE;
        } else {
            mOutputCondensate = outputMaterials.isEmpty() ? null : outputMaterials.object2LongEntrySet().stream().map(e -> new CondensateStack(e.getKey(), e.getLongValue())).collect(Collectors.toList());
            mMaxProgresstime = baseProcessingTime;
            mEfficiency = 10_000;

            useLongPower = true;
            lEUt = -(startingQuota - euQuota) / mMaxProgresstime;

            return CheckRecipeResultRegistry.SUCCESSFUL;
        }
    }

    private long tryDrainFluid(Object2LongOpenHashMap<Object> outputMaterials, long euQuota, MTEHatchInput inputHatch, FluidStack fluidStack) {
        GTRecipe recipe = TecTechRecipeMaps.condensateCreationFromFluidRecipes.findRecipeQuery()
            .fluids(new FluidStack(fluidStack.getFluid(), 1))
            .find();
        
        if (recipe == null) {
            return 0;
        }

        CondensateStack[] output = recipe.getMetadata(GTRecipeConstants.CONDENSATE_OUTPUTS);

        if (output == null) {
            return 0;
        }
        
        long cost = BECRecipeLoader.getRecipeCost(recipe);

        long availableQuota = euQuota * 1000 / cost;
        long toRemove = GTUtility.clamp(fluidStack.amount, 0, availableQuota);

        if (toRemove <= 0) {
            return 0;
        }

        euQuota -= GTUtility.ceilDiv(toRemove * cost, 1000);

        long toDrain = GTUtility.min(fluidStack.amount, toRemove, Integer.MAX_VALUE);

        FluidStack drained = inputHatch.drain(ForgeDirection.UNKNOWN, new FluidStack(fluidStack, (int) toDrain), true);

        for (CondensateStack stack : output) {
            outputMaterials.merge(stack, (long) drained.amount, Long::sum);
        }

        return euQuota;
    }
}
