package tectech.thing.metaTileEntity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.enums.GTValues.L;
import static gregtech.api.enums.GTValues.M;
import static gregtech.api.enums.GTValues.V;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.Textures.BlockIcons.getCasingTextureForId;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTUtility.filterValidMTEs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.LongConsumer;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SoundResource;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEHatchMultiInput;
import gregtech.api.objects.ItemData;
import gregtech.api.objects.MaterialStack;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility.ItemId;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.IGTHatchAdder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import tectech.mechanics.boseEinsteinCondensate.BECInventory;
import tectech.thing.block.BlockQuantumGlass;
import tectech.thing.casing.BlockGTCasingsTT;
import tectech.thing.casing.TTCasingsContainer;
import tectech.thing.metaTileEntity.hatch.MTEHatchBECOutput;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyMulti;
import tectech.thing.metaTileEntity.multi.base.INameFunction;
import tectech.thing.metaTileEntity.multi.base.IStatusFunction;
import tectech.thing.metaTileEntity.multi.base.LedStatus;
import tectech.thing.metaTileEntity.multi.base.Parameters;
import tectech.thing.metaTileEntity.multi.base.TTMultiblockBase;
import tectech.thing.metaTileEntity.multi.structures.BECGeneratorStructureDef;

public class MTEBECGenerator extends TTMultiblockBase implements ISurvivalConstructable {
    
    private static final String STRUCTURE_PIECE_MAIN = "main";

    private static final int EU_COST_PER_UNIT = 524_288;

    private static final int MOL_CASING_TEX_OFFSET = BlockGTCasingsTT.textureOffset + 4;

    private final List<BECInventory> mBECOutputs = new ArrayList<>();
    private final HashMap<ItemId, Optional<MaterialInfo>> itemMaterialCache = new HashMap<>();

    private MaterialStack[] mOutputMaterials;

    public MTEBECGenerator(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEBECGenerator(String aName) {
        super(aName);
    }

    //#region Structure

    // spotless:off
    private static final IStructureDefinition<MTEBECGenerator> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEBECGenerator>builder()
        .addShape(STRUCTURE_PIECE_MAIN, BECGeneratorStructureDef.BEC_GENERATOR)
        .addElement('A', lazy(() -> ofBlock(TTCasingsContainer.sBlockCasingsTT, 4)))
        .addElement('B', lazy(() -> ofBlock(TTCasingsContainer.sBlockCasingsTT, 5)))
        .addElement('C', lazy(() -> ofBlock(ModBlocks.blockCasings6Misc, 1)))
        .addElement('D', lazy(() -> ofBlock(BlockQuantumGlass.INSTANCE, 0)))
        .addElement('O', lazy(() -> 
            HatchElementBuilder.<MTEBECGenerator>builder()
                .anyOf(BECHatches.Output)
                .casingIndex(MOL_CASING_TEX_OFFSET)
                .dot(2)
                .build()
        ))
        .addElement('1', lazy(() -> 
            HatchElementBuilder.<MTEBECGenerator>builder()
                .anyOf(InputBus, InputHatch, Maintenance, Energy, ExoticEnergy)
                .casingIndex(MOL_CASING_TEX_OFFSET)
                .dot(1)
                .buildAndChain(ofBlock(TTCasingsContainer.sBlockCasingsTT, 4))
        ))
        .build();
    // spotless:on

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public IStructureDefinition<? extends TTMultiblockBase> getStructure_EM() {
        return STRUCTURE_DEFINITION;
    }

    private static enum BECHatches implements IHatchElement<MTEBECGenerator> {

        Output(MTEHatchBECOutput.class) {
            @Override
            public long count(MTEBECGenerator t) {
                return t.mBECOutputs.size();
            }
        };

        private final List<? extends Class<? extends IMetaTileEntity>> mteClasses;

        @SafeVarargs
        BECHatches(Class<? extends IMetaTileEntity>... mteClasses) {
            this.mteClasses = Collections.unmodifiableList(Arrays.asList(mteClasses));
        }

        @Override
        public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
            return mteClasses;
        }

        @Override
        public IGTHatchAdder<? super MTEBECGenerator> adder() {
            return (self, igtme, id) -> {
                IMetaTileEntity imte = igtme.getMetaTileEntity();

                if (imte instanceof MTEHatch hatch && imte instanceof BECInventory becInv) {
                    hatch.updateTexture(MOL_CASING_TEX_OFFSET);
                    hatch.updateCraftingIcon(self.getMachineCraftingIcon());
                    self.mBECOutputs.add(becInv);
                    return true;
                } else {
                    return false;
                }
            };
        }
    }

