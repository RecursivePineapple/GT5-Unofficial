package tectech.thing.metaTileEntity.multi.bec;

import static gregtech.api.casing.Casings.AdvancedFusionCoilII;
import static gregtech.api.casing.Casings.ElectromagneticWaveguide;
import static gregtech.api.casing.Casings.ElectromagneticallyIsolatedCasing;
import static gregtech.api.casing.Casings.FineStructureConstantManipulator;
import static gregtech.api.casing.Casings.SuperconductivePlasmaEnergyConduit;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.OutputBus;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.NaniteTier;
import gregtech.api.interfaces.IDataCopyable;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.structure.MultiblockTooltipBuilder2;
import gregtech.api.util.GTBECRecipe;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IGTHatchAdder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReason;
import it.unimi.dsi.fastutil.ints.IntIterator;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import tectech.recipe.TecTechRecipeMaps;
import tectech.thing.CustomItemList;
import tectech.thing.metaTileEntity.hatch.MTEHatchNaniteDetector;
import tectech.thing.metaTileEntity.multi.MTEBECAssembler;
import tectech.thing.metaTileEntity.multi.base.MTEBECMultiblockBase;
import tectech.thing.metaTileEntity.multi.structures.BECStructureDefinitions;

public class MTEBECIONode extends MTEBECMultiblockBase<MTEBECIONode> implements IDataCopyable {

    private int assemblerX, assemblerY, assemblerZ;
    private @Nullable MTEBECAssembler assembler;

    /** Not persisted after a restart, be careful when using this field. */
    private @Nullable GTBECRecipe currentRecipe;

    private @Nullable NaniteTier[] requiredNanites;

    private @Nullable NaniteTier providedTier, requiredTier;
    private int availableNanites;

    private List<MTEHatchNaniteDetector> naniteDetectors = new ArrayList<>();

