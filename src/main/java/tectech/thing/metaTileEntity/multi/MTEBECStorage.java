package tectech.thing.metaTileEntity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.Textures.BlockIcons.getCasingTextureForId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.IGTHatchAdder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReason;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryElement;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryGrid;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryNetwork;
import tectech.mechanics.boseEinsteinCondensate.BECInventory;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;
import tectech.thing.block.BlockQuantumGlass;
import tectech.thing.casing.BlockGTCasingsTT;
import tectech.thing.casing.TTCasingsContainer;
import tectech.thing.metaTileEntity.hatch.MTEHatchBEC;
import tectech.thing.metaTileEntity.multi.base.TTMultiblockBase;
import tectech.thing.metaTileEntity.multi.structures.BECGeneratorStructureDef;

public class MTEBECStorage extends TTMultiblockBase implements ISurvivalConstructable, BECFactoryElement, BECInventory {
    
    private static final String STRUCTURE_PIECE_MAIN = "main";

    private static final int MOL_CASING_TEX_OFFSET = BlockGTCasingsTT.textureOffset + 4;

    private final List<BECFactoryElement> mBECHatches = new ArrayList<>();

    private final Object2LongOpenHashMap<Materials> mStoredCondensate = new Object2LongOpenHashMap<>();

    private BECFactoryNetwork network;

    public MTEBECStorage(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEBECStorage(String aName) {
        super(aName);
    }

    //#region Structure

    // spotless:off
    private static final IStructureDefinition<MTEBECStorage> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEBECStorage>builder()
        .addShape(STRUCTURE_PIECE_MAIN, BECGeneratorStructureDef.BEC_CONTAINMENT_FIELD)
        .addElement('A', lazy(() -> ofBlock(TTCasingsContainer.sBlockCasingsTT, 4)))
        .addElement('B', lazy(() -> ofBlock(TTCasingsContainer.sBlockCasingsTT, 5)))
        .addElement('C', lazy(() -> ofBlock(TTCasingsContainer.sBlockCasingsTT, 6)))
        .addElement('D', lazy(() -> ofBlock(BlockQuantumGlass.INSTANCE, 0)))
        .addElement('E', lazy(() -> 
            HatchElementBuilder.<MTEBECStorage>builder()
                .anyOf(BECHatches.Hatch)
                .casingIndex(MOL_CASING_TEX_OFFSET)
                .dot(2)
                .build()
        ))
        .addElement('1', lazy(() -> 
            HatchElementBuilder.<MTEBECStorage>builder()
                .anyOf(Energy, ExoticEnergy)
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

    private static enum BECHatches implements IHatchElement<MTEBECStorage> {

        Hatch(MTEHatchBEC.class) {
            @Override
            public long count(MTEBECStorage t) {
                return t.mBECHatches.size();
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
        public IGTHatchAdder<? super MTEBECStorage> adder() {
            return (self, igtme, id) -> {
                IMetaTileEntity imte = igtme.getMetaTileEntity();

                if (imte instanceof MTEHatchBEC hatch) {
                    hatch.updateTexture(MOL_CASING_TEX_OFFSET);
                    hatch.updateCraftingIcon(self.getMachineCraftingIcon());
                    self.mBECHatches.add(hatch);
                    hatch.setController(self);
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

        mBECHatches.clear();
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        structureBuild_EM(STRUCTURE_PIECE_MAIN, 9, 2, 0, stackSize, hintsOnly);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivialBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 9, 2, 0, elementBudget, env, false, true);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return structureCheck_EM(STRUCTURE_PIECE_MAIN, 9, 2, 0);
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    @Override
    public boolean shouldCheckMaintenance() {
        return false;
    }

    //#endregion

    //#region Misc TE Code

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEBECStorage(mName);
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
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();

        // spotless:off
        tt.addMachineType("Bose-Einstein Condensate Storage")
            .addInfo("Stores fancy atoms")
            .toolTipFinisher(GTValues.AuthorPineapple);
        // spotless:on

        return tt;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10_000;
    }

    @Override
    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);

        screenElements.widget(
            TextWidget.dynamicString(this::generateStoredCondensateText)
                .setTextAlignment(Alignment.CenterLeft));
    }

    protected String generateStoredCondensateText() {
        StringBuffer ret = new StringBuffer();

        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        for (var e : mStoredCondensate.object2LongEntrySet()) {
            ret.append(EnumChatFormatting.AQUA)
                .append(e.getKey().mLocalizedName)
                .append(" Condensate")
                .append(EnumChatFormatting.WHITE)
                .append(" x ")
                .append(EnumChatFormatting.GOLD);
            numberFormat.format(e.getLongValue(), ret);
            ret.append(" L")
                .append(EnumChatFormatting.WHITE);
            ret.append('\n');
        }

        return ret.toString();
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);

        aNBT.setInteger("mCondCount", mStoredCondensate.size());

        int cursor = 0;

        for (var e : mStoredCondensate.object2LongEntrySet()) {
            aNBT.setString("mCondName." + cursor, e.getKey().mName);
            aNBT.setLong("mCondAmount." + cursor, e.getLongValue());
            cursor++;
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);

        mStoredCondensate.clear();

        int count = aNBT.getInteger("mCondCount");

        for(int i = 0; i < count; i++) {
            Materials material = Materials.get(aNBT.getString("mCondName." + i));
            long amount = aNBT.getLong("mCondAmount." + i);

            if (material != null && material != Materials._NULL && amount > 0) {
                mStoredCondensate.put(material, amount);
            }
        }
    }

    @Override
    protected @NotNull CheckRecipeResult checkProcessing_EM() {
        mMaxProgresstime = 20;
        mEfficiency = 10_000;

        useLongPower = true;
        lEUt = 0;

        if (network == null) {
            BECFactoryGrid.INSTANCE.addElement(this);
        }

        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public void stopMachine(@Nonnull ShutDownReason reason) {
        super.stopMachine(reason);
        BECFactoryGrid.INSTANCE.removeElement(this);
    }

    @Override
    public void getNeighbours(Collection<BECFactoryElement> neighbours) {
        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base == null || base.isDead()) return;

        neighbours.addAll(mBECHatches);
    }

    @Override
    public BECFactoryNetwork getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(BECFactoryNetwork network) {
        this.network = network;
    }

    @Override
    public boolean canConnectOnSide(ForgeDirection side) {
        return false;
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
    public List<Pair<Class<?>, Object>> getComponents() {
        return Arrays.asList(
            Pair.of(BECInventory.class, this)
        );
    }

    @Override
    public @Nullable List<CondensateStack> getContents() {
        return mStoredCondensate.object2LongEntrySet()
            .stream()
            .map(e -> new CondensateStack(e.getKey(), e.getLongValue()))
            .collect(Collectors.toList());
    }

    @Override
    public void addCondensate(Collection<CondensateStack> stacks) {
        for (CondensateStack stack : stacks) {
            mStoredCondensate.merge(stack.material, stack.amount, Long::sum);
            stack.amount = 0;
        }
    }

    @Override
    public boolean removeCondensate(Collection<CondensateStack> stacks) {
        return false; // todo
    }
}