    @Override
    protected void clearHatches_EM() {
        super.clearHatches_EM();

        mBECOutputs.clear();
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        structureBuild_EM(STRUCTURE_PIECE_MAIN, 9, 9, 0, stackSize, hintsOnly);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivialBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 9, 9, 0, elementBudget, env, false, true);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return structureCheck_EM(STRUCTURE_PIECE_MAIN, 9, 9, 0);
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    //#endregion

    //#region Misc TE Code

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEBECGenerator(mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {
        if (side == facing) {
            if (active) {
                return new ITexture[] { getCasingTextureForId(MOL_CASING_TEX_OFFSET), TextureFactory.builder()
                    .addIcon(TexturesGtBlock.Overlay_Machine_Controller_Advanced_Active)
                    .extFacing()
                    .build() };
            } else {
                return new ITexture[] { getCasingTextureForId(MOL_CASING_TEX_OFFSET), TextureFactory.builder()
                    .addIcon(TexturesGtBlock.Overlay_Machine_Controller_Advanced)
                    .extFacing()
                    .build() };
            }
        }
        return new ITexture[] { getCasingTextureForId(MOL_CASING_TEX_OFFSET) };
    }

    @Override
    protected SoundResource getProcessStartSound() {
        return SoundResource.GT_MACHINES_FUSION_LOOP;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();

        // spotless:off
        tt.addMachineType("Bose-Einstein Condensate Generator")
            .addInfo("Does stuff with atoms")
            .toolTipFinisher("TecTech");
        // spotless:on

        return tt;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10_000;
    }

    private static final INameFunction<MTEBECGenerator> BLOCKING_MODE_NAME = (base,
        p) -> StatCollector.translateToLocal("gui.tooltips.appliedenergistics2.InterfaceBlockingMode"); // Blocking Mode

    private static final IStatusFunction<MTEBECGenerator> BLOCKING_MODE_STATUS = (base, p) -> LedStatus
        .fromLimitsInclusiveOuterBoundary(p.get(), 0, 0, 1, 1);

    protected Parameters.Group.ParameterIn blockingMode;

    @Override
    protected void parametersInstantiation_EM() {
        Parameters.Group hatch_0 = parametrization.getGroup(0);
        blockingMode = hatch_0.makeInParameter(0, 0, BLOCKING_MODE_NAME, BLOCKING_MODE_STATUS);
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

        int lines = 0;
        int MAX_LINES = 5;
        
        if (mOutputMaterials != null) {
            for (var mat : mOutputMaterials) {
                if (mat == null) continue;
                if (lines >= MAX_LINES) {
                    ret.append("...");
                    return ret.toString();
                }
                lines++;
                ret.append(EnumChatFormatting.AQUA)
                    .append(mat.mMaterial.mLocalizedName)
                    .append(" Condensate")
                    .append(EnumChatFormatting.WHITE)
                    .append(" x ")
                    .append(EnumChatFormatting.GOLD);
                numberFormat.format(mat.mAmount * L / M, ret);
                ret.append(" L")
                    .append(EnumChatFormatting.WHITE);
                double processPerTick = (double) mat.mAmount * L / M / mMaxProgresstime * 20;
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

        if (mOutputMaterials != null) {
            aNBT.setInteger("mOutMatCount", mOutputMaterials.length);

            for (int i = 0; i < mOutputMaterials.length; i++) {
                aNBT.setString("mOutMatName." + i, mOutputMaterials[i].mMaterial.mName);
                aNBT.setLong("mOutMatAmount." + i, mOutputMaterials[i].mAmount);
            }
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);

        int count = aNBT.getInteger("mOutMatCount");

        if (count > 0) {
            List<MaterialStack> outputMats = new ArrayList<>();

            for(int i = 0; i < count; i++) {
                Materials material = Materials.get(aNBT.getString("mOutMatName." + i));
                long amount = aNBT.getLong("mOutMatAmount." + i);

                if (material != null && material != Materials._NULL && amount > 0) {
                    outputMats.add(new MaterialStack(material, amount));
                }
            }

            mOutputMaterials = outputMats.toArray(new MaterialStack[0]);
        } else {
            mOutputMaterials = null;
        }
    }

    //#endregion

    @SubscribeEvent
    public void onLoadComplete(FMLLoadCompleteEvent event) {

    }

    private static class MaterialInfo {
        public final MaterialStack stack;
        public final int voltage;

        public MaterialInfo(MaterialStack stack, int voltage) {
            this.stack = stack;
            this.voltage = voltage;
        }
    }

    private Optional<MaterialInfo> getMaterialForStack(ItemStack itemStack) {
        return itemMaterialCache.computeIfAbsent(ItemId.create(itemStack), i -> {

            GTRecipe recipe = RecipeMaps.fluidExtractionRecipes.findRecipeQuery()
                .items(itemStack)
                .voltage(EU_COST_PER_UNIT)
                .find();

            if (recipe != null && recipe.mOutputs.length == 0) {
                Materials material = Materials.FLUID_MAP.get(recipe.mFluidOutputs[0].getFluid());

                if (material != null) {
                    return Optional.of(new MaterialInfo(new MaterialStack(material, recipe.mFluidOutputs[0].amount * M / L), EU_COST_PER_UNIT));
                }
            }

            recipe = RecipeMaps.arcFurnaceRecipes.findRecipeQuery()
                .fluids(new FluidStack(Materials.Oxygen.mFluid, Integer.MAX_VALUE))
                .items(itemStack)
                .voltage(EU_COST_PER_UNIT)
                .find();

            if (recipe != null && recipe.mOutputs.length == 1) {
                ItemData data = GTOreDictUnificator.getItemData(recipe.mOutputs[0]);

                if (data != null) {
                    return Optional.of(new MaterialInfo(data.mMaterial, EU_COST_PER_UNIT));
                }
            }

            return Optional.empty();
        });
    }

    private boolean hasOutputSpace() {
        return blockingMode.get() == 0 || mBECOutputs.stream().anyMatch(o -> o.isEmpty());
    }

    @Override
    public void outputAfterRecipe_EM() {
        if (mOutputMaterials != null) {
            if (!mBECOutputs.isEmpty()) {
                mBECOutputs.get(0).addCondensate(mOutputMaterials);
            }

            mOutputMaterials = null;
        }
    }

    @Override
    protected void startRecipeProcessing() {
        super.startRecipeProcessing();

        itemMaterialCache.clear();
    }

    private static final int PROCESS_TIME = 1 * SECONDS;

    @Override
    protected @NotNull CheckRecipeResult checkProcessing_EM() {
        if (!hasOutputSpace()) {
            return CheckRecipeResultRegistry.ITEM_OUTPUT_FULL;
        }

        long euQuota = 0;

        for(MTEHatch hatch : filterValidMTEs(getExoticAndNormalEnergyHatchList())) {
            if (hatch instanceof MTEHatchEnergyMulti multi) {
                euQuota += V[multi.mTier] * multi.Amperes * PROCESS_TIME;
            }
        }

        long startingQuota = euQuota;

        HashMap<Materials, Long> outputMaterials = new HashMap<>();

        for (MTEHatchInputBus inputBus : filterValidMTEs(mInputBusses)) {
            for (int i = inputBus.getSizeInventory() - 1; i >= 0; i--) {
                ItemStack slot = inputBus.getStackInSlot(i);

                if (slot != null) {
                    Optional<MaterialInfo> matInfo = getMaterialForStack(slot);
                    
                    if (matInfo.isPresent()) {
                        long quotaRemaining = euQuota / EU_COST_PER_UNIT;
                        long toRemove = Math.min(Math.min(quotaRemaining, slot.stackSize), Integer.MAX_VALUE);
                        
                        if (toRemove == 0) {
                            continue;
                        }

                        inputBus.decrStackSize(i, (int) toRemove);

                        euQuota -= EU_COST_PER_UNIT * toRemove;

                        MaterialStack stack = matInfo.get().stack;
                        
                        long toAdd = toRemove * stack.mAmount;
                        outputMaterials.merge(stack.mMaterial, toAdd, (a, b) -> a + b);
                    }
                }
            }
        }

        for (MTEHatchInput inputHatch : filterValidMTEs(mInputHatches)) {
            if (inputHatch instanceof MTEHatchMultiInput multiInputHatch) {
                for (FluidStack tFluid : multiInputHatch.getStoredFluid()) {
                    if (tFluid != null && tFluid.amount > 0) {
                        euQuota -= tryDrainFluid(outputMaterials, euQuota, inputHatch, tFluid);
                    }
                }
            } else if (inputHatch instanceof MTEHatchInputME meHatch) {
                for (FluidStack fluidStack : meHatch.getStoredFluids()) {
                    if (fluidStack != null && fluidStack.amount > 0) {
                        euQuota -= tryDrainFluid(outputMaterials, euQuota, inputHatch, fluidStack);
                    }
                }
            } else {
                if (inputHatch.getFillableStack() != null && inputHatch.getFillableStack().amount > 0) {
                    euQuota -= tryDrainFluid(outputMaterials, euQuota, inputHatch, inputHatch.getFillableStack());
                }
            }
        }

        if (outputMaterials.isEmpty()) {
            mMaxProgresstime = 0;
            mOutputMaterials = null;

            return CheckRecipeResultRegistry.NO_RECIPE;
        } else {
            mOutputMaterials = outputMaterials.isEmpty() ? null : outputMaterials.entrySet().stream().map(e -> new MaterialStack(e.getKey(), e.getValue())).toArray(MaterialStack[]::new);
            mMaxProgresstime = PROCESS_TIME;
            mEfficiency = 10_000;

            useLongPower = true;
            lEUt = -(startingQuota - euQuota) / mMaxProgresstime;

            return CheckRecipeResultRegistry.SUCCESSFUL;
        }
    }

    private long tryDrainFluid(HashMap<Materials, Long> outputMaterials, long euQuota, MTEHatchInput inputHatch, FluidStack fluidStack) {
        Materials material = Materials.FLUID_MAP.get(fluidStack.getFluid());

        if (material != null) {
            long quotaRemaining = euQuota / EU_COST_PER_UNIT;
            long toRemove = Math.min(quotaRemaining, Math.max(1, fluidStack.amount / L));

            if (toRemove > 0) {
                long toDrain = Math.min(Math.min(fluidStack.amount, toRemove * L), Integer.MAX_VALUE);

                FluidStack drained = inputHatch.drain(ForgeDirection.UNKNOWN, new FluidStack(fluidStack, (int) toDrain), true);

                int removedUnits = Math.max(1, (int) (drained.amount / L));

                outputMaterials.merge(material, (long) drained.amount, (a, b) -> a + b);

                return removedUnits * EU_COST_PER_UNIT;
            }
        }

        return 0;
    }
}