    public MTEBECIONode(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected MTEBECIONode(MTEBECIONode prototype) {
        super(prototype);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEBECIONode(this);
    }

    @Override
    public String[][] getDefinition() {
        return BECStructureDefinitions.BEC_IO_NODE;
    }

    @Override
    public IStructureDefinition<MTEBECIONode> compile(String[][] definition) {
        structure.addCasing('A', SuperconductivePlasmaEnergyConduit);
        structure.addCasingWithHatches('B', ElectromagneticallyIsolatedCasing, 1, 16, Arrays.asList(Energy, ExoticEnergy, InputBus, OutputBus, NaniteHatch.INSTANCE));
        structure.addCasing('C', FineStructureConstantManipulator);
        structure.addCasing('D', AdvancedFusionCoilII);
        structure.addCasing('E', ElectromagneticWaveguide);

        return structure.buildStructure(definition);
    }

    @Override
    protected void clearHatches_EM() {
        super.clearHatches_EM();

        naniteDetectors.clear();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder2<MTEBECIONode> tt = new MultiblockTooltipBuilder2<>(structure);

        tt.addMachineType("Input bus, Output bus")
            .addInfo("Teleports stuff");

        tt.beginStructureBlock();
        tt.addAllCasingInfo();

        tt.toolTipFinisher(EnumChatFormatting.WHITE, 0, GTValues.AuthorPineapple);

        return tt;
    }

    @Override
    protected ITexture getCasingTexture() {
        return SuperconductivePlasmaEnergyConduit.getCasingTexture();
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return TecTechRecipeMaps.condensateAssemblingRecipes;
    }

    @Override
    public boolean supportsInputSeparation() {
        return true;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {

            @Override
            protected @NotNull CheckRecipeResult onRecipeStart(@NotNull GTRecipe recipe) {
                setCurrentRecipe((GTBECRecipe) recipe);

                return super.onRecipeStart(recipe);
            }
        };
    }

    @Override
    public void stopMachine(@NotNull ShutDownReason reason) {
        super.stopMachine(reason);

        setCurrentRecipe(null);
    }

    @Override
    public void outputAfterRecipe_EM() {
        super.outputAfterRecipe_EM();

        setCurrentRecipe(null);
    }

    private @Nullable MTEBECAssembler getAssembler() {
        IGregTechTileEntity igte = getBaseMetaTileEntity();

        if (igte.isDead()) return null;

        if (!isServerSide()) return null;

        if (!(igte.getTileEntity(assemblerX, assemblerY, assemblerZ) instanceof IGregTechTileEntity other)) return null;

        if (!(other.getMetaTileEntity() instanceof MTEBECAssembler assembler)) return null;

        return assembler;
    }

    private static final CheckRecipeResult NO_ASSEMBLER = SimpleCheckRecipeResult.ofFailure("no_bec_assembler");

    @Override
    protected @NotNull CheckRecipeResult checkProcessing_EM() {
        if (assembler == null) {
            connect(getAssembler());
        }

        if (assembler == null) return NO_ASSEMBLER;

        return super.checkProcessing_EM();
    }

    private void setCurrentRecipe(@Nullable GTBECRecipe recipe) {
        currentRecipe = recipe;
        requiredNanites = recipe == null ? null : recipe.mInputTiers;
        setRequiredTier(getRequiredNaniteTier(0));
    }

    public void setNaniteShare(NaniteTier providedTier, int nanites) {
        this.providedTier = providedTier;
        availableNanites = nanites;
    }

    private void setRequiredTier(NaniteTier tier) {
        if (tier != requiredTier) {
            requiredTier = tier;

            Iterator<MTEHatchNaniteDetector> iter = naniteDetectors.iterator();

            while (iter.hasNext()) {
                MTEHatchNaniteDetector naniteDetector = iter.next();

                if (naniteDetector == null || !naniteDetector.isValid()) {
                    iter.remove();
                    continue;
                }

                naniteDetector.setRequiredTier(requiredTier);
            }
        }
    }

    @Override
    protected void setProcessingLogicPower(ProcessingLogic logic) {
        logic.setAmperageOC(false);
        logic.setAvailableVoltage(GTUtility.roundUpVoltage(this.getMaxInputVoltage()));
        logic.setAvailableAmperage(1);
        logic.setMaxParallel(Math.max(1, availableNanites));
    }

    private IntDivisionIterator getCurrentSlot(int progress) {
        IntDivisionIterator iter = new IntDivisionIterator(mMaxProgresstime, requiredNanites.length);

        while (iter.sum + iter.peek() <= progress && iter.hasNext()) {
            iter.nextInt();
        }

        return iter;
    }

    private NaniteTier getRequiredNaniteTier(int progress) {
        if (requiredNanites == null || mMaxProgresstime == 0) return null;

        IntDivisionIterator iter = getCurrentSlot(progress);

        return GTUtility.getIndexSafe(requiredNanites, iter.counter);
    }

    private IntDivisionIterator getNextProgressGate(int progress) {
        if (requiredNanites == null || mMaxProgresstime == 0) return null;

        IntDivisionIterator iter = getCurrentSlot(progress);

        while(iter.hasNext() && iter.counter < requiredNanites.length && requiredNanites[iter.counter].tier <= providedTier.tier) {
            iter.nextInt();
        }

        return iter;
    }

    @Override
    protected void incrementProgressTime() {
        this.requiredTier = getRequiredNaniteTier(mProgresstime);
        setRequiredTier(requiredTier);

        // sanity check, this should never happen
        if (requiredTier == null) return;

        // if the provided tier is insufficient, do nothing
        if (providedTier == null || providedTier.tier < requiredTier.tier) return;

        IntDivisionIterator iter = getNextProgressGate(mProgresstime);

        if (iter != null) {
            mProgresstime = Math.min(iter.sum, mProgresstime + availableNanites);
        }
    }

    private void connect(MTEBECAssembler assembler) {
        if (!isServerSide()) return;

        disconnect();

        if (assembler != null) {
            this.assembler = assembler;
            assembler.addIONode(this);
        }
    }

    public void disconnect() {
        if (!isServerSide()) return;

        if (assembler != null) {
            assembler.removeIONode(this);
            assembler = null;
        }

        setNaniteShare(null, 0);
    }

    @Override
    public void onFirstTick_EM(IGregTechTileEntity igte) {
        super.onFirstTick_EM(igte);

        if (isServerSide()) {
            connect(getAssembler());
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();

        if (isServerSide()) {
            disconnect();
        }
    }

    @Override
    public void onPostTick(IGregTechTileEntity igte, long aTick) {
        super.onPostTick(igte, aTick);

        if (isServerSide()) {
            // periodically try to reconnect to the assembler if we're supposed to be running but the assembler isn't loaded
            if (assembler == null && mMaxProgresstime > 0 && aTick % 200 == 0) {
                connect(getAssembler());
            }
        }
    }

    @Override
    public void onLeftclick(IGregTechTileEntity igte, EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;

        ItemStack heldItem = player.getHeldItem();
        if (!ItemList.Tool_DataStick.isStackEqual(heldItem, false, true)) return;

        heldItem.setTagCompound(getCopiedData(player));
        heldItem.setStackDisplayName(MessageFormat.format(
            "{0} Link Data Stick ({1}, {2}, {3})",
            getStackForm(1).getDisplayName(),
            igte.getXCoord(),
            igte.getYCoord(),
            igte.getZCoord()));
        player.addChatMessage(new ChatComponentText("Saved Link Data to Data Stick"));
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity igte, EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem();
        if (!ItemList.Tool_DataStick.isStackEqual(heldItem, false, true)) {
            return super.onRightclick(igte, player);
        }

        // intentionally run on the client so that the player's arm swings
        if (pasteCopiedData(player, heldItem.getTagCompound())) {
            if (isServerSide()) {
                player.addChatMessage(new ChatComponentText("Successfully connected to " + CustomItemList.Machine_Multi_BECAssembler.getDisplayName()));
            }

            return true;
        } else {
            if (isServerSide()) {
                player.addChatMessage(new ChatComponentText("Could not connect to " + CustomItemList.Machine_Multi_BECAssembler.getDisplayName()));
            }

            return false;
        }
    }

    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString("type", getCopiedDataIdentifier(player));
        tag.setInteger("x", assemblerX);
        tag.setInteger("y", assemblerY);
        tag.setInteger("z", assemblerZ);

        return tag;
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (!nbt.getString("type").equals(getCopiedDataIdentifier(player))) return false;

        assemblerX = nbt.getInteger("x");
        assemblerY = nbt.getInteger("y");
        assemblerZ = nbt.getInteger("z");

        disconnect();
        connect(getAssembler());

        return true;
    }

    @Override
    public String getCopiedDataIdentifier(EntityPlayer player) {
        return "bec-assembler";
    }

    private float getProcessingSpeed() {
        return processingLogic.getCurrentParallels() == 0 ? 0 : availableNanites / (float) processingLogic.getCurrentParallels();
    }

    @Override
    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);

        screenElements.widget(
            new FakeSyncWidget.IntegerSyncer(
                () -> saveNanite(providedTier),
                matId -> providedTier = loadNanite(matId)));

        screenElements.widget(
            new FakeSyncWidget.IntegerSyncer(
                () -> availableNanites,
                amount -> availableNanites = amount));

        screenElements.widget(
            new FakeSyncWidget.IntegerSyncer(
                () -> saveNanite(getRequiredNaniteTier(mProgresstime)),
                matId -> requiredTier = loadNanite(matId)));
    }

    @Override
    protected String generateCurrentRecipeInfoString() {
        StringBuilder ret = new StringBuilder();

        ret.append(EnumChatFormatting.WHITE);

        if (providedTier == null) {
            ret.append("Provided: None\n");
        } else {
            ret.append(MessageFormat.format("Provided: {0} x {1}\n", availableNanites, providedTier.describe()));
        }

        if (requiredTier == null) {
            ret.append("Required: None\n");
        } else {
            ret.append(MessageFormat.format("Required: {0}\n", requiredTier.describe()));
        }

        return ret + super.generateCurrentRecipeInfoString();
    }

    @Override
    protected void generateRecipeProgressString(StringBuffer ret) {
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(0);
        numberFormat.format(mProgresstime, ret);
        ret.append(" / ");
        numberFormat.format(mMaxProgresstime, ret);
        ret.append(" (");
        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(1);
        numberFormat.format((double) mProgresstime / mMaxProgresstime * 100, ret);
        ret.append("%)\n");
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);
    }

    @Override
    protected void generateRate(StringBuffer ret, double amount) {
        // do nothing, rates can't be calculated easily
    }

    private static int saveNanite(NaniteTier tier) {
        return tier == null ? -1 : tier.getMaterial().mMetaItemSubID;
    }

    private static NaniteTier loadNanite(int id) {
        return NaniteTier.fromMaterial(GTUtility.getIndexSafe(GregTechAPI.sGeneratedMaterials, id));
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);

        tag.setInteger("x", assemblerX);
        tag.setInteger("y", assemblerY);
        tag.setInteger("z", assemblerZ);
        tag.setBoolean("con", assembler != null);
        tag.setInteger("nanite", saveNanite(getRequiredNaniteTier(mProgresstime + 1)));
        tag.setInteger("provided", saveNanite(providedTier));
        tag.setFloat("speed", getProcessingSpeed());
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);

        NBTTagCompound tag = accessor.getNBTData();

        currenttip.add(MessageFormat.format("Assembler: {0},{1},{2}", tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")));
        currenttip.add(MessageFormat.format("Connected: {0}", tag.getBoolean("con")));

        NaniteTier required = loadNanite(tag.getInteger("nanite"));
        NaniteTier provided = loadNanite(tag.getInteger("provided"));

        currenttip.add(MessageFormat.format("Required Tier: {0}", required == null ? "None" : required.describe()));
        currenttip.add(MessageFormat.format("Provided Tier: {0}", provided == null ? "None" : provided.describe()));
        currenttip.add(MessageFormat.format("Processing Speed: x{0}", tag.getFloat("speed")));
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);

        aNBT.setInteger("assemblerX", assemblerX);
        aNBT.setInteger("assemblerY", assemblerY);
        aNBT.setInteger("assemblerZ", assemblerZ);

        if (requiredNanites != null) {
            aNBT.setInteger("naniteCount", requiredNanites.length);

            for (int i = 0; i < requiredNanites.length; i++) {
                aNBT.setInteger("nanite" + i, saveNanite(requiredNanites[i]));
            }
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);

        assemblerX = aNBT.getInteger("assemblerX");
        assemblerY = aNBT.getInteger("assemblerY");
        assemblerZ = aNBT.getInteger("assemblerZ");

        int count = aNBT.getInteger("naniteCount");

        requiredNanites = new NaniteTier[count];

        for (int i = 0; i < count; i++) {
            requiredNanites[i] = loadNanite(aNBT.getInteger("nanite" + i));
        }
    }

    /**
     * A deterministic division algorithm that splits an int up into properly rounded chunks, such that the sum of each
     * chunk equals the original number.
     * Rounding will be performed as equally as possible.
     */
    public static class IntDivisionIterator implements IntIterator {

        public int remaining, divisor, counter, sum;

        public IntDivisionIterator(int total, int divisor) {
            this.remaining = total;
            this.divisor = divisor;
            this.counter = 0;
            this.sum = 0;
        }

        @Override
        public int nextInt() {
            if (remaining == 0) return 0;

            int value = peek();

            remaining -= value;
            sum += value;
            counter++;

            return value;
        }

        public int peek() {
            int bucketsLeft = divisor - counter;

            if (bucketsLeft == 0) return 0;

            if (bucketsLeft == 1) {
                return remaining;
            } else {
                return Math.round(remaining / (float) (divisor - counter));
            }
        }

        @Override
        public boolean hasNext() {
            return remaining > 0;
        }
    }

    public enum NaniteHatch implements IHatchElement<MTEBECIONode> {

        INSTANCE;

        @Override
        public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
            return Collections.singletonList(MTEHatchNaniteDetector.class);
        }

        @Override
        public String getDisplayName() {
            return CustomItemList.Hatch_BEC_Nanites.getDisplayName();
        }

        @Override
        public long count(MTEBECIONode self) {
            return self.naniteDetectors.size();
        }

        @Override
        public IGTHatchAdder<MTEBECIONode> adder() {
            return (self, igtme, id) -> {
                IMetaTileEntity imte = igtme.getMetaTileEntity();

                if (imte instanceof MTEHatchNaniteDetector hatch) {
                    hatch.updateTexture(id);
                    hatch.updateCraftingIcon(self.getMachineCraftingIcon());

                    self.naniteDetectors.add(hatch);
                    if (self.mMaxProgresstime > 0) {
                        hatch.setRequiredTier(self.getRequiredNaniteTier(self.mProgresstime));
                    }

                    return true;
                } else {
                    return false;
                }
            };
        }
    }
}
